package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.Notification;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.NotificationLevel;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;

@ModuleInfo(name = "ClientFriend", description = "Treat other users as friend!", category = Category.MISC)
public class ClientFriend extends Module {
    public static TimeHelper attackTimer = new TimeHelper();
    public boolean onEnable(){
        return true;
    }
    @Override
    public void onDisable() {
        attackTimer.reset();
        Notification notification = new Notification(NotificationLevel.INFO, "You can attack other players after 15 seconds.", 15000);
        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
    }
}
