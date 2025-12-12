package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.ichinomiya.naven.events.api.events.Event;

@Data
@AllArgsConstructor
public class EventSlowdown implements Event {
    private double speed;
    private boolean slowdown;
}
