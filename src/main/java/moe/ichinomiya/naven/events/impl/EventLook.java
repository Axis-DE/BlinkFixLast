package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;
import org.lwjgl.util.vector.Vector2f;

@Data
@AllArgsConstructor
public final class EventLook implements Event {
    private Vector2f rotation;
}
