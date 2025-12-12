package moe.ichinomiya.naven.ui.notification.manager;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventRender2D;
import moe.ichinomiya.naven.events.impl.EventShader;
import moe.ichinomiya.naven.modules.impl.render.WaterMark;
import moe.ichinomiya.naven.ui.dynamicisland.DynamicIslandRenderer;
import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.styles.DynamicIslandNotification;
import moe.ichinomiya.naven.ui.notification.type.NotificationType;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import moe.ichinomiya.naven.modules.Module;
import net.minecraft.client.renderer.GlStateManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private final List<DynamicIslandNotification> dynamicIslandNotifications = new CopyOnWriteArrayList<>();
    private boolean enabled = true;
    private NotificationType currentStyle = NotificationType.NAVEN;
    private final Map<Notification, Float> notificationYMap = new HashMap<>();
    private final Map<Notification, Long> animationTimeMap = new HashMap<>();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            notifications.clear();
        }
    }
    public void addDynamicIslandNotification(Module module, boolean enabled) {
        if (!this.enabled) return;

        DynamicIslandNotification notification = new DynamicIslandNotification(module, enabled);
        dynamicIslandNotifications.add(notification);
    }
    public void setCurrentStyle(NotificationType style) {
        this.currentStyle = style;
        for (Notification notification : notifications) {
            notification.setType(style);
        }
    }

    public NotificationType getCurrentStyle() {
        return currentStyle;
    }

    public void addNotification(Notification notification) {
        if (!enabled) return;
        if (notification.getType() == null || notification.getType() == NotificationType.NAVEN) {
            notification.setType(currentStyle);
        }

        notifications.add(notification);
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        if (!enabled) return;

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        float screenWidth = scaledResolution.getScaledWidth();
        float screenHeight = scaledResolution.getScaledHeight();
        float navenY = 5;
        float currentSkyrimY = screenHeight - 20;

        for (Notification notification : notifications) {
            GlStateManager.pushMatrix();

            float lifeTime = System.currentTimeMillis() - notification.getCreateTime();

            if (lifeTime > notification.getMaxAge() && !notification.isHiding()) {
                notification.startHideAnimation();
            } else if (lifeTime <= 100 && !notification.isShowing() && !notification.isHiding()) {
                notification.startShowAnimation();
            }

            if (notification.isHiding() && notification.isAnimationDone()) {
                notifications.remove(notification);
                GlStateManager.popMatrix();
                continue;
            }

            float animationProgress = notification.getAnimationProgress();

            if (notification.getType() == NotificationType.NAVEN) {
                float width = notification.getWidth();
                float height = notification.getHeight();

                SmoothAnimationTimer widthTimer = notification.getWidthTimer();
                SmoothAnimationTimer heightTimer = notification.getHeightTimer();

                if (lifeTime > notification.getMaxAge()) {
                    widthTimer.target = 0;
                    heightTimer.target = 0;
                } else {
                    widthTimer.target = width;
                    heightTimer.target = navenY + height;
                }

                widthTimer.update(true);
                heightTimer.update(true);

                GlStateManager.translate(screenWidth - widthTimer.value + 2, screenHeight - heightTimer.value, 0);
                notification.render();
                navenY += height;

            } else {
                float width = notification.getWidth();
                float height = notification.getHeight();

                float targetX = screenWidth - width - 15;
                float targetY = currentSkyrimY - height;

                GlStateManager.translate(targetX, targetY, 0);
                GlStateManager.color(1.0F, 1.0F, 1.0F, animationProgress);

                notification.render();

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                currentSkyrimY -= height + 8;
            }

            GlStateManager.popMatrix();
        }

        renderDynamicIslandNotifications(e);
    }

    private void renderDynamicIslandNotifications(EventRender2D e) {
        DynamicIslandRenderer dynamicIslandRenderer = getDynamicIslandRenderer();
        if (dynamicIslandRenderer == null) return;
        for (DynamicIslandNotification notification : dynamicIslandNotifications) {
            dynamicIslandRenderer.addDynamicIslandNotification(notification);
        }
        dynamicIslandNotifications.removeIf(DynamicIslandNotification::isExpired);
    }

    private DynamicIslandRenderer getDynamicIslandRenderer() {
        WaterMark waterMarkModule = (WaterMark) BlinkFix.getInstance().getModuleManager().getModule(WaterMark.class);
        if (waterMarkModule != null && waterMarkModule.isEnabled() &&
                waterMarkModule.mode.isCurrentMode("DynamicIsland")) {
            return new DynamicIslandRenderer();
        }
        return null;

    }

    @EventTarget
    public void onRender(EventShader e) {
        if (!enabled) return;

        if (e.getType() == EventType.SHADOW) {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

            for (Notification notification : notifications) {
                GlStateManager.pushMatrix();
                SmoothAnimationTimer widthTimer = notification.getWidthTimer();
                SmoothAnimationTimer heightTimer = notification.getHeightTimer();
                GlStateManager.translate(scaledResolution.getScaledWidth() - widthTimer.value + 2, scaledResolution.getScaledHeight() - heightTimer.value, 0);
                notification.renderShader();
                GlStateManager.popMatrix();
            }
        }
    }
}