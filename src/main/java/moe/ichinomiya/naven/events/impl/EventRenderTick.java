package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;
import moe.ichinomiya.naven.events.api.types.EventType;

@Data
@AllArgsConstructor
public class EventRenderTick implements Event {
    private final EventType type;
}
