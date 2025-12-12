package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventGlobalPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
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
