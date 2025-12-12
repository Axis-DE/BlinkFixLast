package moe.ichinomiya.naven.ui.notification.styles;

import moe.ichinomiya.naven.modules.impl.move.Scaffold;
import moe.ichinomiya.naven.ui.notification.type.NotificationStyle;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.ui.notification.Notification;
import lombok.Data;
import moe.ichinomiya.naven.modules.Module;


@Data
public class DynamicIslandNotification implements NotificationStyle {
    private final Module module;
    private final boolean enabled;
    private final long createTime;
    private static final long DURATION = 1000;

    public DynamicIslandNotification(Module module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
        this.createTime = System.currentTimeMillis();
    }

    public DynamicIslandNotification() {
        this.module = null;
        this.enabled = false;
        this.createTime = 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createTime > DURATION;
    }

    public String getDisplayText() {
        if (module instanceof Scaffold && enabled) {
            return null;
        }
        return "Module " + module.getPrettyName() + (enabled ? " on" : " off");
    }

    public long getRemainingTime() {
        return Math.max(0, DURATION - (System.currentTimeMillis() - createTime));
    }

    public float getProgress() {
        return (float) getRemainingTime() / DURATION;
    }

    public boolean shouldDisplay() {
        return getDisplayText() != null;
    }

    @Override
    public void render(Notification notification) {
        RenderUtils.drawBoundRoundedRect(2, 4, getWidth(notification), 20, 5, notification.getLevel().getColor());
        Notification.getFont().drawStringWithShadow(notification.getMessage(), 6, 7, 0xFFFFFFFF);
    }

    @Override
    public float getWidth(Notification notification) {
        int stringWidth = Notification.getFont().getStringWidth(notification.getMessage());
        return stringWidth + 12;
    }

    @Override
    public float getHeight(Notification notification) {
        return 24;
    }
}