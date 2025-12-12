package moe.ichinomiya.naven.utils;

import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.ui.notification.Notification;
import lombok.Getter;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.protocols.HuaYuTing.HYTUtils;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.potion.Potion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityWatcher {
    private static final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private static final List<EntityPlayer> godAxe = new ArrayList<>();
    @Getter
    private static final List<EntityPlayer> enchantedGApple = new ArrayList<>();
    @Getter
    private static final Map<String, SharedESPData> sharedESPData = new ConcurrentHashMap<>();

    public static int fullPlayerPlayerCounter = 0;

    @EventTarget
    public void onDispatch(EventDispatchPacket e) {
        if (e.getPacket() instanceof C03PacketPlayer) {
            if (e.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition || e.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
                fullPlayerPlayerCounter = 0;
            } else {
                fullPlayerPlayerCounter ++;
            }
        }
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        if (e.getType() == EventType.JOIN_GAME) {
            godAxe.clear();
            enchantedGApple.clear();
        }
    }

    @EventTarget
    public void onPre(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (HYTUtils.isHoldingGodAxe(player) && !godAxe.contains(player)) {
                    Notification navenNotification = new Notification(NotificationLevel.WARNING, player.getName() + " is holding god axe!", 3000);
                    BlinkFix.getInstance().getNotificationManager().addNotification(navenNotification);
                    godAxe.add(player);
                }

                if (HYTUtils.isHoldingEnchantedGoldenApple(player) && !enchantedGApple.contains(player)) {
                    Notification navenNotification = new Notification(NotificationLevel.WARNING, player.getName() + " is holding enchanted golden apple!", 3000);
                    BlinkFix.getInstance().getNotificationManager().addNotification(navenNotification);
                    enchantedGApple.add(player);
                }
            }
        }
    }

    @EventTarget(Priority.HIGHEST)
    public void generateEntityTags(EventMotion e) {
        if (e.getType() == EventType.POST) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                player.setTags(generateEntityTags(player));
            }
        }
    }

    private static List<String> generateEntityTags(EntityPlayer entity) {
        ArrayList<String> description = new ArrayList<>();
        if (godAxe.contains(entity)) {
            description.add("God Axe");
        }

        if (enchantedGApple.contains(entity)) {
            description.add("Enchanted Golden Apple");
        }

        List<Potion> potions = PotionResolver.resolve(entity.getDataWatcher().getWatchableObjectInt(7));

        if (potions.contains(Potion.regeneration)) {
            description.add(Potion.regeneration.getName());
        }

        if (potions.contains(Potion.moveSpeed)) {
            description.add(Potion.moveSpeed.getName());
        }

        if (potions.contains(Potion.damageBoost)) {
            description.add(Potion.damageBoost.getName());
        }

        if (potions.contains(Potion.resistance)) {
            description.add(Potion.resistance.getName());
        }

        if (potions.contains(Potion.jump)) {
            description.add(Potion.jump.getName());
        }

        return description;
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.RECEIVE && e.getPacket() instanceof S0EPacketSpawnObject) {
            S0EPacketSpawnObject packet = (S0EPacketSpawnObject) e.getPacket();

            if (packet.getType() == 2) {
                double posX = packet.getX() / 32D;
                double posY = packet.getY() / 32D;
                double posZ = packet.getZ() / 32D;

                Optional<EntityPlayer> predictedPlayer = Minecraft.getMinecraft().theWorld.playerEntities.stream()
                        .filter(p -> p.getDistance(posX, posY, posZ) < 1)
                        .min(Comparator.comparingDouble(p -> p.getDistance(posX, posY, posZ)));

                predictedPlayer.ifPresent(player -> player.getThrowItemCounter().incrementAndGet());
            }
        }
    }

    @EventTarget
    public void onGlobalPacket(EventGlobalPacket e) {
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;

        if (e.getType() == EventType.RECEIVE && theWorld != null) {
            if (e.getPacket() instanceof S04PacketEntityEquipment) {
                S04PacketEntityEquipment packet = (S04PacketEntityEquipment) e.getPacket();
                Entity entity = theWorld.getEntityByID(packet.getEntityID());

                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    player.serverUsingItem = false;
                }
            }

            if (e.getPacket() instanceof S1CPacketEntityMetadata) {
                S1CPacketEntityMetadata packet = (S1CPacketEntityMetadata) e.getPacket();
                Entity entity = theWorld.getEntityByID(packet.getEntityId());
                if (entity instanceof EntityPlayer && packet.getWatchableObjects() != null) {
                    EntityPlayer player = (EntityPlayer) entity;

                    for (DataWatcher.WatchableObject object : packet.getWatchableObjects()) {
                        if (object.getObjectType() == 0 && object.getDataValueId() == 0) {
                            ItemStack heldItem = ((EntityPlayer) entity).getHeldItem();
                            if (heldItem != null) {
                                Item item = heldItem.getItem();
                                if (item instanceof ItemSword || item instanceof ItemBow || item instanceof ItemFood || item instanceof ItemPotion) {
                                    boolean usingItemState = (((byte) object.getObject()) & 1 << 4) != 0;

                                    if (usingItemState) {
                                        player.getBlockTime().add(System.currentTimeMillis());
                                    }

                                    // 使用物品
                                    if (!player.serverUsingItem && usingItemState) {
                                        player.serverUsingItem = true;
                                    }

                                    // 取消使用物品
                                    if (player.serverUsingItem && !usingItemState) {
                                        player.serverUsingItem = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (e.getPacket() instanceof S0BPacketAnimation) {
                S0BPacketAnimation packet = (S0BPacketAnimation) e.getPacket();

                if (packet.getAnimationType() == 0) {
                    Entity entity = theWorld.getEntityByID(packet.getEntityID());
                    if (entity instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entity;
                        player.getSwingTime().add(System.currentTimeMillis());
                    }
                }
            }
        }
    }
}
