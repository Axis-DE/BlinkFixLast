package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import net.minecraft.entity.Entity;

@Data
@AllArgsConstructor
public class EventMiddleClickEntity implements Event {
    private final Entity entity;
}
