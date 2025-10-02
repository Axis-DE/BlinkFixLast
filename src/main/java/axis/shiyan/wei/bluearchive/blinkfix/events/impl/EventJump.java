package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;

@Setter
@Getter
@AllArgsConstructor
public class EventJump extends EventCancellable {
    private float yaw;
}
