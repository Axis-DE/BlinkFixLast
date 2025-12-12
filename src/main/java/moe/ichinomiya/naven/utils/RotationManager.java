package moe.ichinomiya.naven.utils;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.impl.combat.Aura;
import moe.ichinomiya.naven.modules.impl.combat.AutoThrow;
import moe.ichinomiya.naven.modules.impl.combat.RageBot;
import moe.ichinomiya.naven.modules.impl.combat.ThrowableAimAssist;
import lombok.extern.log4j.Log4j2;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.impl.misc.ChestAura;
import moe.ichinomiya.naven.modules.impl.move.Scaffold;
import moe.ichinomiya.naven.modules.impl.move.Stuck;
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
