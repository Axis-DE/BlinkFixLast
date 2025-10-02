package dev.yalan.live.gui;

import dev.yalan.live.LiveClient;
import dev.yalan.live.events.*;
import dev.yalan.live.netty.LiveProto;
import jnic.JNICExclude;
import jnic.JNICInclude;
import lombok.Getter;
import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.files.FileManager;
import axis.shiyan.wei.bluearchive.blinkfix.utils.Colors;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;
import axis.shiyan.wei.bluearchive.blinkfix.utils.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.FrameGrabber;
import tech.skidonion.obfuscator.annotations.ControlFlowObfuscation;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

@ControlFlowObfuscation
@JNICInclude
public class GuiAuthentication extends GuiScreen {
    private static boolean firstTime = true;

    private final Logger logger = LogManager.getLogger("GuiAuthentication");
    private final File accountDataFile = new File(FileManager.clientFolder, "LiveAccount.dat");
    private GuiTextField username;
    private GuiTextField password;
    private GuiButton loginButton;
    private SmoothAnimationTimer usernameHoverTimer = new SmoothAnimationTimer(5);
    private SmoothAnimationTimer passwordHoverTimer = new SmoothAnimationTimer(6);
    private float inputFieldAlpha = 0.8f;
    public String status;

    public GuiAuthentication() {
        this(EnumChatFormatting.YELLOW + "Pending...");
    }

    public GuiAuthentication(String status) {
        this.status = status;

        LiveClient.INSTANCE.eventManager.register(this);
    }

    @EventTarget
    public void onLiveChannelInactive(EventLiveChannelInactive e) {
        loginButton.enabled = true;
        username.setEnabled(true);
        password.setEnabled(true);
    }

    @EventTarget
    public void onLiveChannelException(EventLiveChannelException e) {
        this.status = e.getCause().toString();
    }

    @EventTarget
    public void onLiveConnectionStatus(EventLiveConnectionStatus e) {
        if (e.getCause() != null) {
            this.status = e.getCause().toString();
        }
    }

    @EventTarget
    public void onLiveGenericMessage(EventLiveGenericMessage e) {
        this.status = e.getMessage();
    }

