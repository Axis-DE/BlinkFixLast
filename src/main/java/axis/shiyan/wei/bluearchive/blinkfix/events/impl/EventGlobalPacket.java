package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import net.minecraft.network.Packet;

@Data
@AllArgsConstructor
public class EventGlobalPacket extends EventCancellable {
    private final EventType type;
    private final Packet<?> packet;
}
