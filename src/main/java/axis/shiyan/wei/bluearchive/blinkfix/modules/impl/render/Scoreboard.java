package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;

@ModuleInfo(name = "Scoreboard", description = "Renders scoreboard", category = Category.RENDER)
public class Scoreboard extends Module {
    public BooleanValue modernStyle = ValueBuilder.create(this, "Modern Style").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue noRenderPoint = ValueBuilder.create(this, "No Render Points").setDefaultBooleanValue(true).build().getBooleanValue();
    public FloatValue down = ValueBuilder.create(this, "Down").setDefaultFloatValue(0).setFloatStep(1).setMinFloatValue(0).setMaxFloatValue(500).build().getFloatValue();
}
