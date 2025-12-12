package moe.ichinomiya.naven.commands.impl;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.commands.Command;
import moe.ichinomiya.naven.commands.CommandInfo;
import moe.ichinomiya.naven.modules.impl.misc.HackerDetector;
import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import net.minecraft.client.Minecraft;

@CommandInfo(name = "hacker", description = "Mark hackers.", aliases = {"hack"})
public class CommandHacker extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            String playerName = args[0];

            HackerDetector detector = (HackerDetector) BlinkFix.getInstance().getModuleManager().getModule(HackerDetector.class);
            if (detector.addHacker(playerName)) {
                Notification navenNotification = new Notification(NotificationLevel.INFO, playerName + " has been marked as a hacker!", 5000);
                BlinkFix.getInstance().getNotificationManager().addNotification(navenNotification);
            }
        }
    }

    @Override
    public String[] onTab(String[] args) {
        return Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream().map(info -> info.getGameProfile().getName()).filter(name -> name.toLowerCase().startsWith(args.length == 0 ? "" : args[0].toLowerCase())).toArray(String[]::new);
    }
}
