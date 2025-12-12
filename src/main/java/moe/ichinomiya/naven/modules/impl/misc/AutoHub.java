package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.network.play.client.C01PacketChatMessage;

@ModuleInfo(name = "AutoHub", description = "Automatically leave current game when health is low.", category = Category.MISC)
public class AutoHub extends Module {
    FloatValue health = ValueBuilder.create(this, "Health Percent").setDefaultFloatValue(0.1f).setFloatStep(0.05f).setMinFloatValue(0f).setMaxFloatValue(1f).build().getFloatValue();

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth() < health.getCurrentValue()) {
            mc.thePlayer.shiftClick(5);
            mc.thePlayer.shiftClick(6);
            mc.thePlayer.shiftClick(7);
            mc.thePlayer.shiftClick(8);

            mc.getNetHandler().getNetworkManager().sendPacket(new C01PacketChatMessage("/hub"));
        }
    }
}
