package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventKey;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMouseClick;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventTick;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.clickgui.ClientClickGUI;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;

@ModuleInfo(name = "PreferWeapon", description = "Prefer a specific weapon", category = Category.MISC)
public class PreferWeapon extends Module {
    public ModeValue weapon = ValueBuilder.create(this, "Current Weapon")
            .setDefaultModeIndex(0)
            .setModes("Best Sword", "God Axe", "KB Ball")
            .build()
            .getModeValue();

    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClientClickGUI) {
            super.toggle();
        }
    }

    @EventTarget
    public void onKey(EventKey e) {
        if (e.getKey() == getKey() && e.isState()) {
            nextWeapon();
        }
    }


    @EventTarget
    public void onMouse(EventMouseClick e) {
        if (e.getKey() == -getKey() && !e.isState()) {
            nextWeapon();
        }
    }

    private void nextWeapon() {
        int index = weapon.getCurrentValue() + 1;

        if (index >= weapon.getValues().length) {
            index = 0;
        }

        weapon.setCurrentValue(index);
    }


    @EventTarget
    public void onTick(EventTick e) {
        setSuffix(weapon.getCurrentMode());
    }
}
