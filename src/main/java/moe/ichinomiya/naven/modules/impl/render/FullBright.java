package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;

@ModuleInfo(name = "FullBright", description = "Make your world brighter.", category = Category.RENDER)
public class FullBright extends Module {
    public FloatValue brightness = ValueBuilder.create(this, "Brightness").setDefaultFloatValue(1).setFloatStep(0.1f).setMinFloatValue(0f).setMaxFloatValue(1f).build().getFloatValue();
}
