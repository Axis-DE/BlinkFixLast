package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventRender2D;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;

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
