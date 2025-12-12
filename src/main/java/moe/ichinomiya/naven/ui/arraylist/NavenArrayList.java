package moe.ichinomiya.naven.ui.arraylist;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleManager;
import moe.ichinomiya.naven.modules.impl.render.ArrayListMod;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.font.FontManager;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import tech.skidonion.obfuscator.annotations.ControlFlowObfuscation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
@ControlFlowObfuscation
public class NavenArrayList {
    private final ArrayListMod arrayListMod;
    List<Module> modules;

    public NavenArrayList(ArrayListMod arrayListMod) {
        this.arrayListMod = arrayListMod;
    }

    public String getModuleDisplayName(Module module) {
        String name = arrayListMod.prettyModuleName.getCurrentValue() ? module.getPrettyName() : module.getName();
        return name + (module.getSuffix() == null ? "" : (" \2477" + module.getSuffix()));
    }

    public void draw() {
        draw(false);
    }

    public void draw(boolean shader) {
        if (Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();
        FontManager fontManager = BlinkFix.getInstance().getFontManager();
        GlyphPageFontRenderer font = fontManager.regular16;

        if (Module.update && !shader) {
            modules = new ArrayList<>(moduleManager.getModules());
            if (arrayListMod.hideRenderModules.getCurrentValue()) {
                modules.removeIf(module -> module.getCategory() == Category.RENDER);
            }
            modules.sort((o1, o2) -> {
                int o1Width = font.getStringWidth(getModuleDisplayName(o1));
                int o2Width = font.getStringWidth(getModuleDisplayName(o2));
                return Integer.compare(o2Width, o1Width);
            });
        }

        if (modules == null) {
            return;
        }

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        GlStateManager.pushMatrix();
        int maxWidth = modules.isEmpty() ? 0 : font.getStringWidth(getModuleDisplayName(modules.get(0)));

        if (arrayListMod.mode.isCurrentMode("Right")) {
            GlStateManager.translate(sr.getScaledWidth() - maxWidth - 6 + arrayListMod.xOffset.getCurrentValue(), arrayListMod.yOffset.getCurrentValue(), 0);
        } else {
            GlStateManager.translate(3 + arrayListMod.xOffset.getCurrentValue(), arrayListMod.yOffset.getCurrentValue(), 0);
        }

        float height = 0;
        for (Module module : modules) {
            SmoothAnimationTimer animation = module.getAnimation();

            if (!shader) {
                if (module.isEnabled()) {
                    animation.target = 100;
                } else {
                    animation.target = 0;
                }

                animation.update(true);
            }

            if (animation.value > 0) {
                String text = getModuleDisplayName(module);
                int stringWidth = font.getStringWidth(text);

                GlStateManager.pushMatrix();
                float left = -stringWidth * (1 - animation.value / 100f);
                float right = maxWidth - (stringWidth * (animation.value / 100f));

                GlStateManager.translate(arrayListMod.mode.isCurrentMode("Left") ? left : right, 0, 0);
                RenderUtils.drawRectBound(0, height + 2, stringWidth + 3, animation.value / 10f, shader ? 0xFFFFFFFF : new Color(0, 0, 0, 100).getRGB());

                if (!shader) {
                    int color = 0xffffffff;

                    if (arrayListMod.rainbow.getCurrentValue()) {
                        color = RenderUtils.getRainbowOpaque((int) (-height * arrayListMod.rainbowOffset.getCurrentValue()), 1f, 1f, (21 - arrayListMod.rainbowSpeed.getCurrentValue()) * 1000);
                    }

                    float alpha = animation.value / 100f;

                    color = RenderUtils.reAlpha(color, alpha + 0.03f);
                    font.drawStringWithShadow(text, 0, height + 1, color);
                }
                GlStateManager.popMatrix();

                height += animation.value / 10f;
            }
        }

        GlStateManager.popMatrix();
    }

    public void drawShader() {
        draw(true);
    }
}
