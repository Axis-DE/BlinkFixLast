package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.misc.AutoPhase;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;

import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;
@ModuleInfo(name = "Blink", category = Category.MOVEMENT, description = "Suspends all movement packets for teleporting!")
public class Blink extends Module {
    public static Blink instance;
    private final static int mainColor = new Color(150, 45, 45, 255).getRGB();
    public static EntityOtherPlayerMP localPlayer = null;
    public final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    ModeValue mode = ValueBuilder.create(this, "Blink Mode").setDefaultModeIndex(0).setModes("Maximum Time", "Back Track", "Anti Aim", "Delay Release", "Normal").build().getModeValue();
    public FloatValue blinkTicks = ValueBuilder.create(this, "Ticks").setDefaultFloatValue(100).setFloatStep(1).setMinFloatValue(10).setMaxFloatValue(190).build().getFloatValue();
    FloatValue alpha = ValueBuilder.create(this, "Fake Player Alpha").setDefaultFloatValue(1).setFloatStep(0.05f).setMinFloatValue(0).setMaxFloatValue(1).build().getFloatValue();
    public BooleanValue render = ValueBuilder.create(this, "Render").setDefaultBooleanValue(true).build().getBooleanValue();
    public ModeValue rendermode = ValueBuilder.create(this, "Render Mode")
            .setDefaultModeIndex(0)
            .setModes("Naven", "DynamicIsland")
            .setVisibility(() -> render.getCurrentValue())
            .build()
            .getModeValue();
    BooleanValue moveOnDamage = ValueBuilder.create(this, "Move on Damage").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue damageMoveTicks = ValueBuilder.create(this, "Damage Move Ticks").setDefaultFloatValue(50).setFloatStep(1).setMinFloatValue(1).setMaxFloatValue(190).setVisibility(() -> moveOnDamage.getCurrentValue()).build().getFloatValue();

    BooleanValue slowMove = ValueBuilder.create(this, "Slow Move").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue slowMoveTicks = ValueBuilder.create(this, "Slow Move Ticks").setDefaultFloatValue(3).setFloatStep(1).setMinFloatValue(1).setMaxFloatValue(5).setVisibility(() -> slowMove.getCurrentValue()).build().getFloatValue();
    long time;
    ScaledResolution resolution;
    SmoothAnimationTimer progress = new SmoothAnimationTimer(0, 0.2f);
    int ticks = 0;
    private double posX, posY, posZ;
    private int ReleaseCount = 0;

    @Override
    public boolean onEnable() {
        packets.clear();
        time = System.currentTimeMillis();
        localPlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        localPlayer.setFakePlayer(true);
        localPlayer.copyLocationAndAnglesFrom(mc.thePlayer);
        localPlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
        localPlayer.setSprinting(mc.thePlayer.serverSprintState);
        mc.thePlayer.capabilities.allowEdit = true;
        mc.theWorld.addEntityToWorld(-1337, localPlayer);
        progress.value = progress.target = mode.isCurrentMode("Maximum") ? 0 : 1;
        posX = mc.thePlayer.posX;
        posY = mc.thePlayer.posY;
        posZ = mc.thePlayer.posZ;

        ticks = 0;
        return false;
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (mc.thePlayer == null) {
            toggle();
        }
    }

    @EventTarget
    public void onEventRenderEntity(EventRenderEntity e) {
        if (e.getEntity() == localPlayer) {
            e.setCancelled(false);
        }
    }

