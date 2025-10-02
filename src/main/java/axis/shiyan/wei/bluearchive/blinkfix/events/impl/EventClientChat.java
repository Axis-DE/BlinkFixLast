package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;

@Getter
@AllArgsConstructor
public class EventClientChat extends EventCancellable {
    private final String message;
}
