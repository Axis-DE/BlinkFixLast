package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;

@Getter
@Setter
@AllArgsConstructor
public class EventMotion extends EventCancellable {
    private final EventType type;
    private double x, y, z;
    private float yaw;
    private float pitch;

    private boolean onGround;

    public EventMotion(EventType type, float yaw, float pitch) {
        this.type = type;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
