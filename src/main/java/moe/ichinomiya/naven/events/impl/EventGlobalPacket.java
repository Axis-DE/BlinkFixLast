package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;
import moe.ichinomiya.naven.events.api.types.EventType;
import net.minecraft.network.Packet;

@Data
@AllArgsConstructor
public class EventGlobalPacket extends EventCancellable {
    private final EventType type;
    private final Packet<?> packet;
}
