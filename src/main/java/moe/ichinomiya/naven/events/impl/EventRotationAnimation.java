package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@Data
@AllArgsConstructor
public class EventRotationAnimation implements Event {
    private final EntityLivingBase entity;
    private float rotationYawHead, renderYawOffset, renderHeadYaw, renderHeadPitch, partialTicks;

    private boolean isModified = false;

    public void setYaw(float yaw) {
        setYaw(yaw, yaw);
    }

    public void setYaw(float yaw, float prevYaw) {
        if (getPartialTicks() != 1.0F) {
            setRenderYawOffset(interpolateAngle(getPartialTicks(), prevYaw, yaw));
            setRenderHeadYaw(interpolateAngle(getPartialTicks(), prevYaw, yaw) - getRenderYawOffset());
            isModified = true;
        }
    }

    public void setPitch(float pitch) {
        setPitch(pitch, pitch);
    }

    public void setPitch(float pitch, float prevPitch) {
        if (getPartialTicks() != 1.0F) {
            setRenderHeadPitch(lerp(getPartialTicks(), prevPitch, pitch));
        }
    }

    public static float interpolateAngle(float p_219805_0_, float p_219805_1_, float p_219805_2_) {
        return p_219805_1_ + p_219805_0_ * MathHelper.wrapAngleTo180_float(p_219805_2_ - p_219805_1_);
    }

    public static float lerp(float pct, float start, float end) {
        return start + pct * (end - start);
    }
}
