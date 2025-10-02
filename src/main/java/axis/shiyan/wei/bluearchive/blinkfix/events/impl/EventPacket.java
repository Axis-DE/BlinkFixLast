package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import net.minecraft.network.Packet;

@Getter @Setter
@AllArgsConstructor
public class EventPacket extends EventCancellable {
    private final EventType type;
    private Packet<?> packet;

    private final EventState eventType;
    public enum EventState {
        SEND,
        RECEIVE
    }
}
