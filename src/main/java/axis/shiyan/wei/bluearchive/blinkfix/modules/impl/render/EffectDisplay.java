package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRenderUI;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRespawn;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventShader;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.StencilUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.font.GlyphPageFontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "EffectDisplay", description = "Displays potion effects on the HUD", category = Category.RENDER)
public class EffectDisplay extends Module {
    protected static final ResourceLocation inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");
    public List<PotionEffect> effects = new CopyOnWriteArrayList<>();

    int headerColor = new Color(150, 45, 45, 255).getRGB();
    int bodyColor = new Color(0, 0, 0, 80).getRGB();

    @Override
    public boolean onEnable() {
        effects.clear();
        return false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        effects.clear();
    }

    ScaledResolution resolution;

    @EventTarget
    public void onShader(EventShader e) {
        if (resolution != null) {
            for (PotionEffect effect : effects) {
                float x = effect.xTimer.value;
                float y = effect.yTimer.value;

                RenderUtils.drawBoundRoundedRect(x + 2, y + 2, effect.width - 2, 28, 5, 0xFFFFFFFF);
            }
        }
    }

    @EventTarget
    public void onRender(EventRenderUI e) {
        resolution = e.getResolution();
        Collection<PotionEffect> activePotionEffects = mc.thePlayer.getActivePotionEffects();

        for (PotionEffect effect : activePotionEffects) {
            if (!effects.contains(effect)) {
                effects.add(effect);
            }
        }

        int startY = resolution.getScaledHeight() / 2 - effects.size() * 16;
        for (PotionEffect effect : effects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];

            String text = getDisplayName(effect);

            if (effect.yTimer.value == -1) {
                effect.yTimer.value = startY;
            }

            effect.maxDuration = Math.max(effect.getDuration(), effect.maxDuration);

            GlyphPageFontRenderer siyuan13 = BlinkFix.getInstance().getFontManager().siyuan13;
            GlyphPageFontRenderer siyuan16 = BlinkFix.getInstance().getFontManager().siyuan16;

            effect.width = 25 + siyuan16.getStringWidth(text) + 20;

            float x = effect.xTimer.value;
            float y = effect.yTimer.value;

            if (effect.shouldDisappear) {
                if (x <= -effect.width - 20) {
                    effects.remove(effect);
                }
            } else {
                effect.durationTimer.target = (float) effect.getDuration() / effect.maxDuration * effect.width;

                if (effect.durationTimer.value <= 0) {
                    effect.durationTimer.value = effect.durationTimer.target;
                }

                effect.xTimer.target = 10;
                effect.yTimer.target = startY;
                effect.yTimer.update(true);
            }

            if (!activePotionEffects.contains(effect) && !effect.shouldDisappear) {
                effect.shouldDisappear = true;
                effect.xTimer.target = -effect.width - 20;
            }

            effect.durationTimer.update(true);
            effect.xTimer.update(true);

            StencilUtils.write(false);
            RenderUtils.drawBoundRoundedRect(x + 2, y + 2, effect.width - 2, 28, 5, 0xFFFFFFFF);

            StencilUtils.erase(true);

            RenderUtils.drawRectBound(x, y, effect.width, 30, bodyColor);
            RenderUtils.drawRectBound(x, y, effect.durationTimer.value, 30, bodyColor);

            RenderUtils.drawBoundRoundedRect(x + effect.width - 10, y + 7, 5, 18, 2, headerColor);

            siyuan16.drawStringWithShadow(text, x + 27, y + 4, headerColor);
            String duration = Potion.getDurationString(effect);
            siyuan13.drawStringWithShadow(duration, x + 27, y + 16, 0xFFFFFFFF);

            if (potion.hasStatusIcon()) {
                mc.getTextureManager().bindTexture(inventoryBackground);
                int i1 = potion.getStatusIconIndex();
                mc.ingameGUI.drawTexturedModalRect(x + 6, y + 7, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
            }
            StencilUtils.dispose();
            startY += 34;
        }
    }

    public String getDisplayName(PotionEffect activePotionEffect) {
        String effectName = I18n.format(activePotionEffect.getEffectName());
        String amplifierName;
        if (activePotionEffect.getAmplifier() == 0) {
            amplifierName = "";
        } else if (activePotionEffect.getAmplifier() == 1) {
            amplifierName = " " + I18n.format("enchantment.level.2");
        } else if (activePotionEffect.getAmplifier() == 2) {
            amplifierName = " " + I18n.format("enchantment.level.3");
        } else if (activePotionEffect.getAmplifier() == 3) {
            amplifierName = " " + I18n.format("enchantment.level.4");
        } else {
            amplifierName = " " + activePotionEffect.getAmplifier();
        }

        return effectName + amplifierName;
    }
}
