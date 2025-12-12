package net.minecraft.client.gui;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.ui.AltManager.GuiAltMenu;
import moe.ichinomiya.naven.ui.button.MainMenuButton;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.utils.font.FontManager;
//import dev.yalan.live.LiveClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.FrameGrabber;

import java.io.IOException;
import java.util.*;

import static moe.ichinomiya.naven.BlinkFix.CLIENT_DISPLAY_NAME;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    private static final Logger logger = LogManager.getLogger();

    int backgroundWidth = 2781, backgroundHeight = 1564;

    public GuiMainMenu() {

    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    List<MainMenuButton> buttons = new ArrayList<>();

    public void initGui() {
//        if (LiveClient.INSTANCE.loginIndex1.get() + LiveClient.INSTANCE.loginIndex2.get() != 20250805) {
//            mc.shutdown();
//            return;
//        }
        buttons.clear();
        buttons.add(new MainMenuButton(1, "Single Player", () -> mc.displayGuiScreen(new GuiSelectWorld(this))));
        buttons.add(new MainMenuButton(2, "Multi Player", () -> mc.displayGuiScreen(new GuiMultiplayer(this))));
        buttons.add(new MainMenuButton(3, "Alt Manager", () -> mc.displayGuiScreen(new GuiAltMenu())));
        buttons.add(new MainMenuButton(4, "Options", () -> mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings))));
        buttons.add(new MainMenuButton(5, "Quit", () -> mc.shutdown()));
    }

    public void confirmClicked(boolean result, int id) {

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Render Background Image
        int imageWidth = this.width, imageHeight = this.height;
        float scale = Math.max((float) imageWidth / (float) backgroundWidth, (float) imageHeight / (float) backgroundHeight);
        imageWidth = (int) (backgroundWidth * scale);
        imageHeight = (int) (backgroundHeight * scale);
        int imageX = (this.width - imageWidth) / 2, imageY = (this.height - imageHeight) / 2;
        //RenderUtils.drawImage(new ResourceLocation("SilenceFix/background.png"), imageX, imageY, imageWidth, imageHeight, 0xffffffff);
        try {
            BlinkFix.videoPlayer.render(0, 0, width, height);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        // render blur
        StencilUtils.write(false);
        RenderUtils.drawRect(14, 0, 171, height, 0xFFFFFFFF);
        StencilUtils.erase(true);
        DualBlurUtils.renderBlur(3, 5, true);
        StencilUtils.dispose();

        // render left side
        RenderUtils.drawRect(14, 0, 171, height, Colors.getColor(0, 0, 0, 80));

        FontManager fontManager = BlinkFix.getInstance().getFontManager();

        // draw blue client name
        fontManager.comfortaa35.drawCenteredStringWithShadow(CLIENT_DISPLAY_NAME, 90, 17, 0xFF00AAFF);

        // draw buttons (singleplayer, multiplayer, alt manager, options, quit)
        float buttonX = 55;
        float baseButtonY = 80;

        for (MainMenuButton button : buttons) {
            button.getTimer().update(true);
        }

        for (int i = 0; i < buttons.size(); i++) {
            MainMenuButton mainMenuButton = buttons.get(i);
            SmoothAnimationTimer timer = mainMenuButton.getTimer();

            if (RenderUtils.isHovering(mouseX, mouseY, buttonX - 25, baseButtonY + i * 25 - 5, 170 - 20, baseButtonY + i * 25 + 20)) {
                timer.target = 60;
                RenderUtils.drawRoundedRect(buttonX - 25, baseButtonY + i * 25 - 5, 170 - 20, baseButtonY + i * 25 + 20, 5f, Colors.getColor(255, 255, 255, (int) timer.value));
            } else {
                timer.target = 0;
            }

            fontManager.siyuan18.drawStringWithShadow(mainMenuButton.getText(), buttonX, baseButtonY + i * 25, 0xFFFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float buttonX = 55;
        float baseButtonY = 80;

        for (int i = 0; i < buttons.size(); i++) {
            MainMenuButton mainMenuButton = buttons.get(i);
            if (RenderUtils.isHovering(mouseX, mouseY, buttonX - 25, baseButtonY + i * 25 - 5, 170 - 20, baseButtonY + i * 25 + 20) && mainMenuButton.getRunnable() != null) {
                mainMenuButton.getRunnable().run();
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void onGuiClosed() {
    }
}
