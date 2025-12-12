package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;
import moe.ichinomiya.naven.events.api.types.EventType;
import net.minecraft.client.gui.ScaledResolution;

@Data
@AllArgsConstructor
public class EventShader implements Event {
    private final ScaledResolution resolution;
    private final EventType type;
}
