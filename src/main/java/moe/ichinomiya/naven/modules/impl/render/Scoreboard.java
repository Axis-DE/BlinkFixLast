package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;

@ModuleInfo(name = "Scoreboard", description = "Renders scoreboard", category = Category.RENDER)
public class Scoreboard extends Module {
    public BooleanValue modernStyle = ValueBuilder.create(this, "Modern Style").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue noRenderPoint = ValueBuilder.create(this, "No Render Points").setDefaultBooleanValue(true).build().getBooleanValue();
    public FloatValue down = ValueBuilder.create(this, "Down").setDefaultFloatValue(0).setFloatStep(1).setMinFloatValue(0).setMaxFloatValue(500).build().getFloatValue();
}
