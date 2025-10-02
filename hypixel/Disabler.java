package moe.ichinomiya.naven.modules.impl.misc;

import lombok.extern.log4j.Log4j2;
import moe.ichinomiya.naven.modules.impl.combat.AutoGapple;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import tech.NavenLoader.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.cooldown.CooldownBar;
import moe.ichinomiya.naven.utils.ChatUtils;
import moe.ichinomiya.naven.utils.ServerUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemFood;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
@ModuleInfo(name = "Disabler", description = "Disable some checks of anticheats.", category = Category.MISC)
public class Disabler extends Module {
    private final BooleanValue logging = ValueBuilder.create(this, "Logging").setDefaultBooleanValue(false).build().getBooleanValue();
    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Grim", "Hypixel").build().getModeValue();
    public final Queue<Queue<Packet<INetHandlerPlayClient>>> delayedServerPackets = new ConcurrentLinkedQueue<>();
    BooleanValue Inv= ValueBuilder.create(this, "Hypixel Inventory").setDefaultBooleanValue(false).build().getBooleanValue();
    public final Queue<Packet<INetHandlerPlayServer>> delayedClientPackets = new ConcurrentLinkedQueue<>();
    private final Queue<Packet<INetHandlerPlayClient>> releasingPackets = new ConcurrentLinkedQueue<>();
    private Queue<Packet<INetHandlerPlayClient>> lastServerPackets;
    private boolean c16 = false;
    private boolean c0d = false;
    public static boolean disabled = false;
    private boolean added = false;
    private final Map<Integer, EntityOtherPlayerMP> fakePlayers = new ConcurrentHashMap<>();

    private final CooldownBar cooldownBar = new CooldownBar(1000, "Time Balance") {
        @Override
        public float getState() {
            return 1F - (delayedServerPackets.size() / 190f);
        }
    };

    private static final List<Class<? extends Packet<INetHandlerPlayClient>>> whitelistPackets = Arrays.asList(
            S06PacketUpdateHealth.class,
            S3EPacketTeams.class,
            S3DPacketDisplayScoreboard.class,
            S29PacketSoundEffect.class,
            S47PacketPlayerListHeaderFooter.class,
            S3BPacketScoreboardObjective.class,
            S44PacketWorldBorder.class,
            S21PacketChunkData.class,
            S03PacketTimeUpdate.class,
            S19PacketEntityStatus.class,
            S02PacketChat.class,
            S45PacketTitle.class,
            S2APacketParticles.class,
            S1FPacketSetExperience.class,
            S3CPacketUpdateScore.class,
            S3FPacketCustomPayload.class,
            S37PacketStatistics.class,
            S07PacketRespawn.class,
            S3APacketTabComplete.class,
            S49PacketUpdateEntityNBT.class,
            S2CPacketSpawnGlobalEntity.class,
            S01PacketJoinGame.class,
            S0EPacketSpawnObject.class
    );
    private int lastSlot = -1;

    @Override
    public boolean onDisable() {
        disabled = false;
        releaseAllServerPackets();
        return false;
    }

    @Override
    public boolean onEnable() {
        delayedServerPackets.clear();
        delayedClientPackets.clear();
        return false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        delayedClientPackets.clear();
        fakePlayers.clear();
        worldTick = 0;
        playerTick = 0;
        positions.clear();

        if (e.getType() == EventType.JOIN_GAME) {
            mc.getNetHandler().playerInfoMap.clear();
            delayedServerPackets.clear();
        }
    }

    @EventTarget
    public void onRenderEntity(EventRenderEntity e) {
        if (mode.isCurrentMode("Grim")) {
            Stuck stuck = (Stuck) Naven.getInstance().getModuleManager().getModule(Stuck.class);
            if (e.getEntity() instanceof EntityOtherPlayerMP && ((EntityOtherPlayerMP) e.getEntity()).isFakePlayer() && delayedServerPackets.size() > 1 && !stuck.isEnabled()) {
                e.setCancelled(false);
            }
        }
    }

