package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;
import net.minecraft.client.gui.ScaledResolution;

@Data
@AllArgsConstructor
public class EventRenderUI implements Event {
    ScaledResolution resolution;
}
