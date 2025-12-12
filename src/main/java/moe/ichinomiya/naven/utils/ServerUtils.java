package moe.ichinomiya.naven.utils;

import moe.ichinomiya.naven.ui.notification.Notification;
import lombok.Getter;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.EventGlobalPacket;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

public class ServerUtils {
    public static ServerType serverType = ServerType.UNKNOWN;

    @Getter
    private static int grimTransactionCount = 0;

    @EventTarget(Priority.HIGHEST)
    public void onAllPackets(EventGlobalPacket e) {
        if (e.getType() == EventType.RECEIVE) {
            if (e.getPacket() instanceof S32PacketConfirmTransaction) {
                S32PacketConfirmTransaction packet = (S32PacketConfirmTransaction) e.getPacket();

                if (!packet.func_148888_e() && packet.getWindowId() == 0 && packet.getActionNumber() <= 0) {
                    grimTransactionCount++;
                }
            } else if (e.getPacket() instanceof S01PacketJoinGame) {
                grimTransactionCount = 0;
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (serverType == ServerType.UNKNOWN && e.getType() == EventType.RECEIVE && e.getPacket() instanceof S3FPacketCustomPayload) {
            S3FPacketCustomPayload packet = (S3FPacketCustomPayload) e.getPacket();
            if (packet.getChannelName().contains("hyt0")) {
                serverType = ServerType.GERM_PLUGIN;
            }
        } else if (e.getPacket() instanceof C00PacketLoginStart) {
            serverType = ServerType.UNKNOWN;
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (e.getType() == EventType.POST && serverType == ServerType.UNKNOWN && player != null && player.ticksExisted > 10) {
            String clientBrand = player.getClientBrand();

            if (clientBrand != null && clientBrand.contains("DionaSpigot")) {
                serverType = ServerType.LOYISA_TEST_SERVER;
            }

            if (player.ticksExisted == 20) {
                BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.WARNING, "You are playing on a unsupported server, some features are disabled.", 3000));
            }
        }
    }

    public enum ServerType {
        UNKNOWN, LOYISA_TEST_SERVER, GERM_PLUGIN
    }
}
