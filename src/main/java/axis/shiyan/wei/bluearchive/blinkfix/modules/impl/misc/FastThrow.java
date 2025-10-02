package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
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
