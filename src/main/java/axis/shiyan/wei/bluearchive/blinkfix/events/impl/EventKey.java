package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;

@Getter @Setter
@AllArgsConstructor
public class EventKey extends EventCancellable {
    private final int key;
    private final boolean state;
}
