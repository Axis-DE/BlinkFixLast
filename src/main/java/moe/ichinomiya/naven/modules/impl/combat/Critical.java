package moe.ichinomiya.naven.modules.impl.combat;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;

@ModuleInfo(name = "Critical", description = "Enable each of your attacks to deal a critical hit.", category = Category.COMBAT)
public class Critical extends Module {
    public ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("Legit", "Stuck")
            .build()
            .getModeValue();

    private boolean shouldJump = false;
    private boolean isAttacking = false;
    public static boolean velocityed = false;
    private boolean attacking = false;

    @Override
    public boolean onEnable() {
        setSuffix(mode.getCurrentMode());
        resetState();
        return false;
    }

    @Override
    public boolean onDisable() {
        resetState();
        return false;
    }

    private void resetState() {
        shouldJump = false;
        isAttacking = false;
        velocityed = false;
        attacking = false;
    }

    @EventTarget
    public void onAttack(EventAttack e) {
        if (mode.isCurrentMode("Stuck")) {
            isAttacking = true;
            if (mc.thePlayer.onGround) {
                shouldJump = true;
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        setSuffix(mode.getCurrentMode());
        if (e.getType() == EventType.PRE) {
            if (mode.isCurrentMode("Legit")) {
                if (Aura.target != null && attacking) {
                    if (mc.thePlayer.fallDistance > 0 || getOffGroundTicks() > 3) {
                        e.setOnGround(false);
                    }
                } else {
                    attacking = false;
                }
            }
            else if (mode.isCurrentMode("Stuck")) {
                if (!isAuraAttacking()) {
                    resetState();
                    return;
                }
                if (velocityed && mc.thePlayer.positionUpdateTicks >= 1) {
                    velocityed = false;
                }
                if (mc.thePlayer.onGround && shouldJump) {
                    mc.thePlayer.jump();
                    shouldJump = false;
                }
                if (!isAuraAttacking()) {
                    isAttacking = false;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (mode.isCurrentMode("Legit")) {
            final Packet<?> packet = e.getPacket();

            if (packet instanceof C02PacketUseEntity) {
                final C02PacketUseEntity wrapped = (C02PacketUseEntity) packet;

                if (wrapped.getAction() == C02PacketUseEntity.Action.ATTACK) {
                    attacking = true;
                }
            }
        }
    }

    @EventTarget
    private void onMoveInput(EventMoveInput e) {
        if (mode.isCurrentMode("Stuck")) {
            if (!isAttacking || !isAuraAttacking()) {
                return;
            }
            if (!mc.thePlayer.onGround && shouldStopForCritical()) {
                e.setForward(0.0F);
                e.setStrafe(0.0F);
                e.setJump(false);
            }
            if (shouldJump && mc.thePlayer.onGround) {
                e.setJump(true);
            }
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (mc.thePlayer == null) return;
    }
    private int getOffGroundTicks() {
        if (mc.thePlayer.onGround) {
            return 0;
        }
        return mc.thePlayer.ticksExisted % 20;
    }

    public boolean isAuraAttacking() {
        if (Aura.target == null) {
            return false;
        }
        double distance = mc.thePlayer.getDistanceToEntity(Aura.target);
        float attackRange = getAuraAttackRange();

        return distance <= attackRange;
    }
    private float getAuraAttackRange() {
        Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule("KillAura");
        if (aura != null && aura.isEnabled()) {
            return aura.range.getCurrentValue();
        }
        return 3.0f;
    }

    public static boolean shouldCritical() {
        Critical criticalModule = (Critical) BlinkFix.getInstance().getModuleManager().getModule("Critical");
        if (criticalModule == null || !criticalModule.isEnabled()) return false;

        if (criticalModule.mode.isCurrentMode("Stuck")) {
            if (!criticalModule.isAuraAttacking()) {
                return false;
            }
            if (!Minecraft.getMinecraft().thePlayer.onGround) {
                return Minecraft.getMinecraft().thePlayer.positionUpdateTicks < 1;
            }
        }
        return false;
    }

    public static boolean shouldJumpForCritical() {
        Critical instance = (Critical) BlinkFix.getInstance().getModuleManager().getModule("Critical");
        if (instance == null || !instance.isEnabled()) return false;

        if (instance.mode.isCurrentMode("Stuck")) {
            return instance.isAttacking && instance.shouldJump && instance.isAuraAttacking();
        }
        return false;
    }

    public static boolean shouldStopForCritical() {
        Critical instance = (Critical) BlinkFix.getInstance().getModuleManager().getModule("Critical");
        if (instance == null || !instance.isEnabled()) return false;

        if (instance.mode.isCurrentMode("Stuck")) {
            if (!instance.isAuraAttacking()) {
                return false;
            }
            if (!Minecraft.getMinecraft().thePlayer.onGround) {
                return Minecraft.getMinecraft().thePlayer.positionUpdateTicks < 1 && !velocityed;
            }
        }
        return false;
    }

    public static void onVelocity() {
        Critical criticalModule = (Critical) BlinkFix.getInstance().getModuleManager().getModule("Critical");
        if (criticalModule != null && criticalModule.isEnabled() && criticalModule.mode.isCurrentMode("Stuck")) {
            criticalModule.velocityed = true;
        }
    }
}