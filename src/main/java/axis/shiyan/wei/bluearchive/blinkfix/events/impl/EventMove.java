package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;

@Data
@AllArgsConstructor
public class EventMove implements Event {
    private double x;
    private double y;
    private double z;
    private boolean safeWalk;
    private boolean onGround;
}
