package axis.shiyan.wei.bluearchive.blinkfix.utils;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat.Aura;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat.AutoThrow;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat.RageBot;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat.ThrowableAimAssist;
import lombok.extern.log4j.Log4j2;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.ChestAura;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Scaffold;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Stuck;
import org.lwjgl.util.vector.Vector2f;

@Log4j2
public class RotationManager extends Module {
    public static Vector2f rotations, lastRotations;
    private boolean active = false;

    public static void setRotations(final Vector2f rotations) {
        RotationManager.rotations = rotations;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        if (e.getType() == EventType.JOIN_GAME) {
            rotations = lastRotations = null;
        }
    }

    @EventTarget(Priority.LOWEST)
    public void updateGlobalYaw(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            Aura aura = (Aura) BlinkFix.getInstance().getModuleManager().getModule(Aura.class);
            Scaffold scaffold = (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
            RageBot rageBot = (RageBot) BlinkFix.getInstance().getModuleManager().getModule(RageBot.class);
            ThrowableAimAssist throwableAimAssist = (ThrowableAimAssist) BlinkFix.getInstance().getModuleManager().getModule(ThrowableAimAssist.class);
            ChestAura chestAura = (ChestAura) BlinkFix.getInstance().getModuleManager().getModule(ChestAura.class);
            AutoThrow autoThrow = (AutoThrow) BlinkFix.getInstance().getModuleManager().getModule(AutoThrow.class);

            active = true;
            if (scaffold.isEnabled() && scaffold.rots != null) {
                Aura.disableHelper.reset();
                RotationManager.setRotations(new Vector2f(scaffold.rots[0], scaffold.rots[1]));
            }else
                if (aura.isEnabled() && Aura.aimingTarget != null) {
                RotationManager.setRotations(new Vector2f(aura.yaw, aura.pitch));
            } else if (autoThrow.isEnabled() && autoThrow.rotation != null) {
                RotationManager.setRotations(autoThrow.rotation);
                autoThrow.rotationSet = 2;
            } else if (chestAura.rotation != null) {
                RotationManager.setRotations(chestAura.rotation);
            } else if (throwableAimAssist.isEnabled() && throwableAimAssist.rotation != null) {
                RotationManager.setRotations(throwableAimAssist.rotation);
            } else if (rageBot.isEnabled() && RageBot.target != null && RageBot.aiming) {
                RotationManager.setRotations(new Vector2f(RageBot.yaw, RageBot.pitch));
            } else {
                active = false;
            }
        }
    }

    @EventTarget
    public void onRotation(EventRotationAnimation e) {
        if (active && lastRotations != null && rotations != null) {
            e.setYaw(rotations.x, lastRotations.x);
            e.setPitch(rotations.y, lastRotations.y);
        }
    }

    @EventTarget
    public void onPre(EventMotion e) {
        if (e.getType() == EventType.PRE && !BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()) {
            if (rotations == null || lastRotations == null) {
                rotations = lastRotations = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            }

            float yaw = rotations.x;
            float pitch = rotations.y;

            if (!Float.isNaN(yaw) && !Float.isNaN(pitch) && active) {
                e.setYaw(yaw);
                e.setPitch(pitch);
            }

            lastRotations = new Vector2f(e.getYaw(), e.getPitch());
        }
    }

    @EventTarget
    public void onMove(EventMoveInput event) {
        if (active && rotations != null) {
            float yaw = rotations.x;

            MoveUtils.fixMovement(event, yaw);
        }
    }

    @EventTarget
    public void onMove(EventLook event) {
        if (active && rotations != null) {
            event.setRotation(rotations);
        }
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        if (active && rotations != null) {
            event.setYaw(rotations.x);
        }
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (active && rotations != null) {
            event.setYaw(rotations.x);
        }
    }
}
