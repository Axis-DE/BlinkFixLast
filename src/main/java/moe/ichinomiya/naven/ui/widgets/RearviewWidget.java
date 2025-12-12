package moe.ichinomiya.naven.ui.widgets;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventRender2D;
import moe.ichinomiya.naven.events.impl.EventRenderTick;
import moe.ichinomiya.naven.utils.RenderGlobalHelper;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

public class RearviewWidget extends DraggableWidget {
    private final RenderGlobalHelper mirrorRenderGlobal = new RenderGlobalHelper();
    private final BooleanValue value;
    int width = 16 * 20, height = 9 * 20;

    private final TimeHelper timer = new TimeHelper();
    private final int mirrorFBO;
    private final int mirrorTex;
    private final int mirrorDepth;
    private boolean firstUpdate;
    private long renderEndNanoTime;

    public RearviewWidget(BooleanValue value) {
        super("Rearview");
        this.value = value;

        mirrorFBO = ARBFramebufferObject.glGenFramebuffers();
        mirrorTex = GL11.glGenTextures();
        mirrorDepth = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorTex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, width, height, 0, GL11.GL_RGBA, GL11.GL_INT, (IntBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorDepth);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_INT, (IntBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        renderEndNanoTime = mc.entityRenderer.getRenderEndNanoTime();
    }


    @EventTarget
    public void onRender(EventRenderTick e) {
        if (e.getType() == EventType.POST && mc.getRenderViewEntity() != null && shouldRender()) {
            if (timer.delay(1000 / 30F)) {
                timer.reset();
                updateMirror();
            }
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D e) {
        if (shouldRender()) {
            float x = getX().value;
            float y = getY().value;
            GlStateManager.pushMatrix();

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

            ScaledResolution resolution = e.getResolution();
            int factor = resolution.getScaleFactor();

            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_POLYGON_BIT | GL11.GL_TEXTURE_BIT);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0, mc.displayWidth, mc.displayHeight, 0, 1000, 3000);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glTranslatef(0, 0, -2000);

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);

            GL11.glColor3ub((byte) 255, (byte) 255, (byte) 255);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mirrorTex);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(x * factor, y * factor + height, 0).tex(1, 0).endVertex();
            worldRenderer.pos(x * factor + width, y * factor + height, 0).tex(0, 0).endVertex();
            worldRenderer.pos(x * factor + width, y * factor, 0).tex(0, 1).endVertex();
            worldRenderer.pos(x * factor, y * factor, 0).tex(1, 1).endVertex();
            tessellator.draw();

            GlStateManager.bindTexture(0);
            GL11.glPopAttrib();

            mc.entityRenderer.setupOverlayRendering();
            GlStateManager.popMatrix();
        }
    }

    private void updateMirror() {
        int width = mc.displayWidth, height = mc.displayHeight;
        boolean hide = mc.gameSettings.hideGUI;
        int view = mc.gameSettings.thirdPersonView, limit = mc.gameSettings.limitFramerate;
        long endTime = 0;
        float fov = mc.gameSettings.fovSetting;

        Entity renderViewEntity = mc.getRenderViewEntity();

        GuiScreen currentScreen = mc.currentScreen;
        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers();
            this.firstUpdate = true;
        }
        switchToFB();

        if (limit != 0) {
            endTime = renderEndNanoTime;
        }

        renderViewEntity.rotationYaw += 180;
        renderViewEntity.prevRotationYaw += 180;

        mc.currentScreen = null;
        mc.displayWidth = this.width;
        mc.displayHeight = this.height;
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.thirdPersonView = 0;
        mc.gameSettings.limitFramerate = 0;

        mc.gameSettings.fovSetting = 90;

        mirrorRenderGlobal.switchTo();

        GL11.glPushAttrib(272393);

        mc.entityRenderer.renderWorld(mc.timer.renderPartialTicks, System.nanoTime());
        mc.entityRenderer.setupOverlayRendering();

        if (limit != 0) {
            renderEndNanoTime = endTime;
        }

        GL11.glPopAttrib();

        mirrorRenderGlobal.switchFrom();

        mc.setRenderViewEntity(renderViewEntity);

        renderViewEntity.rotationYaw -= 180;
        renderViewEntity.prevRotationYaw -= 180;
        mc.currentScreen = currentScreen;
        mc.gameSettings.limitFramerate = limit;
        mc.gameSettings.thirdPersonView = view;
        mc.gameSettings.hideGUI = hide;
        mc.displayWidth = width;
        mc.displayHeight = height;
        mc.gameSettings.fovSetting = fov;

        switchFromFB();
    }

    private void switchToFB() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();

        OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, mirrorFBO);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, mirrorTex, 0);
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, mirrorDepth, 0);
    }

    private void switchFromFB() {
        OpenGlHelper.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0);
    }

    @Override
    public void renderBackground() {

    }

    @Override
    public void renderBody() {

    }

    @Override
    public float getWidth() {
        return width / 2f;
    }

    @Override
    public float getHeight() {
        return height / 2f;
    }

    @Override
    public boolean shouldRender() {
        return value.getCurrentValue();
    }
}
