package axis.shiyan.wei.bluearchive.blinkfix.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class RenderUtils {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean isHovering(int mouseX, int mouseY, float xLeft, float yUp, float xRight, float yBottom) {
        return mouseX > xLeft && mouseX < xRight && mouseY > yUp && mouseY < yBottom;
    }

    public static boolean isHoveringBound(int mouseX, int mouseY, float xLeft, float yUp, float width, float height) {
        return mouseX > xLeft && mouseX < xLeft + width && mouseY > yUp && mouseY < yUp + height;
    }

    public static int reAlpha(int color, float alpha) {
        int col = MathUtils.clamp((int) (alpha * 255), 0, 255) << 24;
        col |= MathUtils.clamp(color >> 16 & 255, 0, 255) << 16;
        col |= MathUtils.clamp(color >> 8 & 255, 0, 255) << 8;
        col |= MathUtils.clamp(color & 255, 0, 255);

        return col;
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = MathUtils.clamp(alpha, 0, 255) << 24;
        color |= MathUtils.clamp(red, 0, 255) << 16;
        color |= MathUtils.clamp(green, 0, 255) << 8;
        color |= MathUtils.clamp(blue, 0, 255);

        return color;
    }

    public static void glColor(int hex) {
        float alpha = (float) (hex >> 24 & 255) / 255.0F;
        glColor(hex, alpha);
    }

    public static float interpolate(float old, float now, float partialTicks) {
        return old + (now - old) * partialTicks;
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
    }

    public static int colorBlendBetween(int color1, int color2, float offset) {
        if (offset > 1)
            offset = 1 - offset % 1;

        double invert = 1 - offset;
        int r = (int) ((color1 >> 16 & 0xFF) * invert +
                (color2 >> 16 & 0xFF) * offset);
        int g = (int) ((color1 >> 8 & 0xFF) * invert +
                (color2 >> 8 & 0xFF) * offset);
        int b = (int) ((color1 & 0xFF) * invert +
                (color2 & 0xFF) * offset);
        int a = (int) ((color1 >> 24 & 0xFF) * invert +
                (color2 >> 24 & 0xFF) * offset);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    public static void glColor(int hex, float alpha) {
        float red = (float) (hex >> 16 & 255) / 255.0F;
        float green = (float) (hex >> 8 & 255) / 255.0F;
        float blue = (float) (hex & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, int color) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        glColor(color);

        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();

        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    public static void drawOutlinedRect(float x, float y, float width, float height, float lineSize, int lineColor) {
        RenderUtils.drawRect(x, y, width, y + lineSize, lineColor);
        RenderUtils.drawRect(x, height - lineSize, width, height, lineColor);
        RenderUtils.drawRect(x, y + lineSize, x + lineSize, height - lineSize, lineColor);
        RenderUtils.drawRect(width - lineSize, y + lineSize, width, height - lineSize, lineColor);
    }

    public static void drawCircleWithTexture(float cX, float cY, int start, int end, float radius, ResourceLocation res, int color) {
        double radian, x, y, tx, ty, xsin, ycos;
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(res);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        color(color);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = start; i < end; ++i) {
            radian = i * (Math.PI / 180.0f);
            xsin = Math.sin(radian);
            ycos = Math.cos(radian);

            x = xsin * radius;
            y = ycos * radius;

            tx = xsin * 0.5 + 0.5;
            ty = ycos * 0.5 + 0.5;

            GL11.glTexCoord2d(cX + tx, cY + ty);
            GL11.glVertex2d(cX + x, cY + y);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GL11.glPopMatrix();

        GlStateManager.resetColor();
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        worldRenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldRenderer.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).endVertex();
        worldRenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }


    public static void drawRectBound(float left, float top, float width, float height, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, top + height, 0.0D).endVertex();
        worldrenderer.pos(left + width, top + height, 0.0D).endVertex();
        worldrenderer.pos(left + width, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void color(final double red, final double green, final double blue, final double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    public static void color(final double red, final double green, final double blue) {
        color(red, green, blue, 1);
    }

    public static void color(Color color) {
        if (color == null)
            color = Color.white;
        color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public static void color(Color color, final int alpha) {
        if (color == null)
            color = Color.white;
        color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 0.5);
    }


    public static void color(int color) {
        float f = (color >> 24 & 255) / 255.0f;
        float f1 = (color >> 16 & 255) / 255.0f;
        float f2 = (color >> 8 & 255) / 255.0f;
        float f3 = (color & 255) / 255.0f;
        GL11.glColor4f(f1, f2, f3, f);
    }

    public static void drawGradientRect(float left, float top, float right, float bottom, boolean sideways, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;

        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        if (sideways) {
            worldRenderer.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
            worldRenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
            worldRenderer.pos(left, bottom, 0).color(f1, f2, f3, f).endVertex();
            worldRenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        } else {
            worldRenderer.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
            worldRenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
            worldRenderer.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
            worldRenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        }

        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static int getRainbowOpaque(int index, float saturation, float brightness, float speed) {
        float hue = ((System.currentTimeMillis() + index) % ((int) speed)) / speed;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float edgeRadius, int color, float borderWidth, int borderColor) {
        if (color == 16777215) color = Colors.WHITE.c;
        if (borderColor == 16777215) borderColor = Colors.WHITE.c;

        if (edgeRadius < 0.0F) {
            edgeRadius = 0.0F;
        }

        if (edgeRadius > width / 2.0F) {
            edgeRadius = width / 2.0F;
        }

        if (edgeRadius > height / 2.0F) {
            edgeRadius = height / 2.0F;
        }

        drawRectBound(x + edgeRadius, y + edgeRadius, width - edgeRadius * 2.0F, height - edgeRadius * 2.0F, color);
        drawRectBound(x + edgeRadius, y, width - edgeRadius * 2.0F, edgeRadius, color);
        drawRectBound(x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2.0F, edgeRadius, color);
        drawRectBound(x, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0F, color);
        drawRectBound(x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0F, color);
        GlStateManager.enableBlend();
        GlStateManager.enableCull();
        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.0F);

        color(color);
        GL11.glBegin(6);
        float centerX = x + edgeRadius;
        float centerY = y + edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        int vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        int i;
        double angleRadians;
        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 270) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }
        GL11.glEnd();

        GL11.glBegin(6);
        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) i / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }
        GL11.glEnd();

        RenderUtils.color(borderColor);
        GL11.glLineWidth(borderWidth);
        GL11.glBegin(3);
        centerX = x + edgeRadius;
        centerY = y + edgeRadius;
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d(x + edgeRadius, y);
        GL11.glVertex2d(x + width - edgeRadius, y);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d(x + width, y + edgeRadius);
        GL11.glVertex2d(x + width, y + height - edgeRadius);
        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) i / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d(x + width - edgeRadius, y + height);
        GL11.glVertex2d(x + edgeRadius, y + height);
        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 270) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d(x, y + height - edgeRadius);
        GL11.glVertex2d(x, y + edgeRadius);
        GL11.glEnd();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.shadeModel(7424);

        GlStateManager.resetColor();
    }

    public static void drawAndRotateArrow(float x, float y, float size, boolean rotate) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0f);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0f);
        GL11.glDisable(3553);
        GL11.glBegin(4);

        if (rotate) {
            GL11.glVertex2f(size, (size / 2.0f));
            GL11.glVertex2f((size / 2.0f), 0.0f);
            GL11.glVertex2f(0.0f, (size / 2.0f));
        } else {
            GL11.glVertex2f(0.0f, 0.0f);
            GL11.glVertex2f((size / 2.0f), (size / 2.0f));
            GL11.glVertex2f(size, 0.0f);
        }

        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawTracer(float x, float y, float size, float widthDiv, float heightDiv, int color) {
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glPushMatrix();
        color(color);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x - size / widthDiv, y + size);
        GL11.glVertex2d(x, y + size / heightDiv);
        GL11.glVertex2d(x + size / widthDiv, y + size);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        GL11.glColor4f(0, 0, 0, 0.8f);
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        if (!blend)
            GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void drawRoundedRect(float left, float top, float right, float bottom, float radius, int color) {
        final int semicircle = 18;
        final float f = 90.0f / semicircle;
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        color(color);
        GL11.glBegin(5);
        GL11.glVertex2f(left + radius, top);
        GL11.glVertex2f(left + radius, bottom);
        GL11.glVertex2f(right - radius, top);
        GL11.glVertex2f(right - radius, bottom);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(left, top + radius);
        GL11.glVertex2f(left + radius, top + radius);
        GL11.glVertex2f(left, bottom - radius);
        GL11.glVertex2f(left + radius, bottom - radius);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(right, top + radius);
        GL11.glVertex2f(right - radius, top + radius);
        GL11.glVertex2f(right, bottom - radius);
        GL11.glVertex2f(right - radius, bottom - radius);
        GL11.glEnd();
        GL11.glBegin(6);
        float f6 = right - radius;
        float f7 = top + radius;
        GL11.glVertex2f(f6, f7);
        int j = 0;
        for (j = 0; j <= semicircle; ++j) {
            final float f8 = j * f;
            GL11.glVertex2d((f6 + radius * Math.cos(Math.toRadians(f8))),
                    (f7 - radius * Math.sin(Math.toRadians(f8))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = left + radius;
        f7 = top + radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f9 = j * f;
            GL11.glVertex2d((f6 - radius * Math.cos(Math.toRadians(f9))),
                    (f7 - radius * Math.sin(Math.toRadians(f9))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = left + radius;
        f7 = bottom - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f10 = j * f;
            GL11.glVertex2d((f6 - radius * Math.cos(Math.toRadians(f10))),
                    (f7 + radius * Math.sin(Math.toRadians(f10))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = right - radius;
        f7 = bottom - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f11 = j * f;
            GL11.glVertex2d((f6 + radius * Math.cos(Math.toRadians(f11))),
                    (f7 + radius * Math.sin(Math.toRadians(f11))));
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2884);
        GL11.glDisable(3042);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void drawBoundRoundedRect(float left, float top, float width, float height, float radius, int color) {
        float right = left + width;
        float bottom = top + height;

        final int semicircle = 18;
        final float f = 90.0f / semicircle;
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        color(color);
        GL11.glBegin(5);
        GL11.glVertex2f(left + radius, top);
        GL11.glVertex2f(left + radius, bottom);
        GL11.glVertex2f(right - radius, top);
        GL11.glVertex2f(right - radius, bottom);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(left, top + radius);
        GL11.glVertex2f(left + radius, top + radius);
        GL11.glVertex2f(left, bottom - radius);
        GL11.glVertex2f(left + radius, bottom - radius);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(right, top + radius);
        GL11.glVertex2f(right - radius, top + radius);
        GL11.glVertex2f(right, bottom - radius);
        GL11.glVertex2f(right - radius, bottom - radius);
        GL11.glEnd();
        GL11.glBegin(6);
        float f6 = right - radius;
        float f7 = top + radius;
        GL11.glVertex2f(f6, f7);
        int j = 0;
        for (j = 0; j <= semicircle; ++j) {
            final float f8 = j * f;
            GL11.glVertex2d((f6 + radius * Math.cos(Math.toRadians(f8))),
                    (f7 - radius * Math.sin(Math.toRadians(f8))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = left + radius;
        f7 = top + radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f9 = j * f;
            GL11.glVertex2d((f6 - radius * Math.cos(Math.toRadians(f9))),
                    (f7 - radius * Math.sin(Math.toRadians(f9))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = left + radius;
        f7 = bottom - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f10 = j * f;
            GL11.glVertex2d((f6 - radius * Math.cos(Math.toRadians(f10))),
                    (f7 + radius * Math.sin(Math.toRadians(f10))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = right - radius;
        f7 = bottom - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= semicircle; ++j) {
            final float f11 = j * f;
            GL11.glVertex2d((f6 + radius * Math.cos(Math.toRadians(f11))),
                    (f7 + radius * Math.sin(Math.toRadians(f11))));
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2884);
        GL11.glDisable(3042);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void circle(float x, float y, float radius, int fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void arc(float x, float y, float start, float end, float radius, int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(float x, float y, float start, float end, float w, float h, int color) {
        float temp;

        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }

        float var11 = (color >> 24 & 0xFF) / 255.0f;
        float var12 = (color >> 16 & 0xFF) / 255.0f;
        float var13 = (color >> 8 & 0xFF) / 255.0f;
        float var14 = (color & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);

        if (var11 > 0.5f) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (float i = end; i >= start; i -= 4.0f) {
                float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w * 1.001f;
                float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        for (float i = end; i >= start; i -= 4.0f) {
            float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w;
            float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void outlinedCircle(float x, float y, float start, float end, float radius, int color) {
        float temp;

        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }

        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (float i = end; i >= start; i -= 4.0f) {
            float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * radius * 1.001f;
            float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * radius * 1.001f;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static int darker(int hexColor, int factor) {
        float alpha = (float) (hexColor >> 24 & 255);
        float red = Math.max((float) (hexColor >> 16 & 255) - (float) (hexColor >> 16 & 255) / (100.0F / (float) factor), 0.0F);
        float green = Math.max((float) (hexColor >> 8 & 255) - (float) (hexColor >> 8 & 255) / (100.0F / (float) factor), 0.0F);
        float blue = Math.max((float) (hexColor & 255) - (float) (hexColor & 255) / (100.0F / (float) factor), 0.0F);
        return (int) ((float) (((int) alpha << 24) + ((int) red << 16) + ((int) green << 8)) + blue);
    }

    public static double[] convertTo2D(double x, double y, double z, Entity ent) {
        float pTicks = mc.timer.renderPartialTicks;
        float prevYaw = mc.thePlayer.rotationYaw;
        float prevPrevYaw = mc.thePlayer.prevRotationYaw;
        float[] rotations = getRotationFromPosition(ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks, ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks, ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * pTicks - 1.6D);
        mc.getRenderViewEntity().rotationYaw = (mc.getRenderViewEntity().prevRotationYaw = rotations[0]);
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        double[] convertedPoints = convertTo2D(x, y, z);
        mc.getRenderViewEntity().rotationYaw = prevYaw;
        mc.getRenderViewEntity().prevRotationYaw = prevPrevYaw;
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        return convertedPoints;
    }

    public static double[] convertTo2D(double x, double y, double z, BlockPos pos) {
        float pTicks = mc.timer.renderPartialTicks;
        float prevYaw = mc.thePlayer.rotationYaw;
        float prevPrevYaw = mc.thePlayer.prevRotationYaw;
        float[] rotations = getRotationFromPosition(pos.getX() + 0.5f, pos.getZ() + 0.5f, pos.getY() + 0.5f);
        mc.getRenderViewEntity().rotationYaw = (mc.getRenderViewEntity().prevRotationYaw = rotations[0]);
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        double[] convertedPoints = convertTo2D(x, y, z);
        mc.getRenderViewEntity().rotationYaw = prevYaw;
        mc.getRenderViewEntity().prevRotationYaw = prevPrevYaw;
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        return convertedPoints;
    }

    public static double[] convertTo2DPos(double x, double y, double z) {
        float pTicks = mc.timer.renderPartialTicks;
        float prevYaw = mc.thePlayer.rotationYaw;
        float prevPrevYaw = mc.thePlayer.prevRotationYaw;
        float[] rotations = getRotationFromPosition(x, z, y);
        mc.getRenderViewEntity().rotationYaw = (mc.getRenderViewEntity().prevRotationYaw = rotations[0]);
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        double[] convertedPoints = convertTo2D(x, y, z);
        mc.getRenderViewEntity().rotationYaw = prevYaw;
        mc.getRenderViewEntity().prevRotationYaw = prevPrevYaw;
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        return convertedPoints;
    }

    public static Vec3 vec3To2D(double x, double y, double z) {
        double[] c2D = convertTo2D(x, y, z);
        if (c2D != null) {
            return new Vec3(c2D[0], c2D[1], c2D[2]);
        }

        return null;
    }

    public static double[] convertTo2D(double x, double y, double z) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);

        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);

        boolean result = GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, screenCoords);

        if (result) {
            return new double[]{screenCoords.get(0), Display.getHeight() - screenCoords.get(1), screenCoords.get(2)};
        }

        return null;
    }

    public static float[] getRotationFromPosition(double x, double z, double y) {
        double xDiff = x - mc.thePlayer.posX;
        double zDiff = z - mc.thePlayer.posZ;
        double yDiff = y - mc.thePlayer.posY - 1.2;

        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
        return new float[]{yaw, pitch};
    }

    public static void drawBorderedRect(float x, float y, float width, float height, float borderWidth, Color rectColor, Color borderColor) {
        drawBorderedRect(x, y, width, height, borderWidth, rectColor.getRGB(), borderColor.getRGB());
    }

    public static void drawBorderedRect(float x, float y, float width, float height, float borderWidth, int rectColor, int borderColor) {
        drawRectBound(x + borderWidth, y + borderWidth, width - borderWidth * 2.0F, height - borderWidth * 2.0F, rectColor);
        drawRectBound(x, y, width, borderWidth, borderColor);
        drawRectBound(x, y + borderWidth, borderWidth, height - borderWidth, borderColor);
        drawRectBound(x + width - borderWidth, y + borderWidth, borderWidth, height - borderWidth, borderColor);
        drawRectBound(x + borderWidth, y + height - borderWidth, width - borderWidth * 2.0F, borderWidth, borderColor);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return framebuffer;
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, (float) (limit * .01));
    }

    private double[] convertTo2D(double x, double y, double z, double wX, double wY, double wZ) {
        float pTicks = mc.timer.renderPartialTicks;
        float prevYaw = mc.thePlayer.rotationYaw;
        float prevPrevYaw = mc.thePlayer.prevRotationYaw;
        float[] rotations = RotationUtils.getRotationFromPosition(wX, wZ, wY);
        mc.getRenderViewEntity().rotationYaw = (mc.getRenderViewEntity().prevRotationYaw = rotations[0]);
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        double[] convertedPoints = convertTo2D(x, y, z);
        mc.getRenderViewEntity().rotationYaw = prevYaw;
        mc.getRenderViewEntity().prevRotationYaw = prevPrevYaw;
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        return convertedPoints;
    }

    public static void drawSolidBlockESP(BlockPos pos, int color) {
        double xPos = pos.getX() - mc.getRenderManager().renderPosX, yPos = pos.getY() - mc.getRenderManager().renderPosY, zPos = pos.getZ() - mc.getRenderManager().renderPosZ;
        double height = mc.theWorld.getBlockState(pos).getBlock().getBlockBoundsMaxY() - mc.theWorld.getBlockState(pos).getBlock().getBlockBoundsMinY();
        float f = (float) (color >> 16 & 0xFF) / 255.0f;
        float f2 = (float) (color >> 8 & 0xFF) / 255.0f;
        float f3 = (float) (color & 0xFF) / 255.0f;
        float f4 = (float) (color >> 24 & 0xFF) / 255.0f;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(f, f2, f3, f4);
        drawOutlinedBoundingBox(new AxisAlignedBB(xPos, yPos, zPos, xPos + 1.0, yPos + height, zPos + 1.0));
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glDisable(3042);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(2929);
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB axisAlignedBB) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawLine(double x, double y, double z, double x1, double y1, double z1, final Color color, final float width) {
        x = x - mc.getRenderManager().renderPosX;
        x1 = x1 - mc.getRenderManager().renderPosX;
        y = y - mc.getRenderManager().renderPosY;
        y1 = y1 - mc.getRenderManager().renderPosY;
        z = z - mc.getRenderManager().renderPosZ;
        z1 = z1 - mc.getRenderManager().renderPosZ;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(width);

        color(color);
        GL11.glBegin(2);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }


    public static void drawLine(double posX, double posY, double posZ, int color) {
        double renderPosXDelta = posX - mc.getRenderManager().renderPosX;
        double renderPosYDelta = posY - mc.getRenderManager().renderPosY;
        double renderPosZDelta = posZ - mc.getRenderManager().renderPosZ;

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0F);
        float f = (float) (color >> 16 & 0xFF) / 255.0f;
        float f2 = (float) (color >> 8 & 0xFF) / 255.0f;
        float f3 = (float) (color & 0xFF) / 255.0f;
        float f4 = (float) (color >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f(f, f2, f3, f4);
        GL11.glLoadIdentity();
        boolean previousState = mc.gameSettings.viewBobbing;
        mc.gameSettings.viewBobbing = false;
        mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);
        GL11.glBegin(3);
        GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glEnd();
        mc.gameSettings.viewBobbing = previousState;
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void drawLine(BlockPos blockPos, int color) {
        double renderPosXDelta = blockPos.getX() - mc.getRenderManager().renderPosX + 0.5D;
        double renderPosYDelta = blockPos.getY() - mc.getRenderManager().renderPosY + 0.5D;
        double renderPosZDelta = blockPos.getZ() - mc.getRenderManager().renderPosZ + 0.5D;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0F);
        float f = (float) (color >> 16 & 0xFF) / 255.0f;
        float f2 = (float) (color >> 8 & 0xFF) / 255.0f;
        float f3 = (float) (color & 0xFF) / 255.0f;
        float f4 = (float) (color >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f(f, f2, f3, f4);
        GL11.glLoadIdentity();
        boolean previousState = mc.gameSettings.viewBobbing;
        mc.gameSettings.viewBobbing = false;
        mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);
        GL11.glBegin(3);
        GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glEnd();
        mc.gameSettings.viewBobbing = previousState;
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawLine(double eyeHeight, Vec3 to, int color) {
        double renderPosXDelta = to.xCoord - mc.getRenderManager().renderPosX;
        double renderPosYDelta = to.yCoord - mc.getRenderManager().renderPosY;
        double renderPosZDelta = to.zCoord - mc.getRenderManager().renderPosZ;

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0F);
        float f = (float) (color >> 16 & 0xFF) / 255.0f;
        float f2 = (float) (color >> 8 & 0xFF) / 255.0f;
        float f3 = (float) (color & 0xFF) / 255.0f;
        float f4 = (float) (color >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f(f, f2, f3, f4);
        GL11.glLoadIdentity();
        boolean previousState = mc.gameSettings.viewBobbing;
        mc.gameSettings.viewBobbing = false;
        mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);
        GL11.glBegin(3);

        GL11.glVertex3d(0, eyeHeight, 0);

        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glEnd();
        mc.gameSettings.viewBobbing = previousState;
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }
}
