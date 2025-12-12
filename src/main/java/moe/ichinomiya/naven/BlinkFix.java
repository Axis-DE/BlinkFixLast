package moe.ichinomiya.naven;

import moe.ichinomiya.naven.utils.*;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.ViaMCP;
//import dev.yalan.live.LiveClient;
import moe.ichinomiya.naven.events.impl.EventClientInit;
import moe.ichinomiya.naven.events.impl.EventShader;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.impl.render.Notifications;
import moe.ichinomiya.naven.ui.AltManager.NetEaseAltManager.AltManager;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.utils.Managers.BlinkComponent;
import lombok.Getter;
import moe.ichinomiya.naven.commands.CommandManager;
import moe.ichinomiya.naven.events.api.EventManager;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.files.FileManager;
import moe.ichinomiya.naven.protocols.HuaYuTing.MythProtocol;
import moe.ichinomiya.naven.protocols.HuaYuTing.germ.GermMod;
import moe.ichinomiya.naven.protocols.HuaYuTing.world.Wrapper;
import moe.ichinomiya.naven.modules.ModuleManager;
import moe.ichinomiya.naven.ui.cooldown.CooldownBarManager;
import moe.ichinomiya.naven.ui.notification.manager.NotificationManager;
import moe.ichinomiya.naven.utils.font.FontManager;
import moe.ichinomiya.naven.values.HasValueManager;
import moe.ichinomiya.naven.values.ValueManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.main.Main;
import net.minecraft.client.shader.Framebuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;

import static moe.ichinomiya.naven.utils.Utils.mc;

public class BlinkFix {
//    private static final int EXPIRY_MONTH = 9;
//    private static final int EXPIRY_DAY = 1;
//
//    private static LocalDate getJarBuildDate() {
//        try {
//            URL jarUrl = BlinkFix.class.getProtectionDomain().getCodeSource().getLocation();
//            Path jarPath = Paths.get(jarUrl.toURI());
//            FileTime lastModifiedTime = Files.getLastModifiedTime(jarPath);
//            Instant instant = lastModifiedTime.toInstant();
//            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
//        } catch (Exception e) {
//            return LocalDate.now();
//        }
//    }
//
//    private static final LocalDate EXPIRY_DATE = getJarBuildDate().plusDays(6);
//
//        private static final int EXPIRY_MONTH = EXPIRY_DATE.getMonthValue();
//        private static final int EXPIRY_DAY = EXPIRY_DATE.getDayOfMonth();
//        public static void main(String[] args) {
//            System.out.println("构建日期: " + getJarBuildDate());
//            System.out.println("过期日期: " + EXPIRY_DATE);
//            System.out.println("EXPIRY_MONTH = " + EXPIRY_MONTH);
//            System.out.println("EXPIRY_DAY = " + EXPIRY_DAY);
//       }
//
//    //    private static final String TIME_API_URL = "https://cn.apihz.cn/api/time/getapi.php?id=10007008&key=YUXINGMURASAME&type=1";
//    private static final int MAX_TIME_DIFF = 50;
//

    public static String CLIENT_NAME = "BlinkFix";
    public static String CLIENT_DISPLAY_NAME = "BlinkFix";
    public static final String CLIENT_VERSION = "AlphaB";
    private static final Logger logger = LogManager.getLogger(BlinkFix.class);

    @Getter
    private static BlinkFix instance;
    private final TimeHelper blurTimer = new TimeHelper();
    private final TimeHelper shadowTimer = new TimeHelper();
    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);
    @Getter
    public static VideoPlayer videoPlayer;
    @Getter
    private ModuleManager moduleManager;
    @Getter
    private EventManager eventManager;
    @Getter
    private CommandManager commandManager;
    @Getter
    private FileManager fileManager;
    @Getter
    private ValueManager valueManager;
    @Getter
    private HasValueManager hasValueManager;
    @Getter
    private FontManager fontManager;
    @Getter
    private NotificationManager notificationManager;
    @Getter
    private CooldownBarManager cooldownBarManager;

    private BlinkComponent blinkComponent;

    public AltManager altManager;

    public BlinkFix() {
        instance = this;
    }


