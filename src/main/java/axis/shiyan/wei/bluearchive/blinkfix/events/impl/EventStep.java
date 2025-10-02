package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;

@Getter @Setter
@AllArgsConstructor
public class EventStep extends EventCancellable {
    private final EventType type;
    private double stepHeight;
    private double realHeight;
}
