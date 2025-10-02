package axis.shiyan.wei.bluearchive.blinkfix.events;

import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import net.minecraft.entity.EntityLivingBase;

@Data
public class EventLivingUpdate implements Event {
    private final EntityLivingBase entity;
}
