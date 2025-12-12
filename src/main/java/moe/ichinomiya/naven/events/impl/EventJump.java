package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;

@Setter
@Getter
@AllArgsConstructor
public class EventJump extends EventCancellable {
    private float yaw;
}
