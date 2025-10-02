package axis.shiyan.wei.bluearchive.blinkfix.commands.impl;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.commands.Command;
import axis.shiyan.wei.bluearchive.blinkfix.commands.CommandInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.HackerDetector;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.Notification;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.NotificationLevel;
import net.minecraft.client.Minecraft;

@CommandInfo(name = "hacker", description = "Mark hackers.", aliases = {"hack"})
public class CommandHacker extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            String playerName = args[0];

            HackerDetector detector = (HackerDetector) BlinkFix.getInstance().getModuleManager().getModule(HackerDetector.class);
            if (detector.addHacker(playerName)) {
                Notification notification = new Notification(NotificationLevel.INFO, playerName + " has been marked as a hacker!", 5000);
                BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            }
        }
    }

    @Override
    public String[] onTab(String[] args) {
        return Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream().map(info -> info.getGameProfile().getName()).filter(name -> name.toLowerCase().startsWith(args.length == 0 ? "" : args[0].toLowerCase())).toArray(String[]::new);
    }
}
