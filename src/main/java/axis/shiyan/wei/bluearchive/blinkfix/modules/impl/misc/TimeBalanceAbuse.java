package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Blink;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Stuck;
import axis.shiyan.wei.bluearchive.blinkfix.ui.clickgui.ClientClickGUI;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;

@ModuleInfo(name = "TimeBalanceAbuse", description = "Abuse time balance", category = Category.MISC)
public class TimeBalanceAbuse extends Module {
    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Retain", "Idle", "Release").build().getModeValue();

    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClientClickGUI) {
            super.toggle();
        }
    }

    @EventTarget
    public void onKey(EventKey e) {
        if (e.getKey() == getKey() && e.isState()) {
            nextState();
        }
    }


    @EventTarget
    public void onMouse(EventMouseClick e) {
        if (e.getKey() == -getKey() && !e.isState()) {
            nextState();
        }
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        mode.setCurrentValue(2);
    }

    private void nextState() {
        int index = mode.getCurrentValue() + 1;

        if (index >= mode.getValues().length) {
            index = 0;
        }

        mode.setCurrentValue(index);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.timer = 1;
    }

    @EventTarget
    public void onTick(EventRunTicks e) {
        setSuffix(mode.getCurrentMode());

        if (mc.thePlayer != null) {
            Disabler disabler = (Disabler) BlinkFix.getInstance().getModuleManager().getModule(Disabler.class);
            Blink blink = (Blink) BlinkFix.getInstance().getModuleManager().getModule(Blink.class);

            if (mode.isCurrentMode("Retain") && disabler.delayedServerPackets.size() < 190 && !blink.isEnabled()) {
                mc.thePlayer.timer = 0;
            } else if (mode.isCurrentMode("Idle") || disabler.delayedServerPackets.size() >= 190) {
                mc.thePlayer.timer = 1;
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && mode.isCurrentMode("Release") && !BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()) {
            Disabler disabler = (Disabler) BlinkFix.getInstance().getModuleManager().getModule(Disabler.class);
            if (disabler.delayedServerPackets.size() > 1) {
                mc.thePlayer.timer = 3;
            } else {
                mc.thePlayer.timer = 1;
            }
        }
    }
}

//@ModuleInfo(name = "TimeBalanceAbuse", description = "Abuse time balance", category = Category.MISC)
//public class TimeBalanceAbuse extends Module {
//    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Retain", "Idle", "Release").build().getModeValue();
//
//    @Override
//    public void toggle() {
//        if (mc.currentScreen instanceof ClientClickGUI) {
//            super.toggle();
//        }
//    }
//
//    @EventTarget
//    public void onKey(EventKey e) {
//        if (e.getKey() == getKey() && e.isState()) {
//            nextState();
//        }
//    }
//
//
//    @EventTarget
//    public void onMouse(EventMouseClick e) {
//        if (e.getKey() == -getKey() && !e.isState()) {
//            nextState();
//        }
//    }
//
//    @EventTarget
//    public void onRespawn(EventRespawn e) {
//        mode.setCurrentValue(2);
//    }
//
//    private void nextState() {
//        int index = mode.getCurrentValue() + 1;
//
//        if (index >= mode.getValues().length) {
//            index = 0;
//        }
//
//        mode.setCurrentValue(index);
//    }
//
//    @Override
//    public void onDisable() {
//        mc.thePlayer.timer = 1;
//    }
//
//    @EventTarget
//    public void onTick(EventRunTicks e) {
//        setSuffix(mode.getCurrentMode());
//
//        if (mc.thePlayer != null) {
//            Disabler disabler = (Disabler) BlinkFix.getInstance().getModuleManager().getModule(Disabler.class);
//            Blink blink = (Blink) BlinkFix.getInstance().getModuleManager().getModule(Blink.class);
//
//            if (mode.isCurrentMode("Retain") && disabler.delayedServerPackets.size() < 190 && !blink.isEnabled()) {
//                mc.thePlayer.timer = 0;
//            } else if (mode.isCurrentMode("Idle") || disabler.delayedServerPackets.size() >= 190) {
//                mc.thePlayer.timer = 1;
//            }
//        }
//    }
//
//    @EventTarget
//    public void onMotion(EventMotion e) {
//        if (e.getType() == EventType.PRE && mode.isCurrentMode("Release") && !BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()) {
//            Disabler disabler = (Disabler) BlinkFix.getInstance().getModuleManager().getModule(Disabler.class);
//             if (disabler.delayedServerPackets.size() > 1) {
//                mc.thePlayer.timer = 3;
//            } else {
//                mc.thePlayer.timer = 1;
//            }
//        }
//    }
//}
