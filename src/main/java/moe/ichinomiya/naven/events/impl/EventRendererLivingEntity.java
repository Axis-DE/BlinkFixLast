package moe.ichinomiya.naven.events.impl;

import lombok.Data;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;
import moe.ichinomiya.naven.events.api.types.EventType;
import net.minecraft.entity.Entity;

@Data
public class EventRendererLivingEntity extends EventCancellable {
    private Entity entity;
    private EventType type;
    private float limbSwing;
    private float limbSwingAmount;
    private float ageInTicks;
    private float rotationYawHead;
    private float rotationPitch;
    private float chestRot;
    private float offset;

    private boolean shouldInvisible = false;
    private float alpha = 0.15f;
    private boolean hideLayer = false;

    public EventRendererLivingEntity(EventType type, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYawHead, float rotationPitch, float chestRot, float offset) {
        this.type = type;
        this.entity = entity;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.rotationYawHead = rotationYawHead;
        this.rotationPitch = rotationPitch;
        this.chestRot = chestRot;
        this.offset = offset;
    }

    public EventRendererLivingEntity(EventType type, Entity entity) {
        this.type = type;
        this.entity = entity;
    }
}
