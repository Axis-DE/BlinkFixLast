package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ichinomiya.naven.events.api.events.Event;
import moe.ichinomiya.naven.events.api.types.EventType;

@Getter
@AllArgsConstructor
public class EventTick implements Event {
    private final EventType type;
}
