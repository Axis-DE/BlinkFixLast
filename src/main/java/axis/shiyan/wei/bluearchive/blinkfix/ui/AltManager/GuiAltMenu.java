package axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager;

import axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.NetEaseAltManager.GuiAltManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.bytedeco.javacv.FrameGrabber;

import static axis.shiyan.wei.bluearchive.blinkfix.BlinkFix.videoPlayer;

public class GuiAltMenu extends GuiScreen {

    private GuiButton button1;
    private GuiButton button2;
    private GuiButton button3;

    @Override
    public void initGui() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = (this.width - buttonWidth) / 2;
        int centerY = this.height / 2;

        this.buttonList.add(this.button1 = new GuiButton(0, centerX, centerY - 30, buttonWidth, buttonHeight, "Microsoft 登录"));
        this.buttonList.add(this.button2 = new GuiButton(1, centerX, centerY + 10, buttonWidth, buttonHeight, "NetEase 登录"));
        this.buttonList.add(this.button3 = new GuiButton(2, 2, height - 22, 50, 20, "返回"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {
            videoPlayer.render(0, 0, width, height);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        this.drawCenteredString(this.fontRendererObj, "AltManager Select", this.width / 2, 30, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            mc.displayGuiScreen(new GuiAltManager(this));
        } else if (button.id == 0) {
            mc.displayGuiScreen(new axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.MojangAltManager.GuiAltManager(this));
        } else
        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}