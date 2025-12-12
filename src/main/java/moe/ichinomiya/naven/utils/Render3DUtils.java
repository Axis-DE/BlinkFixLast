package moe.ichinomiya.naven.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class Render3DUtils {
    private final static Minecraft mc = Minecraft.getMinecraft();

    public static void drawOutlinedEntityESP(double x, double y, double z, double width, double height, int color) {
        GlStateManager.pushMatrix();
        Render3DUtils.pre3D();
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

        RenderUtils.glColor(color);
        drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));

        Render3DUtils.post3D();
        GlStateManager.popMatrix();
    }

    public static void drawSoiledEntityESP(AxisAlignedBB bb, int color) {
        GlStateManager.pushMatrix();
        Render3DUtils.pre3D();
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

        RenderUtils.glColor(color);
        drawBoundingBox(bb.offset(- mc.getRenderManager().renderPosX, - mc.getRenderManager().renderPosY, - mc.getRenderManager().renderPosZ));

        Render3DUtils.post3D();
        GlStateManager.popMatrix();
    }

    public static void drawSoiledEntityESP(double x, double y, double z, double width, double height, int color) {
        GlStateManager.pushMatrix();
        Render3DUtils.pre3D();
        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

        RenderUtils.glColor(color);
        drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));

        Render3DUtils.post3D();
        GlStateManager.popMatrix();
    }

    public static void drawESP(Vec3 vec, Vec3 vec2, int color) {
        drawESP(vec, vec2, color, false);
    }

    public static void drawESP(Vec3 vec, Vec3 vec2, int color, boolean viewBobbing) {
        GlStateManager.pushMatrix();
        Render3DUtils.pre3D();
        boolean previousState = mc.gameSettings.viewBobbing;

        if (viewBobbing) {
            mc.gameSettings.viewBobbing = false;
        }

        mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

        RenderUtils.glColor(color);
        Render3DUtils.drawBoundingBox(new AxisAlignedBB(vec.xCoord - mc.getRenderManager().renderPosX, vec.yCoord - mc.getRenderManager().renderPosY, vec.zCoord - mc.getRenderManager().renderPosZ, vec2.xCoord - mc.getRenderManager().renderPosX, vec2.yCoord - mc.getRenderManager().renderPosY, vec2.zCoord - mc.getRenderManager().renderPosZ));

        Render3DUtils.post3D();
        if (viewBobbing) {
            mc.gameSettings.viewBobbing = previousState;
        }
        GlStateManager.popMatrix();
    }

    public static void pre3D() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void post3D() {
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();

        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();

        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();

        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();

        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();

        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();

        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();

        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }



    public static void drawOutlinedBlockESP(final double x, final double y, final double z, final float red, final float green, final float blue,
                                            final float alpha, final float lineWidth) {

        drawOutlinedBlockESP(x, y, z, red, green, blue, alpha, lineWidth, 1f);
    }

    public static void drawOutlinedBlockESP(final double x, final double y, final double z, final float red, final float green, final float blue,
                                            final float alpha, final float lineWidth, float height) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(red, green, blue, alpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1D, y + height, z + 1D));
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static void drawBlockESP(final double x, final double y, final double z, final float red, final float green, final float blue, final float alpha,
                                    final float lineRed, final float lineGreen, final float lineBlue, final float lineAlpha, final float lineWidth) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1D, y + 1D, z + 1D));
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha);
        drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1D, y + 1D, z + 1D));
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawSolidBlockESP(final double x, final double y, final double z, final float red, final float green, final float blue, final float alpha, float height) {
        GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1D, y + height, z + 1D));

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static void drawSolidBoundingBox(AxisAlignedBB bb, final float red, final float green, final float blue, final float alpha) {
        GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(bb.offset(- mc.getRenderManager().renderPosX, - mc.getRenderManager().renderPosY, - mc.getRenderManager().renderPosZ));

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static void drawSolidBlockESP(final double x, final double y, final double z, final float red, final float green, final float blue, final float alpha, float width, float height) {
        GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x + width, y + height, z + width));

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static void drawSolidBlockESP(final double x, final double y, final double z, final float red, final float green, final float blue, final float alpha) {
        drawSolidBlockESP(x, y, z, red, green, blue, alpha, 1);
    }

    public static void drawSolidBlockESP(BlockPos blockPos, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        drawSolidBlockESP(blockPos.getX() - mc.getRenderManager().renderPosX, blockPos.getY() - mc.getRenderManager().renderPosY, blockPos.getZ() - mc.getRenderManager().renderPosZ, f, f1, f2, f3);
    }

    public static void drawSolidBlockESP(BlockPos blockPos, int color, float height) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        drawSolidBlockESP(blockPos.getX() - mc.getRenderManager().renderPosX, blockPos.getY() - mc.getRenderManager().renderPosY, blockPos.getZ() - mc.getRenderManager().renderPosZ, f, f1, f2, f3, height);
    }

    public static void drawSolidBlockESP(BlockPos blockPos, int color, float width, float height) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        drawSolidBlockESP(blockPos.getX() - mc.getRenderManager().renderPosX, blockPos.getY() - mc.getRenderManager().renderPosY, blockPos.getZ() - mc.getRenderManager().renderPosZ, f, f1, f2, f3, width, height);
    }

    public static void drawSolidBlockESP(BlockPos blockPos, final float red, final float green, final float blue,
                                         final float alpha) {
        drawSolidBlockESP(blockPos.getX() - mc.getRenderManager().renderPosX, blockPos.getY() - mc.getRenderManager().renderPosY, blockPos.getZ() - mc.getRenderManager().renderPosZ, red, green, blue, alpha);
    }

    public static void drawOutlinedBlockESP(final double x, final double y, final double z, final float red, final float green, final float blue, final float alpha) {
        drawOutlinedBlockESP(x, y, z, red, green, blue, alpha, 1);
    }

    public static void drawOutlinedBlockESP(BlockPos blockPos, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        drawOutlinedBlockESP(blockPos.getX() - mc.getRenderManager().renderPosX, blockPos.getY() - mc.getRenderManager().renderPosY, blockPos.getZ() - mc.getRenderManager().renderPosZ, f, f1, f2, f3);
    }

    public static void drawOutlinedBlockESP(BlockPos blockPos, int color, float height) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        drawOutlinedBlockESP(blockPos.getX() - mc.getRenderManager().renderPosX, blockPos.getY() - mc.getRenderManager().renderPosY, blockPos.getZ() - mc.getRenderManager().renderPosZ, f, f1, f2, f3, 1f, height);
    }

    public static void drawOutlinedBlockESP(BlockPos blockPos, final float red, final float green, final float blue,
                                         final float alpha) {
        drawOutlinedBlockESP(blockPos.getX() - mc.getRenderManager().renderPosX, blockPos.getY() - mc.getRenderManager().renderPosY, blockPos.getZ() - mc.getRenderManager().renderPosZ, red, green, blue, alpha);
    }
}
