package moe.ichinomiya.naven.ui.notification.type;


import moe.ichinomiya.naven.ui.notification.styles.DynamicIslandNotification;
import moe.ichinomiya.naven.ui.notification.styles.NavenNotification;
import moe.ichinomiya.naven.ui.notification.styles.SkyrimNotification;

public enum NotificationType {

    NAVEN(new NavenNotification(), false),
    SKYRIM(new SkyrimNotification(), true),
    DYNAMICISLAND(new DynamicIslandNotification(), false);

    private final NotificationStyle style;
    private final boolean useElasticAnimation;

    NotificationType(NotificationStyle style, boolean useElasticAnimation) {
        this.style = style;
        this.useElasticAnimation = useElasticAnimation;
    }

    public NotificationStyle getStyle() {
        return style;
    }

    public boolean useElasticAnimation() {
        return useElasticAnimation;
    }
}