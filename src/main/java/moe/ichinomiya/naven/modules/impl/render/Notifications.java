package moe.ichinomiya.naven.modules.impl.render;


import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.notification.type.NotificationType;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;

@ModuleInfo(name = "Notifications", description = "Control notification display", category = Category.RENDER)
public class Notifications extends Module {

    public ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(0)
            .setModes("Naven", "Skyrim", "DynamicIsland")
            .setOnUpdate(value -> {
                switch (value.getModeValue().getCurrentMode()) {
                    case "Skyrim":
                        BlinkFix.getInstance().getNotificationManager().setCurrentStyle(NotificationType.SKYRIM);
                        break;
                    case "DynamicIsland":
                        BlinkFix.getInstance().getNotificationManager().setCurrentStyle(NotificationType.DYNAMICISLAND);
                        break;
                    default:
                        BlinkFix.getInstance().getNotificationManager().setCurrentStyle(NotificationType.NAVEN);
                        break;
                }
            }).build().getModeValue();

    @Override
    public boolean onEnable() {
        BlinkFix.getInstance().getNotificationManager().setEnabled(true);
        BlinkFix.getInstance().getNotificationManager().setCurrentStyle(getCurrentStyle());
        return true;
    }

    @Override
    public boolean onDisable() {
        BlinkFix.getInstance().getNotificationManager().setEnabled(false);
        return false;
    }

    public NotificationType getCurrentStyle() {
        switch (mode.getCurrentMode()) {
            case "Skyrim":
                return NotificationType.SKYRIM;
            case "DynamicIsland":
                return NotificationType.DYNAMICISLAND;
            default:
                return NotificationType.NAVEN;
        }
    }
}