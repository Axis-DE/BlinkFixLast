package axis.shiyan.wei.bluearchive.blinkfix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.events.Event;
import org.lwjgl.util.vector.Vector2f;

@Data
@AllArgsConstructor
public final class EventLook implements Event {
    private Vector2f rotation;
}
