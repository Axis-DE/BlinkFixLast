package moe.ichinomiya.naven.events.impl;

import lombok.Data;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;

@Data
public class EventClick extends EventCancellable {
    private boolean shouldRightClick;
    private int slot;

    public EventClick(final int slot) {
        this.slot = slot;
    }
}
