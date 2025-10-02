package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;

@Data
@AllArgsConstructor
public class EventRender implements Event {
    public final float renderPartialTicks;
}
