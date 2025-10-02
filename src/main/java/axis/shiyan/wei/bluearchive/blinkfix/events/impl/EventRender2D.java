package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import net.minecraft.client.gui.ScaledResolution;

@Data
@AllArgsConstructor
public class EventRender2D implements Event {
    private final ScaledResolution resolution;
}
