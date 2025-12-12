package moe.ichinomiya.naven.ui.notification.type;


import moe.ichinomiya.naven.ui.notification.Notification;

public interface NotificationStyle {
    void render(Notification notification);
    float getWidth(Notification notification);
    float getHeight(Notification notification);
}