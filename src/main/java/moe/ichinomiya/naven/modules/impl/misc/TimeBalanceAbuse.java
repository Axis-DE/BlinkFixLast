package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.move.Blink;
import moe.ichinomiya.naven.modules.impl.move.Stuck;
import moe.ichinomiya.naven.ui.clickgui.ClientClickGUI;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;

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
    public boolean onDisable() {
        mc.thePlayer.timer = 1;
        return false;
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
