package moe.ichinomiya.naven.ui.AltManager.MojangAltManager;


import com.mojang.authlib.exceptions.AuthenticationException;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.utils.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import moe.ichinomiya.naven.ui.AltManager.MojangAltManager.alt.MicrosoftAlt;
import moe.ichinomiya.naven.ui.AltManager.MojangAltManager.microsoft.GuiMicrosoftLogin;
import moe.ichinomiya.naven.ui.AltManager.MojangAltManager.microsoft.MicrosoftLogin;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;
import org.bytedeco.javacv.FrameGrabber;

import java.io.IOException;

import static moe.ichinomiya.naven.BlinkFix.videoPlayer;

public final class GuiAltManager extends GuiScreen {
    private final GuiScreen parentScreen;

    private GuiButton buttonLogin;
    private GuiButton buttonRemove;
    private volatile String status = EnumChatFormatting.YELLOW + "Pending...";
    private volatile MicrosoftLogin microsoftLogin;
    private volatile Thread runningThread;

    private static Alt selectAlt;

    public GuiAltManager(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {
            if (microsoftLogin != null) {
                status = microsoftLogin.getStatus();
            }
        } catch (NullPointerException ignored) {
        }
        try {
            videoPlayer.render(0, 0, width, height);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        drawDefaultBackground();

        //drawBackground(0);
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        fontManager.opensans18.drawCenteredStringWithShadow(EnumChatFormatting.YELLOW + "Current user name: " + mc.getSession().getUsername(), width / 2.0f, height / 2.0f - 10, -1);
        fontManager.opensans18.drawCenteredStringWithShadow(status, width / 2.0f, height / 2.0f, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            if (runningThread != null) {
                runningThread.interrupt();
                try {
                    videoPlayer.stop();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }

            mc.displayGuiScreen(parentScreen);
        } else if (button.id == 2) {
            if (selectAlt != null) {
                final Thread thread = new Thread(() -> {
                    status = EnumChatFormatting.YELLOW + "Logging in...";

                    switch (selectAlt.getAccountType()) {
                        case OFFLINE:
                            Minecraft.getMinecraft().session = new Session(selectAlt.getUserName(), "", "", "mojang");
                            status = EnumChatFormatting.GREEN + "Login successful! " + mc.session.getUsername();
                            break;
                        case MICROSOFT: {
                            try {
                                microsoftLogin = new MicrosoftLogin(((MicrosoftAlt) selectAlt).getRefreshToken());

                                while (Minecraft.getMinecraft().running) {
                                    if (microsoftLogin.logged) {
                                        System.out.print("");
                                        mc.session = new Session(microsoftLogin.getUserName(), microsoftLogin.getUuid(), microsoftLogin.getAccessToken(), "mojang");
                                        status = EnumChatFormatting.GREEN + "Login successful! " + mc.session.getUsername();
                                        break;
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                status = EnumChatFormatting.RED + "Login failed! " + e.getClass().getName() + ": " + e.getMessage();
                            }

                            microsoftLogin = null;

                            break;
                        }
                    }
                }, "AltManager Login Thread");

                thread.setDaemon(true);
                thread.start();

                setRunningThread(thread);
            }
        } else if (button.id == 3) {
            if (selectAlt != null) {
                AltManager.Instance.getAltList().remove(selectAlt);
                selectAlt = null;
            }
        } else if (button.id == 4) {
            mc.displayGuiScreen(new GuiAltLogin(this) {
                @Override
                public void onLogin(String account, String password) {
                    final Thread thread = new Thread() {
                        @Override
                        public void run() {
                            final AltManager.LoginStatus loginStatus;
                            try {
                                status = EnumChatFormatting.YELLOW + "Logging in...";
                                loginStatus = AltManager.loginAlt(account, password);

                                switch (loginStatus) {
                                    case FAILED:
                                        status = EnumChatFormatting.RED + "Login failed!";
                                        break;
                                    case SUCCESS:
                                        status = EnumChatFormatting.GREEN + "Login successful! " + mc.session.getUsername();
                                        break;
                                }
                            } catch (AuthenticationException e) {
                                e.printStackTrace();
                                status = EnumChatFormatting.RED + "Login failed! " + e.getClass().getName() + ": " + e.getMessage();
                            }

                            interrupt();
                        }
                    };

                    thread.setDaemon(true);
                    thread.start();

                    setRunningThread(thread);
                }
            });
        } else if (button.id == 5) {
            mc.displayGuiScreen(new GuiMicrosoftLogin(this));
        }
        super.actionPerformed(button);
    }

    @Override
    public void initGui() {
//        buttonList.add(new GuiButton(4, this.width / 2 - 120, this.height - 48, 70, 20, "离线登录"));
        buttonList.add(new GuiButton(5, this.width / 2 - 40, this.height - 48, 70, 20, "微软登录"));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 40, this.height - 48, 70, 20, "返回"));
        super.initGui();
    }

    public void setRunningThread(Thread runningThread) {
        if (this.runningThread != null) {
            this.runningThread.interrupt();
        }

        this.runningThread = runningThread;
    }
}