    @EventTarget
    public void onRenderEntity(EventRendererLivingEntity e) {
        if (e.getEntity() instanceof EntityOtherPlayerMP && ((EntityOtherPlayerMP) e.getEntity()).isFakePlayer()) {
            e.setAlpha(0.5f);
            e.setShouldInvisible(true);
        }
    }

    private boolean shouldDisable() {
        FlyMeToTheMoon module = (FlyMeToTheMoon) Naven.getInstance().getModuleManager().getModule(FlyMeToTheMoon.class);
        return mc.thePlayer != null && mc.thePlayer.ticksExisted > 20 && ServerUtils.getGrimTransactionCount() > 50 && !mc.thePlayer.isDead && !mc.thePlayer.isSpectator() && !mc.isSingleplayer() && (!module.isEnabled() || module.ticks < 30);
    }

    @EventTarget(Priority.LOWEST)
    public void onTicks(EventRunTicks e) {
        if (mc.thePlayer == null) {
            delayedServerPackets.clear();
        }

        if(mode.isCurrentMode("Grim")) {
            if (shouldDisable()) {
                if (!added) {
                    setSuffix(delayedServerPackets.size() + " Ticks Delayed");
                    synchronized (delayedServerPackets) {
                        delayedServerPackets.add(lastServerPackets = new ConcurrentLinkedQueue<>());
                    }
                }
            } else {
                setSuffix("Stopped");
            }

            added = false;

            if (delayedServerPackets.size() > 1) {
                cooldownBar.setCreateTime(System.currentTimeMillis());
                Naven.getInstance().getCooldownBarManager().addBar(cooldownBar);
            }
        }
        if (mode.isCurrentMode("Hypixel")){
            setSuffix("Hypixel");
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (shouldDisable()) {
                if (!Naven.getInstance().getModuleManager().getModule(TimeBalanceAbuse.class).isEnabled() && mode.isCurrentMode("Grim")) {
                    if (delayedServerPackets.size() > 1) {
                        mc.thePlayer.timer = 3;
                    } else {
                        mc.thePlayer.timer = 1;
                    }
                }
            }
        }

        if (mode.isCurrentMode("Hypixel") && Inv.getCurrentValue()){
            c16 = false;
            c0d = false;
            if (mc.currentScreen instanceof GuiInventory || Naven.getInstance().getModuleManager().getModule(InventoryManager.class).isEnabled()) {
                if (mc.thePlayer.ticksExisted % (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 3 : 4) == 0) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C0DPacketCloseWindow());
                } else if (mc.thePlayer.ticksExisted % (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 3 : 4) == 1) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                }
            }
        }
    }

    private void log(String message) {
        if (logging.getCurrentValue()) {
            ChatUtils.addChatMessage(message);
        }
    }

    private int dispatchEntityActionCount = 0;

    @EventTarget
    public void badPacketUDetector(EventDispatchPacket e) {
        if (mode.isCurrentMode("Grim")) {
            if (e.getPacket() instanceof C03PacketPlayer) {
                dispatchEntityActionCount = 0;
            }

            if (e.getPacket() instanceof C0BPacketEntityAction && mode.isCurrentMode("Grim")) {
                C0BPacketEntityAction packet = (C0BPacketEntityAction) e.getPacket();
                if (packet.getAction() == C0BPacketEntityAction.Action.START_SPRINTING || packet.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                    if (dispatchEntityActionCount > 0) {
                        //log("You may just flagged BadPacketU!");
                    }
                    dispatchEntityActionCount++;
                }
            }
        }
    }

    private int heldItemChange = 0, clickWindow = 0;

    @EventTarget(Priority.LOW)
    public void duplicatePacketDisabler(EventPacket e) {
        if (e.getType() == EventType.SEND && !e.isCancelled() && mc.thePlayer != null && mode.isCurrentMode("Grim")) {

            if (e.getPacket() instanceof C09PacketHeldItemChange && !Naven.getInstance().getModuleManager().getModule(AutoArmor.class).isEnabled()) {
                if (heldItemChange > 0) {
                    // log("Bypassing Duplicate HeldItemChange Packet!");
                    mc.getNetHandler().getNetworkManager().sendPacket(new C0FPacketConfirmTransaction(-1337, (short) 1337, true));
                }

                heldItemChange++;
            } else {
                heldItemChange = 0;
            }

            if (e.getPacket() instanceof C0EPacketClickWindow) {
                C0EPacketClickWindow packet = (C0EPacketClickWindow) e.getPacket();

                if (packet.getWindowId() != 0) {
                    if (clickWindow > 0) {
                        // log("Bypassing Duplicate ClickWindow Packet!");
                        mc.getNetHandler().getNetworkManager().sendPacket(new C0FPacketConfirmTransaction(-1337, (short) 1337, true));
                    }

                    clickWindow++;
                }
            } else {
                clickWindow = 0;
            }
        }
    }

    long lastPacketTime = 0;

    @EventTarget(Priority.LOWEST)
    public void blinkDetectionDisabler(EventDispatchPacket e) {
        if (mc.isSingleplayer()) {
            return;
        }
            if (!e.isCancelled() && mc.getNetHandler() != null && mode.isCurrentMode("Grim")) {
                if (System.currentTimeMillis() - lastPacketTime > 100) {
                    if (!(e.getPacket() instanceof C0FPacketConfirmTransaction) && !(e.getPacket() instanceof C03PacketPlayer) && !Naven.getInstance().getModuleManager().getModule(AutoGapple.class).isEnabled()) {
                        log("Disabling Blink!");
                        e.getAdditionalPackets().add(new C0FPacketConfirmTransaction(-1337, (short) 1337, true));
                    }


                lastPacketTime = System.currentTimeMillis();
            }
        }
    }

    public static int worldTick, playerTick;

    @EventTarget
    public void worldTickCounter(EventRunTicks e) {
        if (mc.thePlayer != null) {
            worldTick++;
        }
    }
    @EventTarget
    public void onPacket(EventPacket e){
        Packet packet = e.getPacket();
       if (mode.isCurrentMode("Hypixel") && Inv.getCurrentValue()){
            if (e.getPacket() instanceof C16PacketClientStatus) {
                if (c16) {
                    e.setCancelled(true);
                }
                c16 = true;
            }
            if (e.getPacket() instanceof C0DPacketCloseWindow) {
                if (c0d) {
                    e.setCancelled(true);
                }
                c0d = true;
            }
        }
    }

    @EventTarget
    public void playerTickCounter(EventMotion e) {
        if (e.getType() == EventType.POST) {
            playerTick = worldTick - delayedServerPackets.size();
        }
    }

    private float playerYaw;
    private float deltaYaw;
    private float lastPlacedDeltaYaw;
    private boolean rotated = false;

    @EventTarget(Priority.LOW)
    public void duplicateRotPlaceDisabler(EventPacket e) {
        if (e.getType() == EventType.SEND && !e.isCancelled() && mc.thePlayer != null && mode.isCurrentMode("Grim")) {
            if (e.getPacket() instanceof C03PacketPlayer.C05PacketPlayerLook || e.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
                C03PacketPlayer packet = (C03PacketPlayer) e.getPacket();
                float lastPlayerYaw = playerYaw;
                playerYaw = packet.getYaw();
                deltaYaw = Math.abs(playerYaw - lastPlayerYaw);
                rotated = true;

                // Guess what will happen if you placed the block in current tick
                if (deltaYaw > 2) {
                    float xDiff = Math.abs(deltaYaw - lastPlacedDeltaYaw);
                    if (xDiff < 0.0001) {
                        log("Disabling DuplicateRotPlace!");
                        packet.setYaw(packet.getYaw() + 0.0002F);
                    }
                }
            } else if (e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                if (rotated) {
                    lastPlacedDeltaYaw = deltaYaw;
                    rotated = false;
                }
            }
        }
    }

    public static final Map<Integer, Map<Entity, Double[]>> positions = new ConcurrentHashMap<>();

    private static void putData(int tick, Entity entity, Double[] position) {
        if (!positions.containsKey(tick)) {
            positions.put(tick, new ConcurrentHashMap<>());
        }

        Map<Entity, Double[]> positionMap = positions.get(tick);
        positionMap.put(entity, position);
    }

    private static Double[] getData(int tick, Entity entity) {
        if (!positions.containsKey(tick)) {
            return null;
        }

        Map<Entity, Double[]> positionMap = positions.get(tick);
        return positionMap.get(entity);
    }

    public static Double[] getNextTickPosition(Entity entity) {
        Double[] nextTickData = getData(playerTick + 1, entity);
        if (nextTickData != null) {
            return nextTickData;
        }

        Double[] currentTickData = getData(playerTick, entity);
        if (currentTickData != null) {
            return currentTickData;
        }

        return new Double[]{entity.posX, entity.posY, entity.posZ};
    }

    @EventTarget
    public void onAllPacket(EventGlobalPacket e) {
        Stuck stuck = (Stuck) Naven.getInstance().getModuleManager().getModule(Stuck.class);

        if (e.getType() == EventType.RECEIVE && mode.isCurrentMode("Grim")) {
            if (e.getPacket() instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport packet = (S18PacketEntityTeleport) e.getPacket();
                // set real position
                Entity realEntity = mc.theWorld.getEntityByID(packet.getEntityId());

                if (realEntity != null) {
                    realEntity.serverPosX2 = packet.getX();
                    realEntity.serverPosY2 = packet.getY();
                    realEntity.serverPosZ2 = packet.getZ();

                    double x = (double) realEntity.serverPosX2 / 32.0D;
                    double y = (double) realEntity.serverPosY2 / 32.0D;
                    double z = (double) realEntity.serverPosZ2 / 32.0D;

                    putData(worldTick, realEntity, new Double[]{x, y, z});

                    if (fakePlayers.containsKey(packet.getEntityId())) {
                        EntityOtherPlayerMP fakePlayer = fakePlayers.get(packet.getEntityId());
                        float yaw = (float) (packet.getYaw() * 360) / 256.0F;
                        float pitch = (float) (packet.getPitch() * 360) / 256.0F;

                        fakePlayer.setPositionAndRotation2(x, y, z, yaw, pitch, 3, true);
                    }
                }
            } else if (e.getPacket() instanceof S14PacketEntity) {
                S14PacketEntity packet = (S14PacketEntity) e.getPacket();
                Entity entity = packet.getEntity(mc.theWorld);

                if (entity != null) {
                    entity.serverPosX2 += packet.func_149062_c();
                    entity.serverPosY2 += packet.func_149061_d();
                    entity.serverPosZ2 += packet.func_149064_e();

                    double x = (double) entity.serverPosX2 / 32.0D;
                    double y = (double) entity.serverPosY2 / 32.0D;
                    double z = (double) entity.serverPosZ2 / 32.0D;

                    putData(worldTick, entity, new Double[]{x, y, z});

                    if (fakePlayers.containsKey(entity.getEntityId())) {
                        EntityOtherPlayerMP fakePlayer = fakePlayers.get(entity.getEntityId());
                        float yaw = packet.func_149060_h() ? (float) (packet.func_149066_f() * 360) / 256.0F : entity.rotationYaw;
                        float pitch = packet.func_149060_h() ? (float) (packet.func_149063_g() * 360) / 256.0F : entity.rotationPitch;

                        fakePlayer.setPositionAndRotation2(x, y, z, yaw, pitch, 3, false);
                    }
                }
            } else if (e.getPacket() instanceof S0FPacketSpawnMob) {
                S0FPacketSpawnMob packet = (S0FPacketSpawnMob) e.getPacket();
                Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());

                if (entity != null) {
                    entity.serverPosX2 = packet.getX();
                    entity.serverPosY2 = packet.getY();
                    entity.serverPosZ2 = packet.getZ();

                    double x = (double) entity.serverPosX2 / 32.0D;
                    double y = (double) entity.serverPosY2 / 32.0D;
                    double z = (double) entity.serverPosZ2 / 32.0D;

                    putData(worldTick, entity, new Double[]{x, y, z});
                }
            } else if (e.getPacket() instanceof S0EPacketSpawnObject) {
                S0EPacketSpawnObject packet = (S0EPacketSpawnObject) e.getPacket();

                Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());

                if (entity != null) {
                    entity.serverPosX2 = packet.getX();
                    entity.serverPosY2 = packet.getY();
                    entity.serverPosZ2 = packet.getZ();

                    double x = (double) entity.serverPosX2 / 32.0D;
                    double y = (double) entity.serverPosY2 / 32.0D;
                    double z = (double) entity.serverPosZ2 / 32.0D;

                    putData(worldTick, entity, new Double[]{x, y, z});
                }
            } else if (e.getPacket() instanceof S13PacketDestroyEntities) {
                S13PacketDestroyEntities packet = (S13PacketDestroyEntities) e.getPacket();
                for (int id : packet.getEntityIDs()) {
                    if (fakePlayers.containsKey(id)) {
                        mc.theWorld.removeEntityFromWorld(fakePlayers.get(id).getEntityId());
                        fakePlayers.remove(id);
                    }
                }
            } else if (e.getPacket() instanceof S0BPacketAnimation) {
                S0BPacketAnimation packet = (S0BPacketAnimation) e.getPacket();

                if (fakePlayers.containsKey(packet.getEntityID())) {
                    EntityOtherPlayerMP player = fakePlayers.get(packet.getEntityID());

                    if (packet.getAnimationType() == 0) {
                        player.swingItem();
                    } else if (packet.getAnimationType() == 1) {
                        player.performHurtAnimation();
                    }
                }
            }
        }

        if (shouldDisable() && !e.isCancelled() ) {
            if (e.getPacket() instanceof C03PacketPlayer) {
                disabled = true;

                C03PacketPlayer packet = (C03PacketPlayer) e.getPacket();

                if (!packet.setback) {
                    releaseClientPackets();
                }

                if (!stuck.isEnabled()) {
                    sendPacketWithEvent(e.getPacket());
                }

                if (!packet.setback) {
                    releaseServerPackets();
                }

                e.setCancelled(true);
            } else if (e.getPacket() instanceof C08PacketPlayerBlockPlacement && mode.isCurrentMode("Grim")) {
                C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) e.getPacket();
                if (packet.getStack() != null && packet.getStack().getItem() instanceof ItemFood) {
                    return;
                }
                delayedClientPackets.add(packet);
                    e.setCancelled(true);
            } else if (e.getPacket() instanceof C07PacketPlayerDigging && mode.isCurrentMode("Grim")) {
                C07PacketPlayerDigging packet = (C07PacketPlayerDigging) e.getPacket();
                delayedClientPackets.add(packet);
                e.setCancelled(true);
            } else if (e.getType() == EventType.RECEIVE) {
                if (e.getPacket() instanceof S0FPacketSpawnMob) {
                    S0FPacketSpawnMob packet = (S0FPacketSpawnMob) e.getPacket();

                    Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());

                    if (entity instanceof EntityArrow || entity instanceof EntityThrowable) {
                        return;
                    }
                } else if (e.getPacket() instanceof S14PacketEntity ) {
                    S14PacketEntity packet = (S14PacketEntity) e.getPacket();
                    Entity entity = packet.getEntity(mc.theWorld);

                    if (entity instanceof EntityThrowable || entity instanceof EntityArrow) {
                        return;
                    }
                } else if (e.getPacket() instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity packet = (S12PacketEntityVelocity) e.getPacket();
                    Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
                    if (entity instanceof EntityThrowable || entity instanceof EntityArrow) {
                        return;
                    }
                } else if (e.getPacket() instanceof S08PacketPlayerPosLook) {
                    if (stuck.isEnabled()) {
                        stuck.toggle();
                        releaseAllServerPackets();
                    }
                } else if (e.getPacket() instanceof S38PacketPlayerListItem) {
                    S38PacketPlayerListItem packet = (S38PacketPlayerListItem) e.getPacket();
                    if (packet.getAction() == S38PacketPlayerListItem.Action.UPDATE_GAME_MODE) {
                        return;
                    }
                } else if (e.getPacket() instanceof S1CPacketEntityMetadata) {
                    S1CPacketEntityMetadata packet = (S1CPacketEntityMetadata) e.getPacket();

                    if (mc.theWorld.getEntityByID(packet.getEntityId()) != mc.thePlayer) {
                        return;
                    }
                } else if (e.getPacket() instanceof S20PacketEntityProperties) {
                    S20PacketEntityProperties packet = (S20PacketEntityProperties) e.getPacket();

                    if (mc.theWorld.getEntityByID(packet.getEntityId()) != mc.thePlayer) {
                        return;
                    }
                } else if (whitelistPackets.contains(e.getPacket().getClass())) {
                    return;
                }

                addDelayServerPacket((Packet<INetHandlerPlayClient>) e.getPacket());
                e.setCancelled(true);
            }
        } else if (disabled) {
            disabled = false;
            releaseAllServerPackets();
        }
    }

    @EventTarget
    public void onPlayerSpawn(EventSpawnPlayer e) {
        EntityOtherPlayerMP player = e.getPlayer();

        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
        fakePlayer.copyLocationAndAnglesFrom(player);
        fakePlayer.setFakePlayer(true);
        fakePlayers.put(player.getEntityId(), fakePlayer);

        mc.theWorld.addEntityToWorld(-player.getEntityId() - 65535, fakePlayer);
    }

    private void sendPacketWithEvent(Packet<?> packet) {
        EventPacket event = new EventPacket(EventType.SEND, packet, EventPacket.EventState.SEND);
        Naven.getInstance().getEventManager().call(event);

        if (event.isCancelled()) {
            return;
        }

        packet = event.getPacket();
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
    }

    private void addDelayServerPacket(Packet<INetHandlerPlayClient> packet) {
        synchronized (delayedServerPackets) {
            if (delayedServerPackets.isEmpty() || lastServerPackets == null) {
                added = true;
                delayedServerPackets.add(lastServerPackets = new ConcurrentLinkedQueue<>());
            }

            lastServerPackets.add(packet);
        }
    }

    private void releaseAllServerPackets() {
        while (!delayedServerPackets.isEmpty()) {
            Queue<Packet<INetHandlerPlayClient>> queue = delayedServerPackets.poll();

            synchronized (delayedServerPackets) {
                if (queue == lastServerPackets) {
                    lastServerPackets = null;
                }
            }

            while (!queue.isEmpty()) {
                releasingPackets.offer(queue.poll());
            }
        }

        releasePackets();
    }

    private void releaseServerPackets() {
        if (!delayedServerPackets.isEmpty()) {
            Queue<Packet<INetHandlerPlayClient>> serverPackets = delayedServerPackets.poll();

            synchronized (delayedServerPackets) {
                if (serverPackets == lastServerPackets) {
                    lastServerPackets = null;
                }
            }

            while (!serverPackets.isEmpty()) {
                releasingPackets.offer(serverPackets.poll());
            }
        }

        releasePackets();
    }

    private void releasePackets() {
        mc.addScheduledTask(() -> {
            while (!releasingPackets.isEmpty()) {
                Packet<INetHandlerPlayClient> packet = releasingPackets.poll();
                releasePacket(packet);
            }
        });
    }

    private void releaseClientPackets() {
        while (!delayedClientPackets.isEmpty()) {
            sendPacketWithEvent(delayedClientPackets.poll());
        }
    }

    private void releasePacket(Packet<INetHandlerPlayClient> packet) {
        EventPacket event = new EventPacket(EventType.RECEIVE, packet, EventPacket.EventState.RECEIVE);
        Naven.getInstance().getEventManager().call(event);
        packet = (Packet<INetHandlerPlayClient>) event.getPacket();

        if (!event.isCancelled() && mc.getNetHandler() != null) {
            try {
                packet.processPacket(mc.getNetHandler());
            } catch (Exception e) {
                log.error("Error while releasing packet: " + packet.getClass().getSimpleName(), e);
            }
        }
    }
}
