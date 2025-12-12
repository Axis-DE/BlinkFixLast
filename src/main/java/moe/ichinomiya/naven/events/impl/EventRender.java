package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;

@Data
@AllArgsConstructor
public class EventRender implements Event {
    public final float renderPartialTicks;
}
