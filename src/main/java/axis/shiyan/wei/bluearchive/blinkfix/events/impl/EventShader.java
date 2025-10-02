package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import net.minecraft.client.gui.ScaledResolution;

@Data
@AllArgsConstructor
public class EventShader implements Event {
    private final ScaledResolution resolution;
    private final EventType type;
}
