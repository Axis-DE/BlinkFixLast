package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.mousetweaks.MouseTweakerMain;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.ModeValue;

@ModuleInfo(name = "MouseTweaker", description = "Tweaks your mouse!", category = Category.MISC)
public class MouseTweaker extends Module {
    public BooleanValue rmbTweak = ValueBuilder.create(this, "RMB Tweak").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue lmbTweakWithItem = ValueBuilder.create(this, "LMB Tweak With Item").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue lmbTweakWithoutItem = ValueBuilder.create(this, "LMB Tweak Without Item").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue wheelTweak = ValueBuilder.create(this, "Wheel Tweak").setDefaultBooleanValue(true).build().getBooleanValue();
    public ModeValue wheelSearchOrder = ValueBuilder.create(this, "Sort Mode").setDefaultModeIndex(0).setModes("First to last", "Last to first").build().getModeValue();

    public static MouseTweaker getModule() {
        return (MouseTweaker) BlinkFix.getInstance().getModuleManager().getModule(MouseTweaker.class);
    }

    @EventTarget
    public void onRender(EventMotion e) {
        if (e.getType() == EventType.PRE && mc.thePlayer != null) {
            MouseTweakerMain.onUpdateInGame();
        }
    }
}
