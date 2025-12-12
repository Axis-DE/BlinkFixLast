package moe.ichinomiya.naven.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class DualBlurUtils {
    private final static Minecraft mc = Minecraft.getMinecraft();

    public static ShaderUtils dualBlurUp = new ShaderUtils("dualBlurUp");
    public static ShaderUtils dualBlurDown = new ShaderUtils("dualBlurDown");

    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    private static int currentIterations;
    private static final List<Framebuffer> framebufferList = new ArrayList<>();

    private static void initFramebuffer(float iterations) {
        for(Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }

        framebufferList.clear();

        framebufferList.add(RenderUtils.createFrameBuffer(framebuffer));

        for(int i = 1; i <= iterations; i++) {
            Framebuffer framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
            framebufferList.add(RenderUtils.createFrameBuffer(framebuffer));
        }
    }

    public static void renderBlur(int iterations, int offset, boolean fresh) {
        if (currentIterations != iterations) {
            initFramebuffer(iterations);
            currentIterations = iterations;
        }

        if (fresh) {
            renderFBO(framebufferList.get(1), mc.getFramebuffer().framebufferTexture, dualBlurDown, offset);

            //下采样 Down sample
            for (int i = 1; i < iterations; i++) {
                renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, dualBlurDown, offset);
            }

            //上采样 Up sample
            for (int i = iterations; i > 1; i--) {
                renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, dualBlurUp, offset);
            }

            mc.getFramebuffer().bindFramebuffer(true);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferList.get(1).framebufferTexture);

        dualBlurUp.init();
        dualBlurUp.setUniformf("offset", offset, offset);
        dualBlurUp.setUniformf("halfpixel", 0.5f / mc.displayWidth, 0.5f / mc.displayHeight);
        dualBlurUp.setUniformi("inTexture", 0);
        ShaderUtils.drawQuads();
        dualBlurUp.unload();
    }

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtils shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        shader.init();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformf("halfpixel", 1f / mc.displayWidth, 1f / mc.displayHeight);
        ShaderUtils.drawQuads();
        shader.unload();
        framebuffer.unbindFramebuffer();
    }
}