//    private boolean checkVirtualMachine() {
//        try {
//            if (VMCheck.getInstance().runChecks()) {
//                logger.error("Ciallo～(∠・ω< )⌒★！，兄弟你为什么在虚拟机运行客户端呀？");
//                return true;
//            }
//            return false;
//        } catch (Exception e) {
//            logger.error("error", e);
//            mc.shutdown();
//            return false;
//        }
//    }
//
    public void onClientInit() {
//        if (shouldShutdownClient()) {
//            return;


        logger.info("Starting The BlinkFix Client...");

        CLIENT_NAME = "BlinkFix";
        CLIENT_DISPLAY_NAME = "BlinkFix";
        logger.info("Using default client name: " + CLIENT_DISPLAY_NAME);

        Display.setTitle(CLIENT_DISPLAY_NAME + " " + Version.getClientVersion());

        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        ViaLoadingBase.getInstance().reload(ProtocolVersion.v1_12_2);

        this.fontManager = new FontManager();
        this.altManager = new AltManager();
        eventManager = new EventManager();
        hasValueManager = new HasValueManager();
        valueManager = new ValueManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        fileManager = new FileManager();
        notificationManager = new NotificationManager();
        cooldownBarManager = new CooldownBarManager();
        blinkComponent = new BlinkComponent();
        videoPlayer = new VideoPlayer();
        eventManager.register(notificationManager);
        eventManager.register(cooldownBarManager);
        fileManager.load();

        eventManager.register(new BlinkComponent());
        eventManager.register(new Wrapper());
        eventManager.register(new GermMod());
        eventManager.register(new RotationManager());
        eventManager.register(BlinkFix.getInstance());
        eventManager.register(Minecraft.getMinecraft().ingameGUI);
        eventManager.register(new ServerUtils());
        eventManager.register(new ChatMessageQueue());
        eventManager.register(new EntityWatcher());
        eventManager.register(new WorldMonitor());
        eventManager.register(new MythProtocol());

        if (Main.rawInput) {
            eventManager.register(new RawInput());
        }
        try {
            onLoadVideo();
            videoPlayer.init(new File(mc.mcDataDir + "/" + CLIENT_NAME + "/background.mp4"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        LiveClient.INSTANCE = new LiveClient();

        logger.info("BlinkFixClient Start Success");
        eventManager.call(new EventClientInit());
    }

    public BlinkComponent getBlinkComponent() {
        return blinkComponent;
    }

    public static void onLoadVideo() throws IOException {
        File file = new File(mc.mcDataDir, "/" + CLIENT_NAME + "/background.mp4");
        if (!file.exists()) {
            FileUtil.unpackFile(file, "assets/minecraft/client/background.mp4");
        }
    }

    public void onClientShutdown() {
        this.fileManager.save();
    }

    private boolean disableShadow = false, disableBlur = false;
    public void glslShaderUpdate() {
        if (ShaderUtils.isSupportGLSL()) {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

            if (!disableShadow) {
                float shadowDelay = 1000 / 90f;
                boolean shadow = shadowTimer.delay(shadowDelay, true);
                if (shadow) {
                    bloomFramebuffer = RenderUtils.createFrameBuffer(bloomFramebuffer);
                    bloomFramebuffer.framebufferClear();
                    bloomFramebuffer.bindFramebuffer(true);
                    getEventManager().call(new EventShader(scaledResolution, EventType.SHADOW));
                    bloomFramebuffer.unbindFramebuffer();
                }
                DropShadowUtils.renderDropShadow(bloomFramebuffer.framebufferTexture, 10, 2, shadow);

                int shadowError = GL11.glGetError();
                if (shadowError != 0) {
                    disableShadow = true;
                    logger.error("OpenGL Error: {}, disabling shadow!", shadowError);
                }
            }

            if (!disableBlur) {
                float blurDelay = 1000 / 60f;
                boolean blur = blurTimer.delay(blurDelay, true);
                StencilUtils.write(false);
                getEventManager().call(new EventShader(scaledResolution, EventType.BLUR));
                StencilUtils.erase(true);
                DualBlurUtils.renderBlur(3, 5, blur);
                StencilUtils.dispose();

                int blurError = GL11.glGetError();
                if (blurError != 0) {
                    disableBlur = true;
                    logger.error("OpenGL Error: {}, disabling blur!", blurError);
                }
            }
        }
    }

    public void onModuleToggle(Module module) {
        if (module instanceof Notifications) {
            notificationManager.setEnabled(module.isEnabled());
        }
    }
}