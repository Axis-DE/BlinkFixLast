package moe.ichinomiya.naven.ui.notification;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import moe.ichinomiya.naven.ui.notification.type.NotificationType;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
import lombok.Data;

@Data
public class Notification {
    private static final GlyphPageFontRenderer font = BlinkFix.getInstance().getFontManager().siyuan18;

    private final NotificationLevel level;
    private String message;
    private NotificationType type = NotificationType.NAVEN;

    private long maxAge, createTime = System.currentTimeMillis();

    private SmoothAnimationTimer widthTimer = new SmoothAnimationTimer(0),
            heightTimer = new SmoothAnimationTimer(0);

    private boolean showing = false;
    private boolean hiding = false;
    private long animationStartTime = 0;
    private static final long ANIMATION_DURATION = 500;

    public Notification(NotificationLevel level, String message, long age) {
        this.level = level;
        this.message = message;
        this.maxAge = age;
    }

    public Notification(NotificationLevel level, String message, long age, NotificationType type) {
        this.level = level;
        this.message = message;
        this.maxAge = age;
        this.type = type;
    }

    public void render() {
        type.getStyle().render(this);
    }

    public void renderShader() {
        if (type == NotificationType.NAVEN) {
            RenderUtils.drawBoundRoundedRect(2, 4, getWidth(), 20, 5, level.getColor());
        }
    }

    public float getWidth() {
        return type.getStyle().getWidth(this);
    }

    public float getHeight() {
        return type.getStyle().getHeight(this);
    }

    public static GlyphPageFontRenderer getFont() {
        return font;
    }

    public float getAnimationProgress() {
        if (!type.useElasticAnimation()) {
            return 1.0f;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - animationStartTime;

        if (elapsed <= 0) return showing ? 0f : 1f;
        if (elapsed >= ANIMATION_DURATION) return showing ? 1f : 0f;

        float progress = (float) elapsed / ANIMATION_DURATION;

        if (showing) {
            return easeOutElastic(progress);
        } else if (hiding) {
            return 1f - easeInBack(progress);
        }

        return 1f;
    }

    private float easeOutElastic(float x) {
        float c4 = (float) ((2 * Math.PI) / 3);
        return x == 0 ? 0 : x == 1 ? 1 : (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
    }

    private float easeInBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }

    public void startShowAnimation() {
        showing = true;
        hiding = false;
        animationStartTime = System.currentTimeMillis();
    }

    public void startHideAnimation() {
        showing = false;
        hiding = true;
        animationStartTime = System.currentTimeMillis();
    }

    public boolean isAnimationDone() {
        if (!type.useElasticAnimation()) return true;

        long currentTime = System.currentTimeMillis();
        return (currentTime - animationStartTime) >= ANIMATION_DURATION;
    }

    public boolean isShowing() {
        return showing;
    }

    public boolean isHiding() {
        return hiding;
    }
}