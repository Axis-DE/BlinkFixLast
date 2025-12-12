package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;
import moe.ichinomiya.naven.events.api.types.EventType;

@Getter @Setter
@AllArgsConstructor
public class EventStep extends EventCancellable {
    private final EventType type;
    private double stepHeight;
    private double realHeight;
}
