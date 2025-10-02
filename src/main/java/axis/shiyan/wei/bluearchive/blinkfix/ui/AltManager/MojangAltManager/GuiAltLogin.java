package axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.MojangAltManager;


import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.utils.font.FontManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import org.bytedeco.javacv.FrameGrabber;

import java.io.IOException;

import static axis.shiyan.wei.bluearchive.blinkfix.BlinkFix.videoPlayer;

public abstract class GuiAltLogin extends GuiScreen {
    private final GuiScreen previousScreen;
    private GuiTextField username;
    protected volatile String status = EnumChatFormatting.YELLOW + "Pending...";
    FontManager fontManager = BlinkFix.getInstance().getFontManager();
    public GuiAltLogin(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                if (username.getText().length() != 0) {
                    this.onLogin(username.getText(), "");
                } else {
                    status = EnumChatFormatting.RED + "Login failed!";
                }
                break;
            case 1:
                this.mc.displayGuiScreen(this.previousScreen);
                break;
            case 1145:
                this.onLogin(StringUtils.randomString(StringUtils.ALPHA_POOL,10), "");
                this.mc.displayGuiScreen(this.previousScreen);
                break;
        }
    }

    public abstract void onLogin(String account,String password);

    public void drawScreen(int x, int y, float z) {
        try {
            videoPlayer.render(0, 0, width, height);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        this.username.drawTextBox();
        fontManager.siyuan18.drawCenteredStringWithShadow("离线登录", (float)(this.width / 2), 20.0F, -1);
        fontManager.opensans18.drawCenteredStringWithShadow(status, (float) (this.width / 2), (this.height / 4 + 24) + 38, -1);
//        if (this.username.getText().isEmpty() && !this.username.isFocused()) {
//            fontManager.opensans18.drawStringWithShadow("Username", (float)(this.width / 2 - 96), (this.height / 4 + 24) + 72 - 4, -7829368);
//        }

        super.drawScreen(x, y, z);
    }

    @Override
    public void initGui() {
        final int var3 = this.height / 4 + 24;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, var3 + 72 + 12, "登录"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, var3 + 72 + 12 + 24, "返回"));
        this.buttonList.add(new GuiButton(1145, this.width / 2 - 100, var3 + 72 + 12 + 48, "随机id"));
        this.username = new GuiTextField(1, this.mc.fontRendererObj, this.width / 2 - 100, var3 + 72 - 12, 200, 20);
        this.username.setFocused(true);
        this.username.setMaxStringLength(200);
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        super.keyTyped(character, key);

        if (character == '\t' && this.username.isFocused()) {
            this.username.setFocused(!this.username.isFocused());
        }

        if (character == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }

        this.username.textboxKeyTyped(character, key);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);

        this.username.mouseClicked(x, y, button);
    }

    @Override
    public void updateScreen() {
        this.username.updateCursorCounter();
    }
}