    @EventTarget
    public void onRenderEntity(EventRendererLivingEntity e) {
        if (e.getEntity() == localPlayer && e.getType() == EventType.PRE) {
            if (alpha.getCurrentValue() < 0.95) {
                e.setShouldInvisible(true);
                e.setAlpha(alpha.getCurrentValue());
                e.setHideLayer(true);
            } else if (alpha.getCurrentValue() < 0.05) {
                e.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        setEnabled(false);
    }

    @Override
    public boolean onDisable() {
        releasePackets();
        ReleaseCount = 0;
        if (localPlayer != null) {
            mc.theWorld.removeEntityFromWorld(localPlayer.getEntityId());
            localPlayer = null;
        }
        return false;
    }


    @EventTarget
    public void onRender(EventRender2D e) {
        if (render.getCurrentValue() && rendermode.isCurrentMode("Naven")) {
            if (rendermode.isCurrentMode("DynamicIsland")) {
                return;
            }
            resolution = e.getResolution();
            int x = resolution.getScaledWidth() / 2 - 50;
            int y = resolution.getScaledHeight() / 2 + 15;
            progress.update(true);
            RenderUtils.drawBoundRoundedRect(x, y, 100, 5, 2, 0x80000000);
            RenderUtils.drawBoundRoundedRect(x, y, (float) (100 * Math.max(0.05, (1 - progress.value))), 5, 2, mainColor);
        }
    }
    @EventTarget
    public void onShader(EventShader e) {
        if (render.getCurrentValue() && rendermode.isCurrentMode("Naven")) {
            if (rendermode.isCurrentMode("DynamicIsland")) {
                return;
            }
            if (e.getType() == EventType.SHADOW && resolution != null) {
                int x = resolution.getScaledWidth() / 2 - 50;
                int y = resolution.getScaledHeight() / 2 + 15;

                RenderUtils.drawBoundRoundedRect(x, y, 100, 5, 2, 0xFFFFFFFF);
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            mc.gameSettings.keyBindUseItem.pressed = false;

            if (mc.thePlayer.hurtTime >= 9 && moveOnDamage.getCurrentValue()) {
                for (int i = 0; i < ((int) damageMoveTicks.getCurrentValue()) && !packets.isEmpty(); i++) {
                    release1Tick();
                }
            }


            float currentblinkTicks = 0;
            if (AutoPhase.start){
                currentblinkTicks = 120;
            } else {
                currentblinkTicks = blinkTicks.getCurrentValue();
            }

            if (mode.isCurrentMode("Maximum Time")) {
                long movementCount = packets.stream().filter(p -> p instanceof C03PacketPlayer).count();
                progress.target = 1 - movementCount / currentblinkTicks;

                if (movementCount >= currentblinkTicks) {
                    releasePackets();
                }
            } else {
                while (mc.theWorld.loadedEntityList.stream().anyMatch(entity -> entity instanceof EntityTNTPrimed && mc.thePlayer.getDistanceToEntity(entity) < 10) && !packets.isEmpty()) {
                    release1Tick();
                }

                if (mode.isCurrentMode("Delay Release")) {
                    long movementCount = packets.stream().filter(p -> p instanceof C03PacketPlayer).count();

                    if (movementCount >= currentblinkTicks) {
                        if (ReleaseCount < 2) {
                            int packetsToSend = 14;
                            for (int i = 0; i < packetsToSend; i++) {
                                if (!packets.isEmpty()) {
                                    release1Tick();
                                }
                            }
                            ReleaseCount++;
                        } else {
                            release1Tick();
                        }

                        if (mc.thePlayer.fallDistance > 8.0f) {
                            ReleaseCount = 0;
                        }
                    }
                }

                while (!packets.isEmpty()) {
                    long movementCount = packets.stream().filter(p -> p instanceof C03PacketPlayer).count();
                    progress.target = (1 - movementCount / currentblinkTicks);
                    if (movementCount < currentblinkTicks) {
                        break;
                    }

                    release1Tick();
                }
            }

            if (slowMove.getCurrentValue() && mc.thePlayer.ticksExisted % ((int) slowMoveTicks.getCurrentValue()) == 0) {
                release1Tick();
            }

        }
    }

    private void release1Tick() {
        float blinkTicks = this.blinkTicks.getCurrentValue();

        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            handleFakePlayerPacket(packet);
            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);

            if (packet instanceof C03PacketPlayer) {
                break;
            }
        }

        long movementCount = packets.stream().filter(p -> p instanceof C03PacketPlayer).count();
        progress.target = (1 - movementCount / blinkTicks);
    }

    private boolean isAiming(EntityPlayer player) {
        boolean isBow = false;
        float pitchDifference = 0.0F;
        float motionFactor = 1.5F;
        float motionSlowdown = 0.99F;

        if (player.getCurrentEquippedItem() != null) {
            Item heldItem = player.getCurrentEquippedItem().getItem();
            float gravity;

            if (heldItem instanceof ItemBow) {
                isBow = true;
                gravity = 0.05F;
                float power = (float) player.getItemInUseDuration() / 20.0F;
                power = (power * power + power * 2.0F) / 3.0F;

                if ((double) power < 0.1D) {
                    return false;
                }

                if (power > 1.0F) {
                    power = 1.0F;
                }

                motionFactor = power * 3.0F;
            } else if (heldItem instanceof ItemFishingRod) {
                gravity = 0.04F;
                motionSlowdown = 0.92F;
            } else {
                if (!(heldItem instanceof ItemSnowball) && !(heldItem instanceof ItemEnderPearl) && !(heldItem instanceof ItemEgg)) {
                    return false;
                }

                gravity = 0.03F;
            }

            float yaw = player.rotationYaw;
            float pitch = player.rotationPitch;

            double posX = player.posX - (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F);
            double posY = player.posY + (double) player.getEyeHeight() - 0.10000000149011612D;
            double posZ = player.posZ - (double) (MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F);

            double motionX = (double) (-MathHelper.sin(yaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
            double motionY = (double) (-MathHelper.sin((pitch + pitchDifference) / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
            double motionZ = (double) (MathHelper.cos(yaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
            float distance = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
            motionX /= distance;
            motionY /= distance;
            motionZ /= distance;
            motionX *= motionFactor;
            motionY *= motionFactor;
            motionZ *= motionFactor;

            MovingObjectPosition landingPosition;
            boolean hasLanded = false;
            boolean hitEntity = false;
            while (!hasLanded && posY > 0.0D) {
                Vec3 posBefore = new Vec3(posX, posY, posZ);
                Vec3 posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                landingPosition = mc.theWorld.rayTraceBlocks(posBefore, posAfter, false, true, false);
                posBefore = new Vec3(posX, posY, posZ);
                posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

                if (landingPosition != null) {
                    hasLanded = true;
                    posAfter = new Vec3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
                }

                AxisAlignedBB boundingBox = new AxisAlignedBB(this.posX - 2,
                        this.posY - 1,
                        this.posZ - 2,
                        this.posX + 2,
                        this.posY + 5,
                        this.posZ + 2);

                MovingObjectPosition possibleEntityLanding = boundingBox.calculateIntercept(posBefore, posAfter);

                if (possibleEntityLanding != null) {
                    hitEntity = true;
                    hasLanded = true;
                }

                posX += motionX;
                posY += motionY;
                posZ += motionZ;

                BlockPos blockPos = new BlockPos(posX, posY, posZ);
                Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                if (block.getMaterial() == Material.water) {
                    motionX *= 0.6D;
                    motionY *= 0.6D;
                    motionZ *= 0.6D;
                } else {
                    motionX *= motionSlowdown;
                    motionY *= motionSlowdown;
                    motionZ *= motionSlowdown;
                }
                motionY -= gravity;
            }

            return hitEntity;
        }

        return false;
    }

    private double getDistanceToLocalPlayer(Entity entity) {
        return entity.getDistance(posX, posY, posZ);
    }

    private void handleFakePlayerPacket(Packet<?> packet) {
        int animation = (int) Math.max(2, slowMove.getCurrentValue() ? slowMoveTicks.getCurrentValue() : 0);

        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            C03PacketPlayer.C04PacketPlayerPosition position = (C03PacketPlayer.C04PacketPlayerPosition) packet;
            localPlayer.setPositionAndRotation2(position.getPositionX(), position.getPositionY(), position.getPositionZ(), localPlayer.rotationYaw, localPlayer.rotationPitch, animation, true);
            localPlayer.onGround = position.isOnGround();

            posX = position.getPositionX();
            posY = position.getPositionY();
            posZ = position.getPositionZ();
        } else if (packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
            C03PacketPlayer.C05PacketPlayerLook look = (C03PacketPlayer.C05PacketPlayerLook) packet;
            localPlayer.setPositionAndRotation2(localPlayer.posX, localPlayer.posY, localPlayer.posZ, look.getYaw(), look.getPitch(), animation, true);
            localPlayer.onGround = look.isOnGround();
            localPlayer.rotationYawHead = look.getYaw();
            localPlayer.rotationYaw = look.getYaw();
            localPlayer.rotationPitch = look.getPitch();
        } else if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer.C06PacketPlayerPosLook posLook = (C03PacketPlayer.C06PacketPlayerPosLook) packet;
            localPlayer.setPositionAndRotation2(posLook.getPositionX(), posLook.getPositionY(), posLook.getPositionZ(), posLook.getYaw(), posLook.getPitch(), animation, true);
            localPlayer.onGround = posLook.isOnGround();

            posX = posLook.getPositionX();
            posY = posLook.getPositionY();
            posZ = posLook.getPositionZ();
            localPlayer.rotationYawHead = posLook.getYaw();
            localPlayer.rotationYaw = posLook.getYaw();
            localPlayer.rotationPitch = posLook.getPitch();
        } else if (packet instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction action = (C0BPacketEntityAction) packet;
            if (action.getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                localPlayer.setSprinting(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                localPlayer.setSprinting(false);
            } else if (action.getAction() == C0BPacketEntityAction.Action.START_SNEAKING) {
                localPlayer.setSneaking(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                localPlayer.setSneaking(false);
            }
        }
    }

    private void releasePackets() {
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            handleFakePlayerPacket(packet);
            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
        }
    }

    @EventTarget(Priority.LOW)
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.SEND) {
            final Packet<?> packet = e.getPacket();

            if (EnumConnectionState.getFromPacket(packet) == EnumConnectionState.LOGIN) {
                return;
            }

            if (EnumConnectionState.getDirection(packet) == EnumPacketDirection.CLIENTBOUND) {
                return;
            }

            if (e.isCancelled()) {
                return;
            }

            if (packet instanceof C01PacketChatMessage) {
                return;
            }

            e.setCancelled(true);

            packets.add(packet);
        } else {
            if (e.getPacket() instanceof S08PacketPlayerPosLook) {
                releasePackets();
            }
        }
    }
}
