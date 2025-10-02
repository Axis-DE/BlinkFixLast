package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventGlobalPacket;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRespawn;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.Notification;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.NotificationLevel;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "AutoModule", description = "Automatically toggle modules in different games..", category = Category.MISC)
public class AutoModule extends Module {
    @EventTarget
    public void onRespawn(EventRespawn e) {
        if (e.getType() == EventType.JOIN_GAME) {
            mc.getNetHandler().getNetworkManager().sendPacket(new C14PacketTabComplete("/about "));
        }
    }

    @EventTarget
    public void onPacket(EventGlobalPacket e) {
        if (e.getPacket() instanceof S3APacketTabComplete) {
            S3APacketTabComplete packet = (S3APacketTabComplete) e.getPacket();
            List<String> plugins = Arrays.asList(packet.func_149630_c());

            if (plugins.contains("Skywars")) {
                BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.INFO, "You are currently playing Skywars!", 3000));
            } else if (plugins.contains("BedWarsAddon")) {
                BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.INFO, "You are currently playing BedWars!", 3000));
            } else if (plugins.contains("KitBattle")) {
                BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.INFO, "You are currently playing KitBattle!", 3000));
            }
        }
    }
}
