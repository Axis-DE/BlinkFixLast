package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventShader;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.watermark.NavenWatermark;
import axis.shiyan.wei.bluearchive.blinkfix.ui.watermark.SilenceFixWaterMark;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;

@ModuleInfo(name = "WaterMark", description = "The WaterMark", category = Category.RENDER)
public class WaterMark extends Module {
    private axis.shiyan.wei.bluearchive.blinkfix.ui.watermark.WaterMark waterMark;

    public ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("BlinkFix", "SilenceFix").setOnUpdate(value -> {
        if (value.getModeValue().isCurrentMode("BlinkFix")) {
            waterMark = new NavenWatermark();
        } else {
            waterMark = new SilenceFixWaterMark();
        }
    }).build().getModeValue();

    @EventTarget
    public void onRender(EventRender2D e) {
        if (waterMark != null) {
            waterMark.render();
        }
    }

    @EventTarget
    public void onRender(EventShader e) {
        if (waterMark != null) {
            waterMark.renderShader();
        }
    }
}
