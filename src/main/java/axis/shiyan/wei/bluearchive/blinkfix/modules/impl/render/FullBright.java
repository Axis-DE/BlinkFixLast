package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;

@ModuleInfo(name = "FullBright", description = "Make your world brighter.", category = Category.RENDER)
public class FullBright extends Module {
    public FloatValue brightness = ValueBuilder.create(this, "Brightness").setDefaultFloatValue(1).setFloatStep(0.1f).setMinFloatValue(0f).setMaxFloatValue(1f).build().getFloatValue();
}
