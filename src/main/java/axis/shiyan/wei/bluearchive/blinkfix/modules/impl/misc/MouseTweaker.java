package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.mousetweaks.MouseTweakerMain;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;

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
