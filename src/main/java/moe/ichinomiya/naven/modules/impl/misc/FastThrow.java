package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.init.Items;

@ModuleInfo(name = "FastThrow", description = "Throw eggs and snowballs faster", category = Category.MISC)
public class FastThrow extends Module {
    private final FloatValue delay = ValueBuilder.create(this, "Delay").setDefaultFloatValue(0).setFloatStep(1).setMinFloatValue(0).setMaxFloatValue(10).build().getFloatValue();

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            // check held item is egg or snowball
            if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() == Items.egg || mc.thePlayer.getHeldItem().getItem() == Items.snowball || mc.thePlayer.getHeldItem().getItem() == Items.experience_bottle)) {
                if (mc.getRightClickDelayTimer() > delay.getCurrentValue()) {
                    mc.setRightClickDelayTimer((int) delay.getCurrentValue());
                }
            }
        }
    }
}
