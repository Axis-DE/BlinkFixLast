package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;

import static axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority.HIGHEST;

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
    public void onDisable() {
        mc.gameSettings.keyBindSprint.pressed = false;
    }
}
