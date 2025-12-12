package moe.ichinomiya.naven.ui.watermark;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.Version;
import moe.ichinomiya.naven.utils.Colors;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.font.FontManager;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
//import dev.yalan.live.LiveClient;
import net.minecraft.client.Minecraft;
import tech.skidonion.obfuscator.annotations.ControlFlowObfuscation;

@ControlFlowObfuscation
public class SilenceFixWaterMark extends WaterMark {
    int width;

    public void render() {
        if (Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        BlinkFix blinkFix = BlinkFix.getInstance();

        int color1 = RenderUtils.getRainbowOpaque(0, 0.5f, 1f, 3000);
        int color2 = RenderUtils.getRainbowOpaque(2000, 0.5f, 1f, 3000);
        RenderUtils.drawGradientRect(5, 5, width, 6, true, color1, color2);

        RenderUtils.drawRect(5, 6, width, 23, Colors.getColor(0, 0, 0, 60));

        FontManager fontManager = blinkFix.getFontManager();
        GlyphPageFontRenderer font = fontManager.regular18;
        GlyphPageFontRenderer smallFont = fontManager.regular16;

        char[] title = BlinkFix.CLIENT_DISPLAY_NAME.toCharArray();
        float titleWidth = 0;

        for (int i = 0; i < title.length; i++) {
            char c = title[i];

            font.drawString(String.valueOf(c), 7.5f + titleWidth, 9.5f, 0x000000);
            font.drawString(String.valueOf(c), 7 + titleWidth, 9, RenderUtils.getRainbowOpaque(i * -100, 0.5f, 1f, 5000));
            titleWidth += font.getCharWidth(c);
        }

        String serverIP = Minecraft.getMinecraft().getCurrentServerData() == null ? "Local"  : Minecraft.getMinecraft().getCurrentServerData().serverIP;
        String text = "| " + Version.getClientVersion() +  " | User: " + "LiveClient.INSTANCE.autoUsername" + " | " + Minecraft.getDebugFPS() + "FPS | " + serverIP;
        smallFont.drawStringWithShadow(text, 9f + titleWidth, 9.5f, Colors.WHITE.c);

        width = (int) (titleWidth + smallFont.getStringWidth(text) + 14);
    }

    public void renderShader() {
        if (Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        RenderUtils.drawRect(5, 6, width, 23, 0xFFFFFFFF);
    }
}
