package moe.ichinomiya.naven.events.impl;

import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;
import net.minecraft.client.entity.EntityOtherPlayerMP;

@Data
public class EventSpawnPlayer implements Event {
    private final EntityOtherPlayerMP player;
}
