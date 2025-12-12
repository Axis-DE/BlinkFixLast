package moe.ichinomiya.naven.ui.notification.styles;

import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.type.NotificationStyle;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class SkyrimNotification implements NotificationStyle {

    private static final int BACKGROUND_COLOR = 0xFF4A6572;
    private static final int BORDER_COLOR = 0xFF87CEEB;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ACCENT_COLOR = 0xFF5D8AA8;
    private static final int MIN_WIDTH = 120;
    private static final int MAX_WIDTH = 250;
    private static final int BASE_HEIGHT = 32;
    private static final int PADDING_HORIZONTAL = 20;
    private static final int PADDING_VERTICAL = 8;
    private static final int CORNER_RADIUS = 8;

    @Override
    public void render(Notification notification) {
        float width = getWidth(notification);
        float height = getHeight(notification);
        float animationProgress = notification.getAnimationProgress();

        float scale;
        if (notification.isShowing()) {
            scale = easeOutElastic(animationProgress);
        } else if (notification.isHiding()) {
            scale = 1.0f - easeInBack(1.0f - animationProgress);
        } else {
            scale = 1.0f;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2, height / 2, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-width / 2, -height / 2, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, animationProgress);

        RenderUtils.drawRoundedRect(0, 0, width, height, CORNER_RADIUS, BACKGROUND_COLOR);
        RenderUtils.drawRect(0, 0, width, 2, BORDER_COLOR);
        RenderUtils.drawRect(0, height - 2, width, 2, BORDER_COLOR);
        RenderUtils.drawRect(0, 0, 2, height, BORDER_COLOR);
        RenderUtils.drawRect(width - 2, 0, 2, height, BORDER_COLOR);

        drawSkyrimDecoration(width, height);

        GlyphPageFontRenderer font = Notification.getFont();
        String message = notification.getMessage();
        String displayText = message;
        int textWidth = font.getStringWidth(message);

        if (textWidth > width - PADDING_HORIZONTAL * 2) {
            displayText = truncateText(message, (int)(width - PADDING_HORIZONTAL * 2), font);
        }

        int finalTextWidth = font.getStringWidth(displayText);
        float textX = (width - finalTextWidth) / 2;
        float textY = (height - font.getFontHeight()) / 2;

        font.drawStringWithShadow(displayText, textX, textY, TEXT_COLOR);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
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

    @Override
    public float getWidth(Notification notification) {
        String message = notification.getMessage();
        int stringWidth = Notification.getFont().getStringWidth(message);
        int calculatedWidth = stringWidth + PADDING_HORIZONTAL * 2;

        float maxScreenWidth = Minecraft.getMinecraft().displayWidth / 2 - 40;
        return Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, Math.min(calculatedWidth, maxScreenWidth)));
    }

    private String truncateText(String text, int maxWidth, GlyphPageFontRenderer font) {
        if (font.getStringWidth(text) <= maxWidth) {
            return text;
        }

        StringBuilder builder = new StringBuilder();
        String[] words = text.split(" ");

        for (String word : words) {
            String testString = builder.length() > 0 ? builder.toString() + " " + word : word;
            if (font.getStringWidth(testString + "...") <= maxWidth) {
                builder.append(builder.length() > 0 ? " " + word : word);
            } else {
                break;
            }
        }

        if (builder.length() == 0) {
            builder.append(text.substring(0, Math.min(8, text.length())));
        }

        return builder.toString() + "...";
    }

    @Override
    public float getHeight(Notification notification) {
        return BASE_HEIGHT;
    }

    private void drawSkyrimDecoration(float width, float height) {
        RenderUtils.drawRect(15, 4, width - 30, 1, ACCENT_COLOR);
        RenderUtils.drawRect(15, height - 5, width - 30, 1, ACCENT_COLOR);
        drawCornerSymbol(8, 8, ACCENT_COLOR);
        drawCornerSymbol(width - 12, 8, ACCENT_COLOR);
        drawCornerSymbol(8, height - 12, ACCENT_COLOR);
        drawCornerSymbol(width - 12, height - 12, ACCENT_COLOR);
    }

    private void drawCornerSymbol(float x, float y, int color) {
        RenderUtils.drawRect(x, y, 4, 1, color);
        RenderUtils.drawRect(x, y, 1, 4, color);
    }
}