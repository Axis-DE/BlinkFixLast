package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRespawn;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;

@ModuleInfo(name = "FlyMeToTheMoon", description = "Fly me to the moon", category = Category.MOVEMENT)
public class FlyMeToTheMoon extends Module {
    public int ticks = 0;

    @Override
    public boolean onEnable() {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }
        ticks = 0;
        super.onEnable();
        return false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        toggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && ++ticks > 1) {
            e.setX(e.getX() + 1337);
            e.setZ(e.getZ() + 1337);
        }
    }
}
