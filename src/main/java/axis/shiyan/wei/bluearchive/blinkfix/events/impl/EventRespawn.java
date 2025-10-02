package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;

@Data
@AllArgsConstructor
public class EventRespawn implements Event {
    private EventType type;
}
