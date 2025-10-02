package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;

@Data
public class EventClick extends EventCancellable {
    private boolean shouldRightClick;
    private int slot;

    public EventClick(final int slot) {
        this.slot = slot;
    }
}
