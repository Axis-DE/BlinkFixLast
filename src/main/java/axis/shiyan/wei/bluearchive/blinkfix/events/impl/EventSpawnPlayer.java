package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import net.minecraft.client.entity.EntityOtherPlayerMP;

@Data
public class EventSpawnPlayer implements Event {
    private final EntityOtherPlayerMP player;
}
