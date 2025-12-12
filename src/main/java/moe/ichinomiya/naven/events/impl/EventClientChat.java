package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;

@Getter
@AllArgsConstructor
public class EventClientChat extends EventCancellable {
    private final String message;
}
