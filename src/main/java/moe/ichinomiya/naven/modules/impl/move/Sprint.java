package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;

import static moe.ichinomiya.naven.events.api.types.Priority.HIGHEST;

@ModuleInfo(name = "Sprint", description = "Automatically sprints", category = Category.MOVEMENT)
public class Sprint extends Module {
    @EventTarget(HIGHEST)
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if ((mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0)
                    && mc.thePlayer.getFoodStats().getFoodLevel() > 6 && !mc.thePlayer.isSneaking()) {
                mc.gameSettings.keyBindSprint.pressed = true;
            }
        }
    }

    @Override
    public boolean onDisable() {
        mc.gameSettings.keyBindSprint.pressed = false;
        return false;
    }
}
