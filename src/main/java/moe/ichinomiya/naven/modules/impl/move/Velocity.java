package moe.ichinomiya.naven.modules.impl.move;

import de.florianmichael.viamcp.fixes.AttackOrder;
//import dev.yalan.live.LiveClient;
import lombok.Getter;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.combat.Aura;
import moe.ichinomiya.naven.modules.impl.combat.AutoGapple;
import moe.ichinomiya.naven.modules.impl.misc.Disabler;
import moe.ichinomiya.naven.ui.cooldown.CooldownBar;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import moe.ichinomiya.naven.BlinkFix;

import java.util.Optional;

import static moe.ichinomiya.naven.events.api.types.Priority.*;

@ModuleInfo(name = "Velocity", description = "Reduces knockback.", category = Category.MOVEMENT)
public class Velocity extends Module {
    private final TimeHelper disableHelper = new TimeHelper(), velocityTimer = new TimeHelper();
    public final ModeValue mode = ValueBuilder.create(this,"Mode").setModes("Grim","Prediction").setDefaultModeIndex(0).build().getModeValue();
    public final BooleanValue debugMessage = ValueBuilder.create(this, "Verbose Output").setDefaultBooleanValue(false).build().getBooleanValue();
    public final BooleanValue onlySprint = ValueBuilder.create(this, "Sprint Only").setDefaultBooleanValue(false).build().getBooleanValue();
    public static boolean toggle = false;
    boolean needJump = false;
    public static int direction = 1;
    @Getter
    private boolean isFallDamage;
    CooldownBar bar;

    private Optional<Entity> findEntity() {
        return mc.theWorld.loadedEntityList.stream()
                .filter(livingBase -> mc.thePlayer.getEntityId() != livingBase.getEntityId() && livingBase instanceof EntityLivingBase)
                .filter(livingBase -> !livingBase.isDead && ((EntityLivingBase) livingBase).getHealth() > 0 && !((EntityLivingBase) livingBase).isPlayerSleeping())
                .filter(livingBase -> !(livingBase instanceof EntityOtherPlayerMP) || !((EntityOtherPlayerMP) livingBase).isFakePlayer())
                .filter(livingBase -> !(livingBase instanceof EntityOtherPlayerMP) || ((EntityOtherPlayerMP) livingBase).getPlayerDeadTimer().delay(1000))
                .filter(livingBase -> RotationUtils.getMinDistance(livingBase, RotationManager.lastRotations) < 3)
                .findAny();
    }

    public static S12PacketEntityVelocity velocityPacket;

    public static void sendLookPacket() {
        mc.skipTicks += 1;
        direction *= -1;
        float playerYaw = RotationManager.rotations.x + 0.0001f * direction;
        mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C05PacketPlayerLook(playerYaw, RotationManager.rotations.y, mc.thePlayer.onGround));
    }
    public static boolean isS12;

    @EventTarget(HIGHEST)
    public void onPacket(EventPacket e) {
        if (mc.thePlayer != null && e.getType() == EventType.RECEIVE && !e.isCancelled()) {
            if (e.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) e.getPacket();
                if (mode.isCurrentMode("Grim")) {
                    if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                        double x = packet.getMotionX() / 8000D;
                        double z = packet.getMotionZ() / 8000D;
                        double speed = Math.sqrt(x * x + z * z);

                        if (mc.thePlayer.isInWeb || mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isOnLadder()) {
                            if (debugMessage.getCurrentValue()) {
                                ChatUtils.addChatMessage("Ignore: Player is in Web\\Water\\Lava\\Ladder!");
                            }
                        } else if (!disableHelper.delay(1000)) {
                            if (debugMessage.getCurrentValue()) {
                                ChatUtils.addChatMessage("Ignore: Player just flagged!");
                            }
                        } else if (speed < 0.1) {
                            if (debugMessage.getCurrentValue()) {
                                ChatUtils.addChatMessage("Ignore: Speed is too low!");
                            }
                        } else if (onlySprint.getCurrentValue() && !mc.thePlayer.serverSprintState) {
                            if (debugMessage.getCurrentValue()) {
                                ChatUtils.addChatMessage("Ignore: You are not sprinting!");
                            }
                        } else if (Disabler.disabled) {
                            Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);

                            if (aura.isEnabled() && Aura.target != null) {
                                velocityPacket = packet;
                                e.setCancelled(true);
                                return;
                            }
                            Optional<Entity> any = findEntity();

                            if (any.isPresent()) {
                                velocityTimer.reset();
                                Entity entity = any.get();
                                e.setCancelled(true);

                                boolean needSprint = !mc.thePlayer.serverSprintState;

                                if (needSprint) {
                                    packet.setToggleSprint(true);
                                    mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));

                                    int currentValue = (int) 0;
                                    if (!BlinkFix.getInstance().getModuleManager().getModule(AutoGapple.class).isEnabled()) {
                                        for (int i = 0; i < currentValue; i++) {
                                            sendLookPacket();
                                        }
                                    }
                                }

                                for (int i = 0; i < 8; i++) {
                                    AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                                }

                                x *= Math.pow(0.6, 5);
                                z *= Math.pow(0.6, 5);

                                if (needSprint) {
                                    toggle = true;
                                    mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                                }

                                packet.setMotionX((int) (x * 8000));
                                packet.setMotionZ((int) (z * 8000));
                                packet.setModified(true);

                                velocityPacket = packet;
                            }
                        }
                    }
                }
                if (mode.isCurrentMode("Prediction")){
                    double velocityX = packet.getMotionX() / 8000.0;
                    double velocityY = packet.getMotionY() / 8000.0;
                    double velocityZ = packet.getMotionZ() / 8000.0;
                    isFallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY < 0;
                }
            }

            if (e.getPacket() instanceof S08PacketPlayerPosLook && !velocityTimer.delay(500)) {
                if (bar == null || bar.isExpired()) {
                    bar = new CooldownBar(1000, "Velocity Temporarily Disabled");
                    BlinkFix.getInstance().getCooldownBarManager().addBar(bar);
                } else {
                    bar.setCreateTime(System.currentTimeMillis());
                }
                disableHelper.reset();
            }
        }
    }
    @EventTarget
    public void onMotion(EventMotion e){
        if (mode.isCurrentMode("Prediction")){
            needJump = mc.thePlayer.hurtTime >= 7;
            if (needJump && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.isInWeb && !mc.thePlayer.isInLava() && !mc.thePlayer.isInWater() && !mc.thePlayer.isBurning()) {
                mc.thePlayer.jump();
            }
        }
    }

}
