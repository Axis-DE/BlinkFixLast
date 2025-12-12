package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;

@ModuleInfo(name = "NoSmoothCamera", description = "Disable Smooth Camera!", category = Category.MISC)
public class NoSmoothCamera extends Module {
    public final FloatValue sensitivity = ValueBuilder.create(this, "Sensitivity").setDefaultFloatValue(0.25f).setFloatStep(0.01f).setMinFloatValue(0.1f).setMaxFloatValue(1f).build().getFloatValue();
}
