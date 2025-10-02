package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.utils.ChatUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RotationManager;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RotationUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import de.florianmichael.viamcp.fixes.AttackOrder;
import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.events.EventLivingUpdate;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Vector2f;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat.Aura;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.Disabler;
import axis.shiyan.wei.bluearchive.blinkfix.ui.cooldown.CooldownBar;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import static axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority.*;

@ModuleInfo(name = "Velocity", description = "Reduces knockback.", category = Category.MOVEMENT)
public class Velocity extends Module {
    private final TimeHelper disableHelper = new TimeHelper(), velocityTimer = new TimeHelper();
    public ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("Grim", "Prediction","Intave","Grim 1.9+","Vanilla")
            .build()
            .getModeValue();
    FloatValue motionX = ValueBuilder.create(this, "MotionX").setDefaultFloatValue(20).setFloatStep(1f).setMinFloatValue(0f).setMaxFloatValue(100f).setVisibility(() -> mode.isCurrentMode("Vanilla")).build().getFloatValue();
    FloatValue motionY = ValueBuilder.create(this, "MotionY").setDefaultFloatValue(20).setFloatStep(1f).setMinFloatValue(0f).setMaxFloatValue(100f).setVisibility(() -> mode.isCurrentMode("Vanilla")).build().getFloatValue();
    public final BooleanValue debugMessage = ValueBuilder.create(this, "Verbose Output").setDefaultBooleanValue(false).build().getBooleanValue();
    public final BooleanValue onlySprint = ValueBuilder.create(this, "Sprint Only").setDefaultBooleanValue(false).build().getBooleanValue();
    public final BooleanValue d = ValueBuilder.create(this, "Follow Direction").setDefaultBooleanValue(false).setVisibility(() -> mode.isCurrentMode("Prediction")).build().getBooleanValue();
    public static boolean toggle = false;
    boolean needJump = false;
    private boolean isKnockbacked;
    private int offGroundTicks = 0;
    public float knockbackYaw = 0f;
    public int knockbackTicks = 0;
    public boolean shouldRotate = false;
    public static int direction = 1;
    @Getter
    private boolean isFallDamage;
    CooldownBar bar;
    public static boolean jumpreset;
    private boolean veloPacket = false;
    @Getter
    private int hitsCount = 0;
    @Getter
    private int ticksCount = 0;
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
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
    @Getter
    public static int ticksSinceVelocity = Integer.MAX_VALUE;
    @EventTarget(HIGHEST)
    public void onPacket(EventPacket e) {
        if (mc.thePlayer != null && e.getType() == EventType.RECEIVE && !e.isCancelled()) {
            if (e.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) e.getPacket();
                if (mode.isCurrentMode("Grim")) {
                    jumpreset = false;
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
                if (mode.isCurrentMode("Vanilla")) {
                    packet.setMotionX((int) ((double) packet.getMotionX() * this.motionX.getCurrentValue() / 100.0));
                    packet.setMotionY((int) ((double) packet.getMotionY() * this.motionY.getCurrentValue() / 100.0));
                    packet.setMotionZ((int) ((double) packet.getMotionZ() * this.motionX.getCurrentValue() / 100.0));
                }
            }


            if (e.getPacket() instanceof S08PacketPlayerPosLook && !velocityTimer.delay(500) && mode.isCurrentMode("Grim")) {
                if (bar == null || bar.isExpired()) {
                    bar = new CooldownBar(1000, "Velocity Temporarily Disabled");
                    BlinkFix.getInstance().getCooldownBarManager().addBar(bar);
                } else {
                    bar.setCreateTime(System.currentTimeMillis());
                }
                disableHelper.reset();
            }
            if (e.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) e.getPacket();

                if (mode.isCurrentMode("Prediction") && packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    veloPacket = true;
                    double motionX = packet.getMotionX() / 8000D;
                    double motionZ = packet.getMotionZ() / 8000D;

                    knockbackYaw = (float) Math.toDegrees(Math.atan2(motionZ, motionX)) - 90.0f + 180.0f;
                    knockbackYaw = MathHelper.wrapAngleTo180_float(knockbackYaw);

                    shouldRotate = true;
                    knockbackTicks = 0;


                }
            }
            if (mode.isCurrentMode("Grim 1.9+")){
                Packet packet = e.getPacket();
                if (packet instanceof S12PacketEntityVelocity) {
                    if (mc.thePlayer == null) {
                        return;
                    }
                    S12PacketEntityVelocity motionPacket = (S12PacketEntityVelocity) packet;
                    if (motionPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                        ticksSinceVelocity = 0;
                        if (!mc.thePlayer.onGround) {
                            this.packets.add(packet);
                            this.isKnockbacked = true;
                            e.setCancelled(true);
                        }
                    }
                }
                if (packet instanceof S32PacketConfirmTransaction && this.isKnockbacked) {
                    this.packets.add(packet);
                    e.setCancelled(true);
                }
            }
        }
    }
    @EventTarget
    public void onTick(EventRunTicks event) {
        if (mc.thePlayer == null) return;
        if (mode.isCurrentMode("Grim 1.9+")) {
            this.offGroundTicks = mc.thePlayer.onGround ? 0 : this.offGroundTicks + 1;
            if (ticksSinceVelocity < Integer.MAX_VALUE) {
                ++ticksSinceVelocity;
            }
        }
    }
    @EventTarget
    public void onLivingUpdate(EventTick event) {
        if (mode.isCurrentMode("Grim 1.9+")) {
            if (event.getType() == EventType.POST || mc.thePlayer == null) {
                return;
            }
            if ((mc.thePlayer.onGround && this.isKnockbacked) ||
                    (this.isKnockbacked && this.offGroundTicks > 10)) {

                this.isKnockbacked = false;
                Iterator iterator = this.packets.iterator();
                while (iterator.hasNext()) {
                    Packet packet = (Packet) iterator.next();
                    packet.processPacket(Velocity.mc.getNetHandler());
                    iterator.remove();
                }
            }
        }
    }

    @EventTarget
    public void onPre(EventMotion event) {
        if (mc.thePlayer == null) return;
        if (mode.isCurrentMode("Grim 1.9+")) {
            if (event.getType() == EventType.POST) {
                return;
            }
            if (getTicksSinceVelocity() <= 14 && Velocity.mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
        }
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    @EventTarget
    public void onStrafe(EventStrafe event) {
        if (mode.isCurrentMode("Prediction")) {
            boolean shouldJump = veloPacket;

            if (shouldJump && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !checks()) {
                mc.thePlayer.jump();
                veloPacket = false;
                hitsCount = 0;
                ticksCount = 0;
            }
        }
    }

    @EventTarget
    public void onUpdate(EventLivingUpdate e){
        if (mode.isCurrentMode("Intave")) {
            if (mc.objectMouseOver == null)
                return;

            if (mc.thePlayer.hurtTime <= 6 && mc.thePlayer.isSwingInProgress && Aura.target != null && mc.thePlayer.hurtTime > 0) {
                mc.thePlayer.motionX = -Math.sin(Math.toRadians((mc.thePlayer.rotationYaw))) * 0.02f;
                mc.thePlayer.motionZ = Math.cos(Math.toRadians((mc.thePlayer.rotationYaw))) * 0.02f;
            }
        }
    }
    public float yaw;
    public float pitch;
    RotationUtils.Data lastRotationData, rotationData;
    @EventTarget
    public void onMotion(EventMotion e) {
        setSuffix(mode.getCurrentMode());
        if (mode.isCurrentMode("Prediction")) {
            jumpreset = true;

            if (shouldRotate && e.getType() == EventType.PRE && d.getCurrentValue() ) {
                knockbackTicks++;

                if (knockbackTicks <= 5) {
                    RotationManager.setRotations(new Vector2f(knockbackYaw, mc.thePlayer.rotationPitch));

                } else if (knockbackTicks == 6) {
                    shouldRotate = false;
                }
            }
        }
    }
    private boolean checks() {
        return mc.thePlayer.isInWeb || mc.thePlayer.isInLava() || mc.thePlayer.isBurning() || mc.thePlayer.isInWater();
    }
}
