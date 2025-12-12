package moe.ichinomiya.naven.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;

import java.io.*;
import java.util.Base64;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final int programID;

    public ShaderUtils(String fragmentShaderLoc) {
        int program = glCreateProgram();

        try {
            int fragmentShaderID;
            switch (fragmentShaderLoc) {
                case "dualBlurUp":
                    fragmentShaderID = createShader(new ByteArrayInputStream(Base64.getDecoder().decode(dualBlurUp)), GL_FRAGMENT_SHADER);
                    break;

                case "dualBlurDown":
                    fragmentShaderID = createShader(new ByteArrayInputStream(Base64.getDecoder().decode(dualBlurDown)), GL_FRAGMENT_SHADER);
                    break;

                case "roundedRect":
                    fragmentShaderID = createShader(new ByteArrayInputStream(Base64.getDecoder().decode(roundedRect)), GL_FRAGMENT_SHADER);
                    break;

                case "dropShadow":
                    fragmentShaderID = createShader(new ByteArrayInputStream(Base64.getDecoder().decode(dropShadow)), GL_FRAGMENT_SHADER);
                    break;

                default:
                    throw new IOException();
            }
            glAttachShader(program, fragmentShaderID);

            int vertexShaderID = createShader(new ByteArrayInputStream(Base64.getDecoder().decode(vertex)), GL_VERTEX_SHADER);
            glAttachShader(program, vertexShaderID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);

        if (status == 0) {
            throw new IllegalStateException("Shader failed to link!");
        }

        this.programID = program;
    }

    public void init() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    public int getUniform(String name) {
        return glGetUniformLocation(programID, name);
    }


    public void setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1:
                glUniform1f(loc, args[0]);
                break;
            case 2:
                glUniform2f(loc, args[0], args[1]);
                break;
            case 3:
                glUniform3f(loc, args[0], args[1], args[2]);
                break;
            case 4:
                glUniform4f(loc, args[0], args[1], args[2], args[3]);
                break;
        }
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public void setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programID, name);
        if (args.length > 1) glUniform2i(loc, args[0], args[1]);
        else glUniform1i(loc, args[0]);
    }

    public static void drawQuads(float x, float y, float width, float height) {
        if (mc.gameSettings.ofFastRender) return;

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        glEnd();
    }

    public static void drawQuads() {
        if (mc.gameSettings.ofFastRender) return;

        ScaledResolution sr = new ScaledResolution(mc);
        float width = (float) sr.getScaledWidth_double();
        float height = (float) sr.getScaledHeight_double();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, readInputStream(inputStream));
        glCompileShader(shader);


        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.out.println(glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
        }

        try {
            inputStream.close();
        } catch (Exception ignored) {
            // Failed to close the stream, ignoring
        }

        return shader;
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static boolean isSupportGLSL() {
        return OpenGlHelper.shadersSupported && mc.getRenderViewEntity() instanceof EntityPlayer && !mc.gameSettings.ofFastRender;
    }

    public static final String dualBlurUp = "I3ZlcnNpb24gMTIwCgp1bmlmb3JtIHNhbXBsZXIyRCBpblRleHR1cmU7CnVuaWZvcm0gdmVjMiBoYWxmcGl4ZWwsIG9mZnNldDsKCnZvaWQgbWFpbigpIHsKICAgIHZlYzQgc3VtID0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QgKyB2ZWMyKC1oYWxmcGl4ZWwueCAqIDIuMCwgMC4wKSAqIG9mZnNldCk7CiAgICBzdW0gKz0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QgKyB2ZWMyKC1oYWxmcGl4ZWwueCwgaGFsZnBpeGVsLnkpICogb2Zmc2V0KSAqIDIuMDsKICAgIHN1bSArPSB0ZXh0dXJlMkQoaW5UZXh0dXJlLCBnbF9UZXhDb29yZFswXS5zdCArIHZlYzIoMC4wLCBoYWxmcGl4ZWwueSAqIDIuMCkgKiBvZmZzZXQpOwogICAgc3VtICs9IHRleHR1cmUyRChpblRleHR1cmUsIGdsX1RleENvb3JkWzBdLnN0ICsgdmVjMihoYWxmcGl4ZWwueCwgaGFsZnBpeGVsLnkpICogb2Zmc2V0KSAqIDIuMDsKICAgIHN1bSArPSB0ZXh0dXJlMkQoaW5UZXh0dXJlLCBnbF9UZXhDb29yZFswXS5zdCArIHZlYzIoaGFsZnBpeGVsLnggKiAyLjAsIDAuMCkgKiBvZmZzZXQpOwogICAgc3VtICs9IHRleHR1cmUyRChpblRleHR1cmUsIGdsX1RleENvb3JkWzBdLnN0ICsgdmVjMihoYWxmcGl4ZWwueCwgLWhhbGZwaXhlbC55KSAqIG9mZnNldCkgKiAyLjA7CiAgICBzdW0gKz0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QgKyB2ZWMyKDAuMCwgLWhhbGZwaXhlbC55ICogMi4wKSAqIG9mZnNldCk7CiAgICBzdW0gKz0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QgKyB2ZWMyKC1oYWxmcGl4ZWwueCwgLWhhbGZwaXhlbC55KSAqIG9mZnNldCkgKiAyLjA7CgogICAgZ2xfRnJhZ0NvbG9yID0gdmVjNChzdW0ucmdiIC8gMTIuMCwgMS4wKTsKfQo=";
    public static final String dualBlurDown = "I3ZlcnNpb24gMTIwCgp1bmlmb3JtIHNhbXBsZXIyRCBpblRleHR1cmU7CnVuaWZvcm0gdmVjMiBvZmZzZXQsIGhhbGZwaXhlbDsKCnZvaWQgbWFpbigpIHsKICAgIHZlYzQgc3VtID0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QpICogNC4wOwogICAgc3VtICs9IHRleHR1cmUyRChpblRleHR1cmUsIGdsX1RleENvb3JkWzBdLnN0IC0gaGFsZnBpeGVsLnh5ICogb2Zmc2V0KTsKICAgIHN1bSArPSB0ZXh0dXJlMkQoaW5UZXh0dXJlLCBnbF9UZXhDb29yZFswXS5zdCArIGhhbGZwaXhlbC54eSAqIG9mZnNldCk7CiAgICBzdW0gKz0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QgKyB2ZWMyKGhhbGZwaXhlbC54LCAtaGFsZnBpeGVsLnkpICogb2Zmc2V0KTsKICAgIHN1bSArPSB0ZXh0dXJlMkQoaW5UZXh0dXJlLCBnbF9UZXhDb29yZFswXS5zdCAtIHZlYzIoaGFsZnBpeGVsLngsIC1oYWxmcGl4ZWwueSkgKiBvZmZzZXQpOwogICAgZ2xfRnJhZ0NvbG9yID0gdmVjNChzdW0ucmdiIC8gOC4wLCAxLjApOwp9Cg==";

    public static final String dropShadow = "I3ZlcnNpb24gMTIwCgp1bmlmb3JtIHNhbXBsZXIyRCBpblRleHR1cmUsIHRleHR1cmVUb0NoZWNrOwp1bmlmb3JtIHZlYzIgdGV4ZWxTaXplLCBkaXJlY3Rpb247CnVuaWZvcm0gZmxvYXQgcmFkaXVzOwp1bmlmb3JtIGZsb2F0IHdlaWdodHNbMjU2XTsKCiNkZWZpbmUgb2Zmc2V0IHRleGVsU2l6ZSAqIGRpcmVjdGlvbgoKdm9pZCBtYWluKCkgewogICAgaWYgKGRpcmVjdGlvbi55ID4gMCAmJiB0ZXh0dXJlMkQodGV4dHVyZVRvQ2hlY2ssIGdsX1RleENvb3JkWzBdLnN0KS5hICE9IDAuMCkgZGlzY2FyZDsKICAgIGZsb2F0IGJsciA9IHRleHR1cmUyRChpblRleHR1cmUsIGdsX1RleENvb3JkWzBdLnN0KS5hICogd2VpZ2h0c1swXTsKCiAgICBmb3IgKGZsb2F0IGYgPSAxLjA7IGYgPD0gcmFkaXVzOyBmKyspIHsKICAgICAgICBibHIgKz0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QgKyBmICogb2Zmc2V0KS5hICogKHdlaWdodHNbaW50KGFicyhmKSldKTsKICAgICAgICBibHIgKz0gdGV4dHVyZTJEKGluVGV4dHVyZSwgZ2xfVGV4Q29vcmRbMF0uc3QgLSBmICogb2Zmc2V0KS5hICogKHdlaWdodHNbaW50KGFicyhmKSldKTsKICAgIH0KCiAgICBnbF9GcmFnQ29sb3IgPSB2ZWM0KDAuMCwgMC4wLCAwLjAsIGJscik7Cn0=";
    public static final String roundedRect = "I3ZlcnNpb24gMTIwCgp1bmlmb3JtIHZlYzIgbG9jYXRpb24sIHJlY3RTaXplOwp1bmlmb3JtIHZlYzQgY29sb3I7CnVuaWZvcm0gZmxvYXQgcmFkaXVzOwp1bmlmb3JtIGJvb2wgYmx1cjsKCmZsb2F0IHJvdW5kU0RGKHZlYzIgcCwgdmVjMiBiLCBmbG9hdCByKSB7CiAgICByZXR1cm4gbGVuZ3RoKG1heChhYnMocCkgLSBiLCAwLjApKSAtIHI7Cn0KCnZvaWQgbWFpbigpIHsKICAgIHZlYzIgcmVjdEhhbGYgPSByZWN0U2l6ZSAqIC41OwogICAgZmxvYXQgc21vb3RoZWRBbHBoYSA9ICAoMS4wIC0gc21vb3Roc3RlcCgwLjAsIDEuMCwgcm91bmRTREYocmVjdEhhbGYgLSAoZ2xfVGV4Q29vcmRbMF0uc3QgKiByZWN0U2l6ZSksIHJlY3RIYWxmIC0gcmFkaXVzIC0gMS4sIHJhZGl1cykpKSAqIGNvbG9yLmE7CiAgICBnbF9GcmFnQ29sb3IgPSB2ZWM0KGNvbG9yLnJnYiwgc21vb3RoZWRBbHBoYSk7Cn0=";

    public static final String vertex = "I3ZlcnNpb24gMTIwCgp2b2lkIG1haW4oKSB7CiAgICBnbF9UZXhDb29yZFswXSA9IGdsX011bHRpVGV4Q29vcmQwOwogICAgZ2xfUG9zaXRpb24gPSBnbF9Nb2RlbFZpZXdQcm9qZWN0aW9uTWF0cml4ICogZ2xfVmVydGV4Owp9";
}
