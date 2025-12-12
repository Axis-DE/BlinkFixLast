// com.Plana.Naven.modules.impl.render.DynamicIsland.java
package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.ChatUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;


@ModuleInfo(name = "DynamicIsland", description = "Dynamic Island Style Information Display", category = Category.RENDER)
public class DynamicIsland extends Module {
/*
    public BooleanValue showFPS = ValueBuilder.create(this, "Show FPS")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue showTime = ValueBuilder.create(this, "Show Time")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue showClientName = ValueBuilder.create(this, "Show Client Name")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
*/
    public FloatValue width = ValueBuilder.create(this, "Width")
            .setDefaultFloatValue(220)
            .setFloatStep(10)
            .setMinFloatValue(120)
            .setMaxFloatValue(350)
            .build()
            .getFloatValue();

    public FloatValue height = ValueBuilder.create(this, "Height")
            .setDefaultFloatValue(25)
            .setFloatStep(1)
            .setMinFloatValue(20)
            .setMaxFloatValue(40)
            .build()
            .getFloatValue();

    public FloatValue cornerRadius = ValueBuilder.create(this, "Corner Radius")
            .setDefaultFloatValue(12)
            .setFloatStep(1)
            .setMinFloatValue(5)
            .setMaxFloatValue(20)
            .build()
            .getFloatValue();

    public FloatValue posX = ValueBuilder.create(this, "Position X")
            .setDefaultFloatValue(0)
            .setFloatStep(1)
            .setMinFloatValue(-200)
            .setMaxFloatValue(200)
            .build()
            .getFloatValue();

    public FloatValue posY = ValueBuilder.create(this, "Position Y")
            .setDefaultFloatValue(15)
            .setFloatStep(1)
            .setMinFloatValue(5)
            .setMaxFloatValue(100)
            .build()
            .getFloatValue();
/*
    public FloatValue animationSpeed = ValueBuilder.create(this, "Animation Speed")
            .setDefaultFloatValue(8)
            .setFloatStep(1)
            .setMinFloatValue(1)
            .setMaxFloatValue(20)
            .build()
            .getFloatValue();

    public FloatValue elasticEffect = ValueBuilder.create(this, "Elastic Effect")
            .setDefaultFloatValue(0.3f)
            .setFloatStep(0.1f)
            .setMinFloatValue(0f)
            .setMaxFloatValue(1f)
            .build()
            .getFloatValue();

    public BooleanValue showBorder = ValueBuilder.create(this, "Show Border")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public FloatValue borderWidth = ValueBuilder.create(this, "Border Width")
            .setDefaultFloatValue(1.5f)
            .setFloatStep(0.5f)
            .setMinFloatValue(0.5f)
            .setMaxFloatValue(3f)
            .setVisibility(() -> showBorder.getCurrentValue())
            .build()
            .getFloatValue();
*/
    @Override
    public boolean onEnable() {
        ChatUtils.addChatMessage("After activating this module, you must look for the modules that can change the style of the Dynamic Island, such as the WaterMark module.");
        return false;
    }
}