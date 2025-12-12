package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

@ModuleInfo(name = "TimeChanger", description = "Change the time of the world", category = Category.RENDER)
public class TimeChanger extends Module {
    FloatValue time = ValueBuilder.create(this, "World Time").setDefaultFloatValue(8000).setFloatStep(1).setMinFloatValue(0).setMaxFloatValue(24000).build().getFloatValue();

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            mc.theWorld.setWorldTime((long) (time.getCurrentValue()));
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.setCancelled(true);
        }
    }
}
