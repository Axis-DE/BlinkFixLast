package moe.ichinomiya.naven.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;

public class DropShadowUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final ShaderUtils gaussianBloom = new ShaderUtils("dropShadow");
    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    public static void renderDropShadow(int sourceTexture, int radius, int offset, boolean fresh) {
        if (!ShaderUtils.isSupportGLSL()) return;
        if (fresh) framebuffer = RenderUtils.createFrameBuffer(framebuffer);

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        GlStateManager.enableBlend();

        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(MathUtils.calculateGaussianValue(i, radius));
        }
        weightBuffer.rewind();

        if (fresh) {
            RenderUtils.setAlphaLimit(0.0F);

            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            gaussianBloom.init();
            setupUniforms(radius, offset, 0, weightBuffer);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
            ShaderUtils.drawQuads();
            gaussianBloom.unload();
            framebuffer.unbindFramebuffer();
            mc.getFramebuffer().bindFramebuffer(true);
        }

        gaussianBloom.init();
        if (fresh) {
            setupUniforms(radius, 0, offset, weightBuffer);
            GL13.glActiveTexture(GL13.GL_TEXTURE16);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBloom.unload();

        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.enableAlpha();

        GlStateManager.bindTexture(0);
    }

    public static void setupUniforms(int radius, int directionX, int directionY, FloatBuffer weights) {
        gaussianBloom.setUniformi("inTexture", 0);
        gaussianBloom.setUniformi("textureToCheck", 16);
        gaussianBloom.setUniformf("radius", radius);
        gaussianBloom.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        gaussianBloom.setUniformf("direction", directionX, directionY);
        OpenGlHelper.glUniform1(gaussianBloom.getUniform("weights"), weights);
    }
}
