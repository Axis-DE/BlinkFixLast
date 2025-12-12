package moe.ichinomiya.naven.ui.dynamicisland;

import moe.ichinomiya.naven.BlinkFix;
//import dev.yalan.live.LiveClient;
import moe.ichinomiya.naven.modules.impl.combat.AutoGapple;
import moe.ichinomiya.naven.modules.impl.move.Velocity;
import moe.ichinomiya.naven.modules.impl.misc.Disabler;
import moe.ichinomiya.naven.modules.impl.move.Blink;
import moe.ichinomiya.naven.modules.impl.render.DynamicIsland;
import moe.ichinomiya.naven.modules.impl.move.Scaffold;
import moe.ichinomiya.naven.ui.notification.styles.DynamicIslandNotification;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.StencilUtils;
import moe.ichinomiya.naven.utils.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicIslandRenderer {
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 24;
    private static final int DEFAULT_CORNER_RADIUS = 11;
    private static final float DEFAULT_ANIMATION_SPEED = 6.0f;
    private static final float DEFAULT_ELASTIC_EFFECT = 1.0f;
    private static final int DEFAULT_POS_X = 0;
    private static final int DEFAULT_POS_Y = 53;

    private final List<DynamicIslandNotification> dynamicIslandNotifications = new CopyOnWriteArrayList<>();
    private DynamicIslandNotification currentDynamicIslandNotification = null;
    private long dynamicIslandNotificationStartTime = 0;
    private int currentWidth = DEFAULT_WIDTH;
    private int targetWidth = DEFAULT_WIDTH;
    private long lastUpdateTime = 0;
    private boolean scaffoldActive = false;
    private long scaffoldAnimationStart = 0;
    private long disablerAnimationStart = 0;
    private float islandScale = 1.0f;
    private float targetScale = 1.0f;
    private long colorPulseTime = 0;
    private int currentAccentColor = 0xFF007AFF;
    private int currentHeight = DEFAULT_HEIGHT;
    private int targetHeight = DEFAULT_HEIGHT;
    private float currentCornerRadius = DEFAULT_CORNER_RADIUS;
    private float targetCornerRadius = DEFAULT_CORNER_RADIUS;
    private boolean disablerActive = false;
    private boolean blinkActive = false;
    private boolean AutoGappleActive = false;
    private boolean velocityActive = false;
    private long blinkAnimationStart = 0;
    private long AutoGappleAnimationStart = 0;
    private long velocityAnimationStart = 0;
    private ProgressAnimation blinkAnimation = new ProgressAnimation(0.2f);
    private ProgressAnimation AutoGappleAnimation = new ProgressAnimation(0.2f);
    private ProgressAnimation velocityAnimation = new ProgressAnimation(0.2f);

    public void addDynamicIslandNotification(DynamicIslandNotification notification) {
        dynamicIslandNotifications.add(notification);
    }

    private Blink getBlink() {
        return (Blink) BlinkFix.getInstance().getModuleManager().getModule(Blink.class);
    }

    private AutoGapple getAutoGapple() {
        return (AutoGapple) BlinkFix.getInstance().getModuleManager().getModule(AutoGapple.class);
    }

    private Velocity getVelocity() {
        return (Velocity) BlinkFix.getInstance().getModuleManager().getModule(Velocity.class);
    }

    private void updateDynamicIslandNotification() {
        if (currentDynamicIslandNotification == null && !dynamicIslandNotifications.isEmpty()) {
            DynamicIslandNotification nextNotification = dynamicIslandNotifications.get(0);
            if (nextNotification.shouldDisplay()) {
                currentDynamicIslandNotification = dynamicIslandNotifications.remove(0);
                dynamicIslandNotificationStartTime = System.currentTimeMillis();
            } else {
                dynamicIslandNotifications.remove(0);
            }
        }
        if (currentDynamicIslandNotification != null && currentDynamicIslandNotification.isExpired()) {
            currentDynamicIslandNotification = null;
        }
        dynamicIslandNotifications.removeIf(DynamicIslandNotification::isExpired);
    }

    private void drawDynamicIsland(int centerX, int y, DynamicIsland config,
                                   boolean scaffoldActive, boolean disablerActive,
                                   boolean blinkActive, boolean AutoGappleActive, boolean velocityActive) {
        updateDynamicIslandNotification();

        boolean hasDynamicIslandNotification = currentDynamicIslandNotification != null;
        if (hasDynamicIslandNotification) {
            String displayText = currentDynamicIslandNotification.getDisplayText();
            FontManager fontManager = BlinkFix.getInstance().getFontManager();
            int textWidth = fontManager.poppinsregular15.getStringWidth(displayText);
            targetWidth = Math.max(DEFAULT_WIDTH, Math.min(350, textWidth + 80));
        } else if (scaffoldActive || disablerActive || blinkActive || AutoGappleActive || velocityActive) {
            targetWidth = 280;
        } else {
            targetWidth = calculateNormalWidth();
        }

        updateWidthAnimation(scaffoldActive || disablerActive || blinkActive || AutoGappleActive || velocityActive || hasDynamicIslandNotification);
        updateIslandSize();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            float scaleCenterX = centerX;
            float scaleCenterY = y + currentHeight / 2f;
            GlStateManager.translate(scaleCenterX * (1 - islandScale), scaleCenterY * (1 - islandScale), 0);
            GlStateManager.scale(islandScale, islandScale, 1);
            int scaledX = (int) (centerX - (currentWidth * islandScale) / 2);
            int scaledY = (int) (y - (currentHeight * (1 - islandScale)) / 2);
            int scaledWidth = (int)(currentWidth * islandScale);
            int scaledHeight = (int)(currentHeight * islandScale);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableTexture2D();
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);

            StencilUtils.write(false);
            RenderUtils.drawBoundRoundedRect(scaledX, scaledY, scaledWidth, scaledHeight, (int)currentCornerRadius, 0xFFFFFFFF);
            StencilUtils.erase(true);
            RenderUtils.drawRect(scaledX, scaledY, scaledWidth, 2, 0xFF007AFF);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (hasDynamicIslandNotification) {
                drawDynamicIslandNotificationContent(scaledX, scaledY, scaledWidth, scaledHeight);
            } else if (disablerActive) {
                drawDisablerContent(scaledX, scaledY, scaledWidth, scaledHeight);
            } else if (scaffoldActive) {
                drawScaffoldContent(scaledX, scaledY, scaledWidth, scaledHeight);
            } else if (blinkActive) {
                drawBlinkContent(scaledX, scaledY, scaledWidth, scaledHeight);
            } else if (AutoGappleActive) {
                drawAutoGappleContent(scaledX, scaledY, scaledWidth, scaledHeight);
            } else if (velocityActive) {
                // Velocity content can be added here if needed
            } else {
                drawNormalContent(scaledX, scaledY, scaledWidth, scaledHeight);
            }

            StencilUtils.dispose();
        } finally {
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    private void drawDynamicIslandNotificationContent(int x, int y, int width, int height) {
        if (currentDynamicIslandNotification == null) return;

        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        String displayText = currentDynamicIslandNotification.getDisplayText();
        if (displayText == null) return;

        long elapsed = System.currentTimeMillis() - dynamicIslandNotificationStartTime;
        float progress = Math.min(elapsed / 500f, 1.0f);

        float scale = 0.8f + 0.2f * easeOutCubic(progress);

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            GlStateManager.translate(x + width / 2, y + height / 2, 0);
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.translate(-(x + width / 2), -(y + height / 2), 0);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            int textWidth = fontManager.poppinsregular15.getStringWidth(displayText);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 8) / 2;

            fontManager.poppinsregular15.drawString(displayText, textX, textY, 0xFFFFFFFF);

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        } finally {
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    private void drawNormalContent(int x, int y, int width, int height) {
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        GlStateManager.pushAttrib();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        try {
            int textY = y + (height - 8) / 2;

            String clientName = BlinkFix.CLIENT_DISPLAY_NAME;
            String fps = Minecraft.getDebugFPS() + " FPS";
            String idle = "String.valueOf(LiveClient.INSTANCE.autoUsername);";

            int clientNameWidth = fontManager.poppinsregular15.getStringWidth(clientName);
            int idleWidth = fontManager.opensans15.getStringWidth(idle);
            int fpsWidth = fontManager.poppinsregular15.getStringWidth(fps);

            int margin = 10;
            int clientNameX = x + margin;
            fontManager.poppinsregular15.drawString(clientName, clientNameX, textY, 0xFFFFFFFF);
            int idleX = x + (width - idleWidth) / 2;
            fontManager.poppinsregular15.drawString(idle, idleX, textY, 0xFFFFFFFF);
            int fpsX = x + width - fpsWidth - margin;
            fontManager.poppinsregular15.drawString(fps, fpsX, textY, 0xFFFFFFFF);
        } finally {
            GlStateManager.popAttrib();
        }
    }

    private void drawScaffoldContent(int x, int y, int width, int height) {
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        Scaffold scaffold = getScaffold();
        if (scaffold == null) return;

        drawCurrentBlock(x + 8, y + (height - 16) / 2, scaffold, 1.0f);
        drawBlockStats(x, y + (height - 16) / 2, width, scaffold, fontManager, 1.0f);
        drawProgressBar(x + 29, y + (height - 3) / 2, width - 70, scaffold, 1.0f);
    }

    private int calculateNormalWidth() {
        String clientName = BlinkFix.CLIENT_DISPLAY_NAME;
        String idle = "String.valueOf(LiveClient.INSTANCE.autoUsername);";
        String fps = "999 FPS";

        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        int clientNameWidth = fontManager.poppinsregular15.getStringWidth(clientName);
        int idleWidth = fontManager.opensans15.getStringWidth(idle);
        int fpsWidth = fontManager.poppinsregular15.getStringWidth(fps);

        int margin = 10;
        return Math.max(DEFAULT_WIDTH, Math.min(350, clientNameWidth + idleWidth + fpsWidth + (margin * 4)));
    }

    private DynamicIsland getConfig() {
        return (DynamicIsland) BlinkFix.getInstance().getModuleManager().getModule(DynamicIsland.class);
    }

    private Scaffold getScaffold() {
        return (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
    }

    private Disabler getDisabler() {
        return (Disabler) BlinkFix.getInstance().getModuleManager().getModule(Disabler.class);
    }

    private ProgressAnimation scaffoldAnimation = new ProgressAnimation(0.2f);
    private ProgressAnimation disablerAnimation = new ProgressAnimation(0.2f);

    private class ProgressAnimation {
        private float value = 0;
        private float target = 0;
        private final float speed;

        public ProgressAnimation(float speed) {
            this.speed = speed;
        }

        public void update() {
            value += (target - value) * speed;
            if (Math.abs(target - value) < 0.001f) {
                value = target;
            }
        }

        public float getValue() {
            return value;
        }

        public void setTarget(float target) {
            this.target = Math.max(0, Math.min(1, target));
        }
    }

    public void render() {
        if (shouldSkipRender()) return;

        DynamicIsland config = getConfig();
        if (config == null || !config.isEnabled()) return;

        Scaffold scaffold = getScaffold();
        Disabler disabler = getDisabler();
        Blink blink = getBlink();
        AutoGapple autogapple = getAutoGapple();
        Velocity velocity = getVelocity();

        boolean wasScaffoldActive = scaffoldActive;
        boolean wasDisablerActive = disablerActive;
        boolean wasBlinkActive = blinkActive;
        boolean wasAutoGappleActive = AutoGappleActive;
        boolean wasVelocityActive = velocityActive;

        scaffoldActive = scaffold != null && scaffold.isEnabled() && scaffold.blocksWidget.getCurrentValue()
                && scaffold.blocksWidgetMode.isCurrentMode("DynamicIsland");
        disablerActive = disabler != null && disabler.isEnabled() && disabler.delayedServerPackets.size() > 1
              && disabler.rendermode.isCurrentMode("DynamicIsland");
        blinkActive = blink != null && blink.isEnabled() && blink.rendermode.isCurrentMode("DynamicIsland")
                && blink.render.getCurrentValue();
//        AutoGappleActive = autogapple != null && autogapple.isEnabled() && autogapple.eating()
//        && autogapple.rendermode.isCurrentMode("DynamicIsland") && autogapple.render.getCurrentValue();
//        velocityActive = velocity != null && velocity.isEnabled() && velocity.getTicksSinceVelocity() < 20;

        if (scaffoldActive != wasScaffoldActive) {
            scaffoldAnimationStart = System.currentTimeMillis();
        }
        if (disablerActive != wasDisablerActive) {
            disablerAnimationStart = System.currentTimeMillis();
        }
        if (blinkActive != wasBlinkActive) {
            blinkAnimationStart = System.currentTimeMillis();
        }
        if (AutoGappleActive != wasAutoGappleActive) {
            AutoGappleAnimationStart = System.currentTimeMillis();
        }
        if (velocityActive != wasVelocityActive) {
            velocityAnimationStart = System.currentTimeMillis();
        }

        updateScaffoldProgress(scaffold);
        updateDisablerProgress(disabler);
        updateBlinkProgress(blink);
//        updateAutoGappleProgress(autogapple);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int screenWidth = sr.getScaledWidth();

        float posX = (float) config.posX.getCurrentValue();
        float posY = (float) config.posY.getCurrentValue();

        float x = screenWidth / 2f + posX;
        float y = posY;
        boolean anyModuleActive = scaffoldActive || disablerActive || blinkActive || AutoGappleActive || velocityActive;
        updateWidthAnimation(anyModuleActive);
        drawDynamicIsland((int) x, (int) y, config, scaffoldActive, disablerActive, blinkActive, AutoGappleActive, velocityActive);

    }
    private void updateIslandSize() {
        // 保持固定尺寸
        targetHeight = DEFAULT_HEIGHT;
        targetCornerRadius = DEFAULT_CORNER_RADIUS;

        float sizeSpeed = 0.3f;
        currentHeight += (targetHeight - currentHeight) * sizeSpeed;
        currentCornerRadius += (targetCornerRadius - currentCornerRadius) * sizeSpeed;
        currentHeight = Math.max(DEFAULT_HEIGHT, Math.min(DEFAULT_HEIGHT, currentHeight));
        currentCornerRadius = Math.max(DEFAULT_CORNER_RADIUS, Math.min(DEFAULT_CORNER_RADIUS, currentCornerRadius));
    }

    private void updateDisablerProgress(Disabler disabler) {
        if (disabler != null && disabler.isEnabled()) {
            int maxPackets = 190;
            int currentPackets = Math.max(0, disabler.delayedServerPackets.size() - 1);
            float targetProgress = Math.min((float) currentPackets / maxPackets, 1.0f);
            disablerAnimation.setTarget(targetProgress);
        } else {
            disablerAnimation.setTarget(0);
        }
        disablerAnimation.update();
    }

    private float getDisablerAnimationProgress() {
        long elapsed = System.currentTimeMillis() - disablerAnimationStart;
        return Math.min(elapsed / 200f, 1.0f);
    }

    private void drawCurrentBlock(int x, int y, Scaffold scaffold, float alpha) {
        if (scaffold.slotID >= 0 && scaffold.slotID < 9) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.mainInventory[scaffold.slotID];
            if (stack != null) {
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
                Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, x, y);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    private void drawBlockStats(int x, int y, int width, Scaffold scaffold, FontManager fontManager, float alpha) {
        int textColor = (int) (0xFFFFFFFF * alpha);
        int heldBlocks = scaffold.getCurrentHeldBlocks();
        String blocksText = heldBlocks + " Blocks";
        int blocksWidth = fontManager.poppinsregular12.getStringWidth(blocksText);
        int blocksX = x + width - blocksWidth - 10;
        int blocksY = y - 2;
        fontManager.poppinsregular12.drawString(blocksText, blocksX, blocksY, textColor);
        String bpsText = String.format("%.1f", getPlayerBPS()) + " BPS";
        int bpsWidth = fontManager.poppinsregular12.getStringWidth(bpsText);
        int bpsX = x + width - bpsWidth - 10;
        int bpsY = y + 9;
        fontManager.poppinsregular12.drawString(bpsText, bpsX, bpsY, textColor);
    }

    private void updateScaffoldProgress(Scaffold scaffold) {
        if (scaffold != null && scaffold.isEnabled()) {
            int maxBlocks = 64;
            int heldBlocks = scaffold.getCurrentHeldBlocks();
            int currentBlocks = Math.min(heldBlocks, maxBlocks);
            float targetProgress = (float) currentBlocks / maxBlocks;
            scaffoldAnimation.setTarget(targetProgress);
        } else {
            scaffoldAnimation.setTarget(0);
        }
        scaffoldAnimation.update();
    }

    private void drawProgressBar(int x, int y, int width, Scaffold scaffold, float alpha) {
        int maxBlocks = 64;
        float progress = scaffoldAnimation.getValue();

        int progressBarWidth = width;
        int progressBarX = x;

        int progressWidth = (int) (progressBarWidth * progress);
        int progressColor = new Color(100, 149, 237, 255).getRGB();
        RenderUtils.drawBoundRoundedRect(progressBarX, y, progressWidth, 5, 2, progressColor);
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        int heldBlocks = scaffold != null ? scaffold.getCurrentHeldBlocks() : 0;
        int currentBlocks = Math.min(heldBlocks, maxBlocks);
        String progressText = "";
        int textWidth = fontManager.poppinsregular12.getStringWidth(progressText);
        int textX = progressBarX + (progressBarWidth - textWidth) / 2;
        int textY = y - 12;
        fontManager.poppinsregular12.drawString(progressText, textX, textY, 0xFFFFFFFF);
    }

    private void updateWidthAnimation(boolean expanded) {
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - lastUpdateTime;

        if (delta > 16) {
            if (expanded) {
                targetWidth = 280;
            } else {
                targetWidth = calculateNormalWidth();
            }
            float animationSpeed = DEFAULT_ANIMATION_SPEED;
            float elasticEffect = DEFAULT_ELASTIC_EFFECT;

            if (currentWidth != targetWidth) {
                float difference = targetWidth - currentWidth;
                float baseStep = difference * 0.2f;
                if (elasticEffect > 0) {
                    float overshoot = difference * elasticEffect * 0.1f;
                    baseStep += overshoot;
                }
                float step = baseStep * (animationSpeed / 8f);
                if (Math.abs(difference) > 1) {
                    currentWidth += step;
                    if ((difference > 0 && currentWidth > targetWidth) ||
                            (difference < 0 && currentWidth < targetWidth)) {
                        currentWidth = targetWidth;
                    }
                } else {
                    currentWidth = targetWidth;
                }
            }
            lastUpdateTime = currentTime;
        }
    }

    private float getScaffoldAnimationProgress() {
        long elapsed = System.currentTimeMillis() - scaffoldAnimationStart;
        return Math.min(elapsed / 200f, 1.0f);
    }

    private double getPlayerBPS() {
        double dx = Minecraft.getMinecraft().thePlayer.posX - Minecraft.getMinecraft().thePlayer.prevPosX;
        double dz = Minecraft.getMinecraft().thePlayer.posZ - Minecraft.getMinecraft().thePlayer.prevPosZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        return distance * 20;
    }

    private boolean shouldSkipRender() {
        return Minecraft.getMinecraft().gameSettings.hideGUI ||
                Minecraft.getMinecraft().gameSettings.showDebugInfo;
    }

    public void renderShader() {
        if (shouldSkipRender()) return;

        DynamicIsland config = getConfig();
        if (config == null) return;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int screenWidth = sr.getScaledWidth();
        float posX = (float) config.posX.getCurrentValue();
        float posY = (float) config.posY.getCurrentValue();

        int x = (int) (screenWidth / 2f + posX - currentWidth / 2f);
        int y = (int) posY;
        int height = (int) config.height.getCurrentValue();
        int radius = (int) config.cornerRadius.getCurrentValue();

        RenderUtils.drawBoundRoundedRect(x, y, currentWidth, height, radius, 0xFFFFFFFF);
    }

    private void drawBlinkProgressBar(int x, int y, int width, Blink blink, float alpha) {
        long movementPackets = blink.packets.stream().filter(p -> p instanceof C03PacketPlayer).count();
        float maxTicks = blink.blinkTicks.getCurrentValue();
        float progress = Math.max(0, Math.min(1, movementPackets / maxTicks));
        int progressWidth = (int) (width * progress);
        int progressColor = new Color(70, 130, 180, 255).getRGB();
        RenderUtils.drawBoundRoundedRect(x, y, progressWidth, 5, 2, progressColor);

        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        String progressText = "Blink: " + movementPackets + " packets";
        int textWidth = fontManager.poppinsregular12.getStringWidth(progressText);
        int textX = x - 55;
        int textY = y - 2;
        fontManager.poppinsregular12.drawString(progressText, textX, textY, 0xFFFFFFFF);
    }

    private void drawAutoGappleProgressBar(int x, int y, int width, AutoGapple AutoGapple, float alpha) {
        int currentC03s = AutoGapple.c03s;
        int maxTicks = (int) AutoGapple.tick.getCurrentValue();
        float progress = Math.min((float) currentC03s / maxTicks, 1.0f);
        int progressWidth = (int) (width * progress);
        int progressColor = new Color(255, 215, 0, 255).getRGB();
        RenderUtils.drawBoundRoundedRect(x, y, progressWidth, 5, 2, progressColor);

        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        String progressText = "Eating: " + currentC03s + "/" + maxTicks;
        int textWidth = fontManager.poppinsregular12.getStringWidth(progressText);
        int textX = x - 55;
        int textY = y - 2;
        fontManager.poppinsregular12.drawString(progressText, textX, textY, 0xFFFFFFFF);
    }

    private void drawDisablerProgressBar(int x, int y, int width, Disabler disabler, float alpha) {
        int maxPackets = 190;
        int currentPackets = Math.max(0, disabler.delayedServerPackets.size() - 1);
        float progress = Math.min((float) currentPackets / maxPackets, 1.0f);
        int progressWidth = (int) (width * progress);
        int progressColor = new Color(147, 112, 219, 255).getRGB();
        RenderUtils.drawBoundRoundedRect(x, y, progressWidth, 5, 2, progressColor);

        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        String progressText = "Packets: " + currentPackets;
        int textWidth = fontManager.poppinsregular12.getStringWidth(progressText);
        int textX = x - 55;
        int textY = y - 2;
        fontManager.poppinsregular12.drawString(progressText, textX, textY, 0xFFFFFFFF);
    }

    private void drawBlinkContent(int x, int y, int width, int height) {
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        Blink blink = getBlink();
        if (blink == null) return;

        drawBlinkProgressBar(x + 60, y + (height - 4) / 2, width - 65, blink, 1.0f);
    }

    private void drawAutoGappleContent(int x, int y, int width, int height) {
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        AutoGapple AutoGapple = getAutoGapple();
        if (AutoGapple == null) return;

        drawAutoGappleProgressBar(x + 60, y + (height - 4) / 2, width - 65, AutoGapple, 1.0f);
    }

    private void drawDisablerContent(int x, int y, int width, int height) {
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        Disabler disabler = getDisabler();
        if (disabler == null) return;

        drawDisablerProgressBar(x + 60, y + (height - 8) / 2, width - 60, disabler, 1.0f);
    }

    private void updateBlinkProgress(Blink blink) {
        if (blink != null && blink.isEnabled()) {
            long movementPackets = blink.packets.stream().filter(p -> p instanceof C03PacketPlayer).count();
            float maxTicks = blink.blinkTicks.getCurrentValue();
            float targetProgress = Math.max(0, Math.min(1, movementPackets / maxTicks));
            blinkAnimation.setTarget(targetProgress);
        } else {
            blinkAnimation.setTarget(0);
        }
        blinkAnimation.update();
    }

//    private void updateAutoGappleProgress(AutoGapple AutoGapple) {
//        if (AutoGapple != null && AutoGapple.isEnabled() && AutoGapple.eating()) {
//            int currentC03s = AutoGapple.c03s;
//            int maxTicks = (int) AutoGapple.tick.getCurrentValue();
//            float targetProgress = Math.min((float) currentC03s / maxTicks, 1.0f);
//            AutoGappleAnimation.setTarget(targetProgress);
//        } else {
//            AutoGappleAnimation.setTarget(0);
//        }
//        AutoGappleAnimation.update();
//    }

    private float easeOutCubic(float x) {
        return (float) (1 - Math.pow(1 - x, 3));
    }
}