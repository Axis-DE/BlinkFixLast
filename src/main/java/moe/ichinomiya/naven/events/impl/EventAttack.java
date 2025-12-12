package moe.ichinomiya.naven.events.impl;


import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;
import net.minecraft.entity.Entity;

public class EventAttack extends EventCancellable {
    private final boolean pre;
    private Entity target;

    public EventAttack( boolean pre) {
        this.pre = pre;
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public boolean isPre() {
        return pre;
    }


    public boolean isPost() {
        return !pre;
    }
}
