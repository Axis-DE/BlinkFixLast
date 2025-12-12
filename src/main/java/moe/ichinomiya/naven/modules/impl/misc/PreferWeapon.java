package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventKey;
import moe.ichinomiya.naven.events.impl.EventMouseClick;
import moe.ichinomiya.naven.events.impl.EventTick;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.clickgui.ClientClickGUI;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;

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
