package moe.ichinomiya.naven.events.impl;

import lombok.Getter;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;
import net.minecraft.entity.Entity;

@Getter
public class EventRenderEntity extends EventCancellable {
    private final Entity entity;

    public EventRenderEntity(Entity entity) {
        this.entity = entity;

        if (entity.getEntityId() < 0) {
            this.setCancelled(true);
        }
    }
}
