package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;

@ModuleInfo(name = "NoSmoothCamera", description = "Disable Smooth Camera!", category = Category.MISC)
public class NoSmoothCamera extends Module {
    public final FloatValue sensitivity = ValueBuilder.create(this, "Sensitivity").setDefaultFloatValue(0.25f).setFloatStep(0.01f).setMinFloatValue(0.1f).setMaxFloatValue(1f).build().getFloatValue();
}
