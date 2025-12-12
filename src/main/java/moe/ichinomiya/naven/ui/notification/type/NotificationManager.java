//package axis.shiyan.wei.bluearchive.blinkfix.ui.notification.manager;
//
//import api.events.moe.ichinomiya.naven.EventTarget;
//import types.api.events.moe.ichinomiya.naven.EventType;
//import impl.events.moe.ichinomiya.naven.EventRender2D;
//import impl.events.moe.ichinomiya.naven.EventShader;
//import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.styles.Notification;
//import utils.moe.ichinomiya.naven.SmoothAnimationTimer;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.GlStateManager;
//
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//public class NotificationManager {
//    private final List<Notification> navenNotifications = new CopyOnWriteArrayList<>();
//
//    public void addNotification(Notification navenNotification) {
//        navenNotifications.add(navenNotification);
//    }
//
//    @EventTarget
//    public void onRender(EventRender2D e) {
//        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
//        float height = 5;
//        for (Notification navenNotification : navenNotifications) {
//            GlStateManager.pushMatrix();
//            float width = navenNotification.getWidth();
//            height += navenNotification.getHeight();
//
//            SmoothAnimationTimer widthTimer = navenNotification.getWidthTimer();
//            SmoothAnimationTimer heightTimer = navenNotification.getHeightTimer();
//
//            float lifeTime = System.currentTimeMillis() - navenNotification.getCreateTime();
//            if (lifeTime > navenNotification.getMaxAge()) {
//                widthTimer.target = 0;
//                heightTimer.target = 0;
//
//                if (widthTimer.isAnimationDone(true)) {
//                    navenNotifications.remove(navenNotification);
//                }
//            } else {
//                widthTimer.target = width;
//                heightTimer.target = height;
//            }
//            widthTimer.update(true);
//            heightTimer.update(true);
//
//            GlStateManager.translate(scaledResolution.getScaledWidth() - widthTimer.value + 2, scaledResolution.getScaledHeight() - heightTimer.value, 0);
//            navenNotification.render();
//            GlStateManager.popMatrix();
//        }
//    }
//
//    @EventTarget
//    public void onRender(EventShader e) {
//        if (e.getType() == EventType.SHADOW) {
//            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
//
//            for (Notification navenNotification : navenNotifications) {
//                GlStateManager.pushMatrix();
//                SmoothAnimationTimer widthTimer = navenNotification.getWidthTimer();
//                SmoothAnimationTimer heightTimer = navenNotification.getHeightTimer();
//                GlStateManager.translate(scaledResolution.getScaledWidth() - widthTimer.value + 2, scaledResolution.getScaledHeight() - heightTimer.value, 0);
//                navenNotification.renderShader();
//                GlStateManager.popMatrix();
//            }
//        }
//    }
//}
