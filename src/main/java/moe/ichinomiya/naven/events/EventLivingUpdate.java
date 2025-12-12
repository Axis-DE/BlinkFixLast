package moe.ichinomiya.naven.events;

import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;
import net.minecraft.entity.EntityLivingBase;

@Data
public class EventLivingUpdate implements Event {
    private final EntityLivingBase entity;
}
