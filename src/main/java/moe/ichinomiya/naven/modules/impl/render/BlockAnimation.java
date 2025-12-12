package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import moe.ichinomiya.naven.values.impl.ModeValue;

@ModuleInfo(name = "BlockAnimation", description = "Block animation", category = Category.RENDER)
public class BlockAnimation extends Module {
    private static BlockAnimation instance;
    ModeValue mode = ValueBuilder.create(this, "Style").setDefaultModeIndex(0).setModes("Vanilla", "Slide", "Sigma", "Push", "1.7", "Fixed", "Reverse", "Spin", "Screw", "Poke", "Swang", "Swong", "Swank", "Swaing").build().getModeValue();
    FloatValue speed = ValueBuilder.create(this, "Speed").setDefaultFloatValue(1).setFloatStep(0.01f).setMinFloatValue(0.1f).setMaxFloatValue(2f).build().getFloatValue();

    BooleanValue customEquippedProgress = ValueBuilder.create(this, "Custom Equipped Progress").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue equippedProgress = ValueBuilder.create(this, "Equipped Progress").setDefaultFloatValue(1).setFloatStep(0.01f).setMinFloatValue(0).setMaxFloatValue(1).setVisibility(() -> customEquippedProgress.getCurrentValue()).build().getFloatValue();

    BooleanValue customSwingProgress = ValueBuilder.create(this, "Custom Swing Progress").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue swingProgress = ValueBuilder.create(this, "Swing Progress").setDefaultFloatValue(1).setFloatStep(0.01f).setMinFloatValue(0.01f).setMaxFloatValue(1).setVisibility(() -> customSwingProgress.getCurrentValue()).build().getFloatValue();

    BooleanValue customPosition = ValueBuilder.create(this, "Custom Position").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue x = ValueBuilder.create(this, "Position X").setDefaultFloatValue(0).setFloatStep(0.01f).setMinFloatValue(-2).setMaxFloatValue(2).setVisibility(() -> customPosition.getCurrentValue()).build().getFloatValue();
    FloatValue y = ValueBuilder.create(this, "Position Y").setDefaultFloatValue(0).setFloatStep(0.01f).setMinFloatValue(-2).setMaxFloatValue(2).setVisibility(() -> customPosition.getCurrentValue()).build().getFloatValue();
    FloatValue z = ValueBuilder.create(this, "Position Z").setDefaultFloatValue(0).setFloatStep(0.01f).setMinFloatValue(-2).setMaxFloatValue(2).setVisibility(() -> customPosition.getCurrentValue()).build().getFloatValue();

    BooleanValue customRotation = ValueBuilder.create(this, "Custom Rotation").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue rotationX = ValueBuilder.create(this, "Rotation X").setDefaultFloatValue(0).setFloatStep(0.01f).setMinFloatValue(-3).setMaxFloatValue(3).setVisibility(() -> customRotation.getCurrentValue()).build().getFloatValue();
    FloatValue rotationY = ValueBuilder.create(this, "Rotation Y").setDefaultFloatValue(0).setFloatStep(0.01f).setMinFloatValue(-3).setMaxFloatValue(3).setVisibility(() -> customRotation.getCurrentValue()).build().getFloatValue();
    FloatValue rotationZ = ValueBuilder.create(this, "Rotation Z").setDefaultFloatValue(0).setFloatStep(0.01f).setMinFloatValue(-3).setMaxFloatValue(3).setVisibility(() -> customRotation.getCurrentValue()).build().getFloatValue();

    public BlockAnimation() {
        super();
        instance = this;
    }

    public static float getEquippedProgress() {
        return instance.isEnabled() && instance.customEquippedProgress.getCurrentValue() ? instance.equippedProgress.getCurrentValue() : 1;
    }

    public static float[] getRotation() {
        return instance.isEnabled() && instance.customRotation.getCurrentValue() ? new float[]{instance.rotationX.getCurrentValue(), instance.rotationY.getCurrentValue(), instance.rotationZ.getCurrentValue()} : null;
    }

    public static float[] getPosition() {
        return instance.isEnabled() && instance.customPosition.getCurrentValue() ? new float[]{instance.x.getCurrentValue(), instance.y.getCurrentValue(), instance.z.getCurrentValue()} : null;
    }

    public static String getBlockAnimation() {
        return instance.isEnabled() ? instance.mode.getCurrentMode() : "Vanilla";
    }

    public static float getAnimationSpeed() {
        return instance.isEnabled() ? instance.speed.getCurrentValue() : 1.0F;
    }

    public static float getSwingProgress() {
        return instance.isEnabled() && instance.customSwingProgress.getCurrentValue() ? instance.swingProgress.getCurrentValue() : 1.0F;
    }
}
