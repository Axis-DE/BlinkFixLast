package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
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
