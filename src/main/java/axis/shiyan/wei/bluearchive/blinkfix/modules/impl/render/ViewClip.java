package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;

@ModuleInfo(name = "ViewClip", description = "Allows you to see through blocks", category = Category.RENDER)
public class ViewClip extends Module {
    public FloatValue scale = ValueBuilder.create(this, "Scale")
            .setMinFloatValue(0.5f)
            .setMaxFloatValue(2f)
            .setDefaultFloatValue(1f)
            .setFloatStep(0.1f)
            .build()
            .getFloatValue();

    public BooleanValue animation = ValueBuilder.create(this, "Animation").setDefaultBooleanValue(true).build().getBooleanValue();

    public FloatValue animationSpeed = ValueBuilder.create(this, "Animation Speed")
            .setMinFloatValue(0.1f)
            .setMaxFloatValue(1f)
            .setDefaultFloatValue(0.5f)
            .setFloatStep(0.1f)
            .setVisibility(() -> animation.getCurrentValue())
            .build()
            .getFloatValue();

    public SmoothAnimationTimer personViewAnimation = new SmoothAnimationTimer(100);

    int lastPersonView = 0;

    @EventTarget
    public void onRender(EventRender2D e) {
        if (lastPersonView != mc.gameSettings.thirdPersonView) {
            lastPersonView = mc.gameSettings.thirdPersonView;

            if (lastPersonView == 1 || lastPersonView == 0) {
                personViewAnimation.value = 0;
            }
        }

        personViewAnimation.speed = animationSpeed.getCurrentValue();
        personViewAnimation.update(true);
    }
}