    @EventTarget
    public void onLiveAuthenticationResult(EventLiveAuthenticationResult e) {
        this.status = (e.isSuccess() ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + e.getMessage();
        this.loginButton.enabled = true;
        this.username.setEnabled(true);
        this.password.setEnabled(true);

        if (e.isSuccess()) {
            LiveClient.INSTANCE.autoUsername.set(username.getText());
            LiveClient.INSTANCE.autoPassword.set(password.getText());

            try {
                saveAccountData();
            } catch (Exception ex) {
                logger.error("Can't save your account data", ex);
            }

            LiveClient.INSTANCE.loginIndex1.set(ThreadLocalRandom.current().nextInt(0, 337722));
            LiveClient.INSTANCE.loginIndex2.set(20250805 - LiveClient.INSTANCE.loginIndex1.get());
            LiveClient.INSTANCE.startReconnectThread();
            Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    public void initGui() {
        final int halfWidth = width / 2;
        final int halfHeight = height / 2;
        final String previousUsername;
        final String previousPassword;
        final boolean previousLoginButtonEnabled;

        if (this.username != null) {
            previousUsername = this.username.getText();
        } else {
            previousUsername = "";
        }
        this.username = new GuiTextField(0, mc.fontRendererObj, halfWidth - 83, halfHeight - 35, 166, 20);
        this.password = new GuiTextField(1, mc.fontRendererObj, halfWidth - 83, halfHeight, 166, 20);
        previousPassword = this.password.getText();

        if (this.loginButton != null) {
            previousLoginButtonEnabled = this.loginButton.enabled;
        } else {
            previousLoginButtonEnabled = true;
        }

        // this.username = new GuiTextField(0, mc.fontRendererObj, halfWidth - 83, halfHeight - 35, 166, 20);
        this.username.setText(previousUsername);
        //this.password = new GuiTextField(1, mc.fontRendererObj, halfWidth - 83, halfHeight, 166, 20);
        this.password.setText(previousPassword);
        this.buttonList.add(this.loginButton = new GuiButton(0, halfWidth - 83, halfHeight + 25, 80, 20, "Login"));
        this.loginButton.enabled = previousLoginButtonEnabled;
        this.buttonList.add(new GuiButton(1, halfWidth + 3, halfHeight + 25, 80, 20, ""));
        this.buttonList.add(new GuiButton(2, halfWidth - 83, halfHeight + 50, 80, 20, ""));
        this.buttonList.add(new GuiButton(3, halfWidth + 3, halfHeight + 50, 80, 20, ""));
        this.buttonList.add(new GuiButton(4, halfWidth - 37, halfHeight + 75, 80, 20, ""));

        if (firstTime) {
            firstTime = false;

            try {
                loadAccountData();
            } catch (Exception e) {
                logger.error("Can't load your account data", e);
            }

            LiveClient.INSTANCE.connect();
        }
    }

    @Override
    public void onGuiClosed() {
        LiveClient.INSTANCE.eventManager.unregister(this);
    }

    private void drawInputField(GuiTextField textField, int mouseX, int mouseY, String label, SmoothAnimationTimer hoverTimer) {
        int x = textField.xPosition;
        int y = textField.yPosition;
        int width = textField.width;
        int height = textField.height;

        boolean hovered = RenderUtils.isHovering(mouseX, mouseY, x, y, x + width, y + height);
        boolean focused = textField.isFocused();

        hoverTimer.target = hovered || focused ? 100 : 0;
        hoverTimer.update(true);
        int bgColor = Colors.getColor(30, 30, 40, (int)(inputFieldAlpha * 255));
        RenderUtils.drawRoundedRect(x - 2, y - 2, x + width + 2, y + height + 2, 5f, bgColor);
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        String text = textField.getText();
        int textColor = Colors.getColor(220, 220, 220, 255);
        int cursorPos = textField.getCursorPosition();
        String displayText = mc.fontRendererObj.trimStringToWidth(text, width - 8);
        fontManager.siyuan18.drawStringWithShadow(displayText, x + 4, y + (height - 8) / 2 - 2, textColor);
        //   mc.fontRendererObj.drawStringWithShadow(displayText, x + 4, y + (height - 8) / 2, textColor);

        if (focused && System.currentTimeMillis() / 500 % 2 == 0) {
            int cursorX = x + 4 + mc.fontRendererObj.getStringWidth(text.substring(0, cursorPos));
            RenderUtils.drawRect(cursorX, y + 4, cursorX + 1, y + height - 4, 0xFFFFFFFF);
        }
        if (text.isEmpty() && !focused) {
            // mc.fontRendererObj.drawStringWithShadow(label, x + 4, y + (height - 8) / 2, Colors.getColor(150, 150, 150, 180));
            fontManager.regular18.drawStringWithShadow(label, x + 4, y + (height - 8) / 2, Colors.getColor(150, 150, 150, 180));
        }
    }
    private void drawButton(int mouseX, int mouseY, float x, float y, float width, float height, String text, SmoothAnimationTimer timer) {
        boolean hovered = RenderUtils.isHovering(mouseX, mouseY, x, y, x + width, y + height);

        if (hovered) {
            timer.target = 60;
        } else {
            timer.target = 0;
        }
        RenderUtils.drawRoundedRect(x, y, x + width, y + height, 5f, Colors.getColor(0, 0, 0, 80));

        if (timer.value > 0) {
            RenderUtils.drawRoundedRect(x, y, x + width, y + height, 5f, Colors.getColor(255, 255, 255, (int) timer.value));
        }
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        fontManager.siyuan18.drawCenteredStringWithShadow(text, x + width / 2, y + (height - 8) / 2 - 2, 0xFFFFFFFF);
    }
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: {
                if (LiveClient.INSTANCE.isActive()) {
                    status = EnumChatFormatting.YELLOW + "Logging...";
                    loginButton.enabled = false;
                    username.setEnabled(false);
                    password.setEnabled(false);
                    LiveClient.INSTANCE.sendPacket(LiveProto.createAuthentication(username.getText(), password.getText(), LiveClient.INSTANCE.getHardwareId()));
                }
                break;
            }
            case 1: {
                Desktop.getDesktop().browse(URI.create("https://www.unitednetwork.cc/BlinkFix/html?name=Register&hardwareId=" + URLEncoder.encode(LiveClient.INSTANCE.getHardwareId(), "UTF-8")));
                break;
            }
            case 2: {
                LiveClient.INSTANCE.connect();
                break;
            }
            case 3: {
//                mc.shutdown();
                break;
            }
            case 4: {
                Desktop.getDesktop().browse(URI.create("https://www.unitednetwork.cc/BlinkFix/html?name=Renew"));
                break;
            }
        }
    }
    @Getter
    private SmoothAnimationTimer loginHoverTimer = new SmoothAnimationTimer(0);
    private SmoothAnimationTimer registerHoverTimer = new SmoothAnimationTimer(1);
    private SmoothAnimationTimer reconnectHoverTimer = new SmoothAnimationTimer(2);
    private SmoothAnimationTimer quitHoverTimer = new SmoothAnimationTimer(3);
    private SmoothAnimationTimer renewTimer = new SmoothAnimationTimer(4);
    @JNICExclude
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int backgroundWidth = 2781, backgroundHeight = 1564;
        int imageWidth = this.width, imageHeight = this.height;
        float scale = Math.max((float) imageWidth / (float) backgroundWidth, (float) imageHeight / (float) backgroundHeight);
        imageWidth = (int) (backgroundWidth * scale);
        imageHeight = (int) (backgroundHeight * scale);
        int imageX = (this.width - imageWidth) / 2, imageY = (this.height - imageHeight) / 2;
        //RenderUtils.drawImage(new ResourceLocation("client/background.png"), imageX, imageY, imageWidth, imageHeight, 0xffffffff);
        try {
            BlinkFix.videoPlayer.render(0, 0, width, height);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        //  super.drawScreen(mouseX, mouseY, partialTicks);
        final float halfWidth = width / 2.0F;
        final float halfHeight = height / 2.0F;

        //username.drawTextBox();
        // password.drawTextBox();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        //  RenderUtils.drawRoundedRect(halfWidth,halfHeight, 200,300,4,new Color(0,0,0,150).getRGB());
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        fontManager.siyuan18.drawCenteredStringWithShadow(status, halfWidth, halfHeight - 75, new Color(241, 216, 6).getRGB());
        fontManager.comfortaa35.drawStringWithShadow("BlinkFix Authentication", halfWidth - 100, halfHeight - 100, new Color(0x3D66AB).getRGB());
        fontManager.regular20.drawString(getLiveConnectionStatus(), 2, height - mc.fontRendererObj.FONT_HEIGHT - 10, -1);
        fontManager.regular20.drawStringWithShadow("Username: ", halfWidth - 82, halfHeight - 48, -1);
        fontManager.regular20.drawStringWithShadow("Password: ", halfWidth - 82, halfHeight - 13, -10);
       /*
       mc.fontRendererObj.drawString("Username: ", halfWidth - 82, halfHeight - 45, -1);
        mc.fontRendererObj.drawString("Password: ", halfWidth - 82, halfHeight - 10, -1);
      */
        loginHoverTimer.update(true);
        registerHoverTimer.update(true);
        reconnectHoverTimer.update(true);
        quitHoverTimer.update(true);

        drawButton(mouseX, mouseY, halfWidth - 83, halfHeight + 25, 80, 20, "Login", loginHoverTimer);
        drawButton(mouseX, mouseY, halfWidth + 3, halfHeight + 25, 80, 20, "Register", registerHoverTimer);
        drawButton(mouseX, mouseY, halfWidth - 83, halfHeight + 50, 80, 20, "Reconnect", reconnectHoverTimer);
        drawButton(mouseX, mouseY, halfWidth + 3, halfHeight + 50, 80, 20, "Quit", quitHoverTimer);
        drawButton(mouseX, mouseY, halfWidth -37, halfHeight + 75, 80, 20, "Renew", renewTimer);
        drawInputField(username, mouseX, mouseY, "Enter your username", usernameHoverTimer);
        drawInputField(password, mouseX, mouseY, "Enter your password", passwordHoverTimer);
    }

    @JNICExclude
    private String getLiveConnectionStatus() {
        if (LiveClient.INSTANCE.isActive()) {
            return EnumChatFormatting.GREEN + "Connected to the server";
        }

        if (LiveClient.INSTANCE.isConnecting.get()) {
            return EnumChatFormatting.YELLOW + "Connecting to the server...";
        }

        return EnumChatFormatting.RED + "No connection to the server";
    }

    @JNICExclude
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        username.textboxKeyTyped(typedChar, keyCode);
        password.textboxKeyTyped(typedChar, keyCode);
    }

