package axis.shiyan.wei.bluearchive.blinkfix.ui.notification;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender2D;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventShader;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        float height = 5;
        for (Notification notification : notifications) {
            GlStateManager.pushMatrix();
            float width = notification.getWidth();
            height += notification.getHeight();

            SmoothAnimationTimer widthTimer = notification.getWidthTimer();
            SmoothAnimationTimer heightTimer = notification.getHeightTimer();

            float lifeTime = System.currentTimeMillis() - notification.getCreateTime();
            if (lifeTime > notification.getMaxAge()) {
                widthTimer.target = 0;
                heightTimer.target = 0;

                if (widthTimer.isAnimationDone(true)) {
                    notifications.remove(notification);
                }
            } else {
                widthTimer.target = width;
                heightTimer.target = height;
            }
            widthTimer.update(true);
            heightTimer.update(true);

            GlStateManager.translate(scaledResolution.getScaledWidth() - widthTimer.value + 2, scaledResolution.getScaledHeight() - heightTimer.value, 0);
            notification.render();
            GlStateManager.popMatrix();
        }
    }

    @EventTarget
    public void onRender(EventShader e) {
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
