package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import lombok.extern.log4j.Log4j2;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventGlobalPacket;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRespawn;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.Notification;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.NotificationLevel;
import axis.shiyan.wei.bluearchive.blinkfix.utils.ChatUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@ModuleInfo(name = "HackerDetector", description = "Detects other hackers", category = Category.MISC)
public class
HackerDetector extends Module {
    private static final List<String> cheatingPlayers = new ArrayList<>();

    public static boolean isCheating(EntityPlayer player) {
        Module module = BlinkFix.getInstance().getModuleManager().getModule(HackerDetector.class);
        return module.isEnabled() && cheatingPlayers.contains(player.getName());
    }

    public static boolean isCheating(String player) {
        Module module = BlinkFix.getInstance().getModuleManager().getModule(HackerDetector.class);
        return module.isEnabled() && cheatingPlayers.contains(player);
    }

    public boolean addHacker(String hacker) {
        if (isEnabled() && !cheatingPlayers.contains(hacker)) {
            cheatingPlayers.add(hacker);
            return true;
        }
        return false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        if (e.getType() == EventType.JOIN_GAME) {
            cheatingPlayers.clear();
        }
    }

    @EventTarget
    public void onPacket(EventGlobalPacket e) {
        if (e.getType() == EventType.RECEIVE) {
            if (e.getPacket() instanceof S02PacketChat) {
                S02PacketChat packet = (S02PacketChat) e.getPacket();

                String chatMessage = packet.getChatComponent().getUnformattedText();
                String[] result = ChatUtils.processMessage(chatMessage);

                if (result != null) {
                    if (ChatUtils.isSpammer(result[1]) && !mc.thePlayer.getName().equals(result[0])) {
                        if (addHacker(result[0])) {
                            Notification notification = new Notification(NotificationLevel.INFO, result[0] + " is using Spammer(90%SilenceFix)!", 5000);
                            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onEnable() {
        cheatingPlayers.clear();
        return false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && mc.thePlayer.ticksExisted > 10) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (mc.thePlayer == player) continue;
                if (player.getEntityId() < 0) continue;

                if (player.autoBlockVl >= 5) {
                    if (addHacker(player.getName())) {
                        Notification notification = new Notification(NotificationLevel.INFO, player.getName() + " is using AutoBlock!", 5000);
                        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                    }
                }

                if (player.noSlowVl >= 15) {
                    if (addHacker(player.getName())) {
                        Notification notification = new Notification(NotificationLevel.INFO, player.getName() + " is using No Slowdown!", 5000);
                        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                    }
                }
            }
        }
    }
}
