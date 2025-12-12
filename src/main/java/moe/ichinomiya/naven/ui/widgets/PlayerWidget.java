package moe.ichinomiya.naven.ui.widgets;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.impl.misc.HackerDetector;
import moe.ichinomiya.naven.modules.impl.misc.Teams;
import moe.ichinomiya.naven.utils.EntityWatcher;
import moe.ichinomiya.naven.utils.FriendManager;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;
import tech.skidonion.obfuscator.annotations.ControlFlowObfuscation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
@ControlFlowObfuscation
public class PlayerWidget extends DraggableWidget {
    final SmoothAnimationTimer height = new SmoothAnimationTimer(0), width = new SmoothAnimationTimer(0);
    final HashMap<String, String> map = new LinkedHashMap<>();
    boolean shouldRender = false;
    BooleanValue value;

    public PlayerWidget(BooleanValue value) {
        super("Players");
        this.value = value;
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        map.clear();

        if (mc.theWorld != null) {
            ArrayList<EntityPlayer> entityPlayers = new ArrayList<>(mc.theWorld.playerEntities);
            entityPlayers.removeIf(entity -> entity.getEntityId() < 0);
            entityPlayers.sort((o1, o2) -> (int) (mc.thePlayer.getDistanceToEntity(o1) * 1000 - mc.thePlayer.getDistanceToEntity(o2) * 1000));

            entityPlayers.forEach(entity -> {
                if (!Teams.isSameTeam(entity) && !FriendManager.isFriend(entity) && entity != mc.thePlayer) {
                    int health = Math.round(entity.getHealth() + entity.getAbsorptionAmount());

                    if (EntityWatcher.getGodAxe().contains(entity)) {
                        map.put("[" + Math.round(mc.thePlayer.getDistanceToEntity(entity)) + "m][" + health + "HP] " + entity.getName(), "God Axe");
                    } else if (EntityWatcher.getEnchantedGApple().contains(entity)) {
                        map.put("[" + Math.round(mc.thePlayer.getDistanceToEntity(entity)) + "m][" + health + "HP] " + entity.getName(), "Enchanted GApple");
                    } else if (HackerDetector.isCheating(entity)) {
                        map.put("[" + Math.round(mc.thePlayer.getDistanceToEntity(entity)) + "m][" + health + "HP] " + entity.getName(), "Hacker");
                    }
                }
            });
        }

        shouldRender = (!map.isEmpty() || mc.currentScreen instanceof GuiChat) && value.getCurrentValue();

        if (!shouldRender) {
            height.target = 0;
            width.target = 0;
        }
    }

    @Override
    public void renderBody() {
        GlyphPageFontRenderer font = BlinkFix.getInstance().getFontManager().siyuan16;
        font.drawStringWithShadow(name, 2, 2, 0xFFFFFFFF);

        if (shouldRender) {
            height.target = 12;
            width.target = font.getStringWidth(name);

            map.forEach((key, value) -> {
                width.target = Math.max(width.target, font.getStringWidth(key + " " + value) + 10);
            });

            map.forEach((key, value) -> {
                font.drawStringWithShadow(key, 2, 2 + height.target, 0xFFFFFFFF);
                font.drawStringWithShadow(value, 2 + width.target - font.getStringWidth(value), 2 + height.target, 0xFFFFFFFF);

                height.target += 10;
            });
        } else {
            height.target = 0;
            width.target = 0;
        }

        height.update(true);
        width.update(true);
    }

    @Override
    public float getWidth() {
        return width.value + 6;
    }

    @Override
    public float getHeight() {
        return height.value + 6;
    }

    @Override
    public boolean shouldRender() {
        return shouldRender || width.value > 1 || height.value > 1;
    }
}
