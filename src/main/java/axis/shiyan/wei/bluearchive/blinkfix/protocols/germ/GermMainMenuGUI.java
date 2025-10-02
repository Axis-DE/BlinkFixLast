package axis.shiyan.wei.bluearchive.blinkfix.protocols.germ;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventShader;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;
import axis.shiyan.wei.bluearchive.blinkfix.utils.StencilUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import tech.skidonion.obfuscator.annotations.ControlFlowObfuscation;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
@ControlFlowObfuscation
public class GermMainMenuGUI extends GuiScreen {
    public final static List<GermModGame> games = new CopyOnWriteArrayList<>();
    private final static int headerColor = new Color(150, 45, 45, 255).getRGB();
    private final static int bodyColor = new Color(0, 0, 0, 190).getRGB();
    private final static int selected = new Color(255, 255, 255, 30).getRGB();
    private final static Map<String, byte[]> modes = new LinkedHashMap<String, byte[]>() {{
        put("\247b起床战争", Packets.selectBedWars);
        put("\247b空岛战争", Packets.selectSkyWars);
        put("\247b休闲模式", Packets.selectLeisure);
        put("\247b竞技游戏", Packets.selectFights);
        put("\247b生存模式", Packets.selectSurvive);
        put("\247b战争模式", Packets.selectTeamFights);
    }};
    byte[] packet = null;
    GermModGame game = null;

    SmoothAnimationTimer widthTimer = new SmoothAnimationTimer(0);
    SmoothAnimationTimer heightTimer = new SmoothAnimationTimer(0);

    SmoothAnimationTimer selectX1 = new SmoothAnimationTimer(0, 0.5f);
    SmoothAnimationTimer selectX2 = new SmoothAnimationTimer(0, 0.5f);
    SmoothAnimationTimer selectY1 = new SmoothAnimationTimer(0, 0.5f);
    SmoothAnimationTimer selectY2 = new SmoothAnimationTimer(0, 0.5f);

    @EventTarget
    public void onShader(EventShader e) {
        GlStateManager.pushMatrix();
        float windowX = width / 2f - widthTimer.value / 2, windowY = height / 2f - 60;
        GlStateManager.translate(windowX, windowY, 0);
        RenderUtils.drawBoundRoundedRect(5, 5, widthTimer.value, heightTimer.value + 3, 5, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }

    @Override
    public void onGuiClosed() {
        BlinkFix.getInstance().getEventManager().unregister(this);
        super.onGuiClosed();
    }

    @Override
    public void initGui() {
        BlinkFix.getInstance().getEventManager().register(this);
        GermMod.sendPacket(new byte[]{0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 8, 109, 97, 105, 110, 109, 101, 110, 117, 8, 109, 97, 105, 110, 109, 101, 110, 117, 8, 109, 97, 105, 110, 109, 101, 110, 117});
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();

        widthTimer.target = 120;
        for (GermModGame game : games) {
            widthTimer.target = Math.max(widthTimer.target, BlinkFix.getInstance().getFontManager().siyuan18.getStringWidth(game.getDisplayName()) + 62);
        }

        heightTimer.target = 120;
        heightTimer.target = Math.max(heightTimer.target, 20 * games.size());

        float originHeight = heightTimer.target;

        if (game != null) {
            String[] split = game.getDescription().split("\n");
            heightTimer.target += split.length * 10;

            for (String desc : split) {
                widthTimer.target = Math.max(widthTimer.target, BlinkFix.getInstance().getFontManager().siyuan16.getStringWidth(desc) + 5);
            }
        }

        widthTimer.update(true);
        heightTimer.update(true);

        float windowX = width / 2f - widthTimer.value / 2, windowY = height / 2f - 60;

        GlStateManager.translate(windowX, windowY, 0);
        StencilUtils.write(false);
        RenderUtils.drawBoundRoundedRect(5, 5, widthTimer.value, heightTimer.value + 3, 5, 0xFFFFFFFF);
        StencilUtils.erase(true);

        RenderUtils.drawRectBound(5, 5, widthTimer.value, 3, headerColor);
        RenderUtils.drawRectBound(5, 8, widthTimer.value, heightTimer.value, bodyColor);

        packet = null;

        int modeY = 8;
        for (Map.Entry<String, byte[]> entry : modes.entrySet()) {
            String mode = entry.getKey();

            if (RenderUtils.isHoveringBound(mouseX, mouseY, windowX + 5, modeY + windowY, 50, 20)) {
                packet = entry.getValue();
                selectX1.target = 5;
                selectX2.target = 55;
                selectY1.target = modeY;
                selectY2.target = modeY + 20;
            }

            BlinkFix.getInstance().getFontManager().siyuan18.drawStringWithShadow(mode, 10, modeY + 3, 0xFFFFFFFF);
            modeY += 20;
        }

        game = null;

        int gameY = 8;
        for (GermModGame game : games) {
            if (RenderUtils.isHoveringBound(mouseX, mouseY, windowX + 55, gameY + windowY, widthTimer.value - 50, 20)) {
                this.game = game;

                selectX1.target = 55;
                selectX2.target = widthTimer.value + 5;
                selectY1.target = gameY;
                selectY2.target = gameY + 20;

                float descY = originHeight + 2;
                for (String desc : game.getDescription().split("\n")) {
                    BlinkFix.getInstance().getFontManager().siyuan16.drawStringWithShadow(desc, 8, descY + 3, 0xFFFFFFFF);
                    descY += 10;
                }
            }

            BlinkFix.getInstance().getFontManager().siyuan18.drawStringWithShadow(game.getDisplayName(), 60, gameY + 3, 0xFFFFFFFF);
            gameY += 20;
        }

        selectX1.update(true);
        selectX2.update(true);
        selectY1.update(true);
        selectY2.update(true);
        drawRect(selectX1.value, selectY1.value, selectX2.value, selectY2.value, selected);

        StencilUtils.dispose();
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (packet != null) {
                GermMod.sendPacket(packet);
            }

            if (game != null) {
                GermMod.sendPacket(GermPacketUtils.buildJoinGamePacket(game.getEntry(), game.getSid()));

                mc.displayGuiScreen(null);
            }
        }
    }
}
