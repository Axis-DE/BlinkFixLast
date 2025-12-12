package moe.ichinomiya.naven.ui.widgets;

import lombok.Getter;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.StencilUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;

public abstract class DraggableWidget {
    protected final static Minecraft mc = Minecraft.getMinecraft();
    private final static int headerColor = new Color(150, 45, 45, 255).getRGB();
    private final static int bodyColor = new Color(0, 0, 0, 190).getRGB();

    private boolean isDragging = false;
    @Getter
    protected final String name;
    @Getter
    protected SmoothAnimationTimer x = new SmoothAnimationTimer(0, 0.5f), y = new SmoothAnimationTimer(0, 0.5f);
    protected float dragX, dragY;

    public DraggableWidget(String name) {
        this.name = name;
    }

    public void renderShader() {
        if (shouldRender()) {
            RenderUtils.drawBoundRoundedRect(0, 0, getWidth(), getHeight(), 5, 0xFFFFFFFF);
        }
    }

    public void renderBackground() {
        RenderUtils.drawRectBound(0, 0, getWidth(), 3, headerColor);
        RenderUtils.drawRectBound(0, 3, getWidth(), getHeight(), bodyColor);
    }

    public void render() {
        if (shouldRender()) {
            this.x.update(true);
            this.y.update(true);

            if (isDragging && Mouse.isButtonDown(0)) {
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                float x = Mouse.getEventX() * scaledResolution.getScaledWidth() / (float) mc.displayWidth;
                float y = scaledResolution.getScaledHeight() - Mouse.getEventY() * scaledResolution.getScaledHeight() / (float) mc.displayHeight - 1;

                this.x.target = x - dragX;
                this.y.target = y - dragY;
            } else {
                isDragging = false;
            }
            StencilUtils.write(false);
            RenderUtils.drawBoundRoundedRect(0, 0, getWidth(), getHeight(), 5f, 0xFFFFFFFF);

            StencilUtils.erase(true);
            renderBackground();
            renderBody();
            StencilUtils.dispose();
        }
    }

    public abstract void renderBody();
    public abstract float getWidth();
    public abstract float getHeight();
    public abstract boolean shouldRender();

    public void processDrag(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && shouldRender()) {
            if (RenderUtils.isHoveringBound(mouseX, mouseY, x.value, y.value, getWidth(), getHeight())) {
                isDragging = true;
                dragX = mouseX - x.value;
                dragY = mouseY - y.value;
            }
        }
    }
}
