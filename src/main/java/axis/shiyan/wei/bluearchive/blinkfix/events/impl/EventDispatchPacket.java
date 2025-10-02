package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.callables.EventCancellable;
import net.minecraft.network.Packet;

import java.util.List;

@Data
@AllArgsConstructor
public class EventDispatchPacket extends EventCancellable {
    private Packet<?> packet;
    private List<Packet<?>> additionalPackets;
}