    @JNICExclude
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        username.mouseClicked(mouseX, mouseY, mouseButton);
        password.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void loadAccountData() throws Exception {
        if (!accountDataFile.exists()) {
            return;
        }

        final byte[] data = FileUtils.readFileToByteArray(accountDataFile);
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final SecretKey key = new SecretKeySpec(Base64.getDecoder().decode("fuiB/+BgnT2b/iz069EfAA=="), "AES");
        final byte[] iv = new byte[12];

        System.arraycopy(data, 0, iv, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        cipher.updateAAD(accountDataFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8));

        final byte[] out = cipher.doFinal(data, iv.length, data.length - iv.length);
        final String[] split = new String(out, StandardCharsets.UTF_8).split(System.lineSeparator());

        username.setText(split[0]);
        password.setText(split[1]);
    }

    private void saveAccountData() throws Exception {
        final byte[] data = (username.getText() + System.lineSeparator() + password.getText()).getBytes(StandardCharsets.UTF_8);
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final SecretKey key = new SecretKeySpec(Base64.getDecoder().decode("fuiB/+BgnT2b/iz069EfAA=="), "AES");
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] iv = new byte[12];

        secureRandom.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        cipher.updateAAD(accountDataFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8));

        final byte[] out = new byte[12 + cipher.getOutputSize(data.length)];
        System.arraycopy(iv, 0, out, 0, iv.length);
        cipher.doFinal(data, 0, data.length, out, 12);

        FileUtils.writeByteArrayToFile(accountDataFile, out);
    }
}
