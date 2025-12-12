package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;

@ModuleInfo(name = "Fly", description = "Fly me to the moon", category = Category.MOVEMENT)
public class FlyMeToTheMoon extends Module {
    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Vertical").build().getModeValue();
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
    public boolean onDisable() {
        super.onDisable();
        return false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && ++ticks > 1) {
            e.setX(e.getX() + 1337);
            e.setZ(e.getZ() + 1337);
        }
    }
}
