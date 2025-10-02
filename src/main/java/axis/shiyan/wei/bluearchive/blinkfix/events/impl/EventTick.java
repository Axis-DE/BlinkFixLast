package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;

@Getter
@AllArgsConstructor
public class EventTick implements Event {
    private final EventType type;
}
