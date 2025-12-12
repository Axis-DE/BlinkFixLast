package moe.ichinomiya.naven.ui.notification.styles;

import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.type.NotificationStyle;
import moe.ichinomiya.naven.utils.RenderUtils;

public class NavenNotification implements NotificationStyle {

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