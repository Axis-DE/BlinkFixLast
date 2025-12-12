package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import moe.ichinomiya.naven.utils.TimeHelper;
//import dev.yalan.live.LiveClient;

@ModuleInfo(name = "ClientFriend", description = "Treat other users as friend!", category = Category.MISC)
public class ClientFriend extends Module {
    public static TimeHelper attackTimer = new TimeHelper();
    
//    /**
//     * Check if a user is friendly (BlinkFix user)
//     * @param playerName the player name to check
//     * @return true if the player is a BlinkFix user (friendly), false otherwise
//     */
//    public static boolean isFriendly(String playerName) {
//        if (!LiveClient.INSTANCE.isActive()) {
//            return false;
//        }
//
//        // Check if the player is a BlinkFix user
//        return LiveClient.INSTANCE.liveUser.isBlinkFixUser() &&
//               LiveClient.INSTANCE.liveUser.getName().equals(playerName);
//    }
    
    public boolean onEnable(){
        return true;
    }
    @Override
    public boolean onDisable() {
        attackTimer.reset();
        Notification navenNotification = new Notification(NotificationLevel.INFO, "You can attack other players after 15 seconds.", 15000);
        BlinkFix.getInstance().getNotificationManager().addNotification(navenNotification);
        return false;
    }
}