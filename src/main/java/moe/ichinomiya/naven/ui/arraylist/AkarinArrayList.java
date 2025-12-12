//package axis.shiyan.wei.bluearchive.blinkfix.ui.arraylist;
//
//
//import moe.ichinomiya.naven.BlinkFix;
//import modules.moe.ichinomiya.naven.Category;
//import modules.moe.ichinomiya.naven.ModuleManager;
//import render.impl.modules.moe.ichinomiya.naven.ArrayListMod;
//import utils.moe.ichinomiya.naven.RenderUtils;
//import utils.moe.ichinomiya.naven.SmoothAnimationTimer;
//import font.utils.moe.ichinomiya.naven.FontManager;
//import font.utils.moe.ichinomiya.naven.GlyphPageFontRenderer;
//import font.utils.moe.ichinomiya.naven.IconManager;
//import net.minecraft.client.Minecraft;
//import modules.moe.ichinomiya.naven.Module;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.GlStateManager;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class AkarinArrayList {
//    private final ArrayListMod arrayListMod;
//    List<Module> modules;
//
//    public AkarinArrayList(ArrayListMod arrayListMod) {
//        this.arrayListMod = arrayListMod;
//    }
//
//    public String getModuleDisplayName(Module module) {
//        String name = arrayListMod.prettyModuleName.getCurrentValue() ? module.getPrettyName() : module.getName();
//        return name + (module.getSuffix() == null ? "" : (" \2477" + module.getSuffix()));
//    }
//
//    public void draw() {
//        draw(false);
//    }
//
//    public void draw(boolean shader) {
//        if (Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().gameSettings.showDebugInfo) {
//            return;
//        }
//
//        ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();
//        FontManager fontManager = BlinkFix.getInstance().getFontManager();
//        GlyphPageFontRenderer font = fontManager.poppinsregular20;
//
//        if (Module.update && !shader) {
//            modules = new ArrayList<>(moduleManager.getModules());
//
//            if (arrayListMod.hideRenderModules.getCurrentValue()) {
//                modules.removeIf(module -> module.getCategory() == Category.RENDER);
//            }
//            modules.removeIf(module -> {
//                String moduleName = module.getName();
//                return moduleName.equals("NoJumpDelay") ||
//                        moduleName.equals("FastThrow") ||
//                        moduleName.equals("TargetStrafe") ||
//                        moduleName.equals("FastWeb") ||
//                        moduleName.equals("NoLiquid") ||
//                        moduleName.equals("AutoStuck") ||
//                        moduleName.equals("AutoReport") ||
//                        moduleName.equals("KillSay") ||
//                        moduleName.equals("Extinguisher") ||
//                        moduleName.equals("HackerDetector") ||
//                        moduleName.equals("MouseTweaker") ||
//                        moduleName.equals("KeepSprint") ||
//                        moduleName.equals("AimAssist") ||
//                        moduleName.equals("AutoArmor") ||
//                        moduleName.equals("NoSmoothCamera");
//
//            });
//
//            modules.sort((o1, o2) -> {
//                int o1Width = font.getStringWidth(getModuleDisplayName(o1));
//                int o2Width = font.getStringWidth(getModuleDisplayName(o2));
//                return Integer.compare(o2Width, o1Width);
//            });
//        }
//
//        if (modules == null) {
//            return;
//        }
//
//        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
//
//        GlStateManager.pushMatrix();
//        int maxWidth = modules.isEmpty() ? 0 : font.getStringWidth(getModuleDisplayName(modules.get(0)));
//
//        if (arrayListMod.mode.isCurrentMode("Right")) {
//            GlStateManager.translate(sr.getScaledWidth() - maxWidth - 6 + arrayListMod.xOffset.getCurrentValue(), arrayListMod.yOffset.getCurrentValue(), 0);
//        } else {
//            GlStateManager.translate(3 + arrayListMod.xOffset.getCurrentValue(), arrayListMod.yOffset.getCurrentValue(), 0);
//        }
//
//        float totalHeight = 0;
//        List<ModuleInfo> moduleInfos = new ArrayList<>();
//        float spacing = arrayListMod.moduleSpacing.getCurrentValue();
//
//        for (Module module : modules) {
//            SmoothAnimationTimer animation = module.getAnimation();
//
//            if (!shader) {
//                if (module.isEnabled()) {
//                    animation.target = 100;
//                } else {
//                    animation.target = 0;
//                }
//                animation.update(true);
//            }
//
//            if (animation.value > 0) {
//                String text = getModuleDisplayName(module);
//                int stringWidth = font.getStringWidth(text);
//                float moduleHeight = animation.value / 7f;
//
//                moduleInfos.add(new ModuleInfo(module, text, stringWidth, moduleHeight, totalHeight, animation.value));
//                if (arrayListMod.separateBackgrounds.getCurrentValue()) {
//                    totalHeight += moduleHeight;
//                } else {
//                    totalHeight += moduleHeight + spacing;
//                }
//            }
//        }
//
//        if (!moduleInfos.isEmpty() && !shader && arrayListMod.showBackground.getCurrentValue()) {
//            drawContinuousFrostedBackground(moduleInfos, maxWidth, spacing);
//        }
//
//        for (int i = 0; i < moduleInfos.size(); i++) {
//            ModuleInfo info = moduleInfos.get(i);
//            GlStateManager.pushMatrix();
//            float left = -info.stringWidth * (1 - info.animationValue / 100f);
//            float right = maxWidth - (info.stringWidth * (info.animationValue / 100f));
//            float textY = info.positionY;
//            if (arrayListMod.separateBackgrounds.getCurrentValue() && i > 0) {
//                textY += spacing * i;
//            }
//
//            GlStateManager.translate(arrayListMod.mode.isCurrentMode("Left") ? left : right, textY, 0);
//            if (!shader) {
//                int color = getModuleColor(info.module, info.positionY, i);
//                float alpha = info.animationValue / 100f;
//                color = RenderUtils.reAlpha(color, alpha + 0.03f);
//                font.drawStringWithShadow(info.text, -1, 0, color);
//            }
//            GlStateManager.popMatrix();
//        }
//
//        GlStateManager.popMatrix();
//    }
//
//    private int getModuleColor(Module module, float positionY, int index) {
//        if (arrayListMod.colorMode.isCurrentMode("Rainbow")) {
//            return RenderUtils.getRainbowOpaque((int) (-positionY * arrayListMod.rainbowOffset.getCurrentValue()),
//                    1f, 1f, (21 - arrayListMod.rainbowSpeed.getCurrentValue()) * 1000);
//        } else if (arrayListMod.colorMode.isCurrentMode("White")) {
//            return 0xffffffff;
//        } else if (arrayListMod.colorMode.isCurrentMode("Custom")) {
//            int r1 = (int) arrayListMod.customR.getCurrentValue();
//            int g1 = (int) arrayListMod.customG.getCurrentValue();
//            int b1 = (int) arrayListMod.customB.getCurrentValue();
//
//            int r2 = (int) arrayListMod.customR2.getCurrentValue();
//            int g2 = (int) arrayListMod.customG2.getCurrentValue();
//            int b2 = (int) arrayListMod.customB2.getCurrentValue();
//            long frameCount = System.currentTimeMillis() / 16;
//            float speed = arrayListMod.colorSpeed.getCurrentValue();
//            float blend = (float) ((Math.sin(frameCount * speed * 0.01f) + 1.0f) / 2.0f);
//            int r = (int)(r1 * (1 - blend) + r2 * blend);
//            int g = (int)(g1 * (1 - blend) + g2 * blend);
//            int b = (int)(b1 * (1 - blend) + b2 * blend);
//            r = Math.max(0, Math.min(255, r));
//            g = Math.max(0, Math.min(255, g));
//            b = Math.max(0, Math.min(255, b));
//            return 0xff000000 | (r << 16) | (g << 8) | b;
//        }
//
//        return 0xffffffff;
//    }
//
//    private int getModuleGlowColor(Module module, float positionY, int index) {
//        int baseColor = getModuleColor(module, positionY, index);
//        int r = (baseColor >> 16) & 0xFF;
//        int g = (baseColor >> 8) & 0xFF;
//        int b = baseColor & 0xFF;
//        float brightnessBoost = 1.3f;
//        float saturationReduce = 0.7f;
//        r = Math.min(255, (int)(r * brightnessBoost));
//        g = Math.min(255, (int)(g * brightnessBoost));
//        b = Math.min(255, (int)(b * brightnessBoost));
//        r = (int)(r * saturationReduce + 255 * (1 - saturationReduce));
//        g = (int)(g * saturationReduce + 255 * (1 - saturationReduce));
//        b = (int)(b * saturationReduce + 255 * (1 - saturationReduce));
//
//        r = Math.min(255, Math.max(0, r));
//        g = Math.min(255, Math.max(0, g));
//        b = Math.min(255, Math.max(0, b));
//
//        return (r << 16) | (g << 8) | b;
//    }
//
//    private void drawContinuousFrostedBackground(List<ModuleInfo> moduleInfos, int maxWidth, float spacing) {
//        if (moduleInfos.isEmpty()) return;
//
//        float padding = 2f;
//        float cornerRadius = 3f;
//        float overallWidth = maxWidth + padding * 2;
//
//        for (int i = 0; i < moduleInfos.size(); i++) {
//            ModuleInfo info = moduleInfos.get(i);
//
//            float moduleBgWidth = info.stringWidth + padding * 2;
//
//            float bgXOffset = 0;
//            if (arrayListMod.mode.isCurrentMode("Right")) {
//                bgXOffset = overallWidth - moduleBgWidth;
//            }
//
//            float bgY, bgHeight;
//            if (arrayListMod.separateBackgrounds.getCurrentValue()) {
//                bgY = info.positionY + spacing * i;
//                bgHeight = info.moduleHeight;
//            } else {
//                bgY = info.positionY;
//                bgHeight = info.moduleHeight;
//                if (i < moduleInfos.size() - 1) {
//                    bgHeight += spacing;
//                }
//            }
//
//            // 绘制发光效果（在背景之前）
//            if (arrayListMod.better.getCurrentValue() && arrayListMod.glowEffect.getCurrentValue()) {
//                drawSimpleGlowEffect(bgXOffset - padding, bgY, moduleBgWidth, bgHeight, cornerRadius, info.module, info.positionY, i);
//            }
//
//            // 绘制模块背景
//            if (arrayListMod.better.getCurrentValue()) {
//                RenderUtils.drawRoundedRect(
//                        bgXOffset - padding,
//                        bgY,
//                        moduleBgWidth,
//                        bgHeight,
//                        cornerRadius,
//                        new Color(255, 255, 255, 30)
//                );
//            } else {
//                RenderUtils.drawRect(bgXOffset - padding, bgY,
//                        bgXOffset + moduleBgWidth, bgY + bgHeight,
//                        new Color(255, 255, 255, 30).getRGB());
//            }
//
//            // 在模块背景右侧绘制圆角小方块
//            drawRoundedSquareBesideModule(bgXOffset - padding, bgY, moduleBgWidth, bgHeight, cornerRadius, info.module, info.positionY, i);
//        }
//    }
//
//    /**
//     * 简单的发光效果 - 只在背景边缘显示颜色
//     */
//    private void drawSimpleGlowEffect(float x, float y, float width, float height, float radius, Module module, float positionY, int index) {
//        float intensity = arrayListMod.glowIntensity.getCurrentValue();
//        int glowColor = getModuleGlowColor(module, positionY, index);
//        float glowSize = 1.2f * intensity;
//        int glowAlpha = (int) (60 * intensity);
//
//        RenderUtils.drawRoundedRect(
//                x - glowSize,
//                y - glowSize,
//                width + glowSize * 2,
//                height + glowSize * 2,
//                radius + glowSize,
//                RenderUtils.reAlpha(glowColor, glowAlpha / 255f)
//        );
//    }
//
//    /**
//     * 优化的发光颜色计算
//     *
//
//     /**
//     * 小方块的简单发光效果
//     */
//    private void drawRoundedSquareBesideModule(float bgX, float bgY, float bgWidth, float bgHeight, float radius, Module module, float positionY, int index) {
//        float squareSize = bgHeight;
//        float squareX = bgX + bgWidth + 3;
//        float squareY = bgY;
//        Color squareColor = new Color(255, 255, 255, 30);
//
//        if (arrayListMod.better.getCurrentValue()) {
//            RenderUtils.drawRoundedRect(
//                    squareX,
//                    squareY,
//                    squareSize,
//                    squareSize,
//                    radius,
//                    squareColor
//            );
//        } else {
//            RenderUtils.drawRect(
//                    squareX, squareY,
//                    squareX + squareSize, squareY + squareSize,
//                    squareColor.getRGB()
//            );
//        }
//
//        drawIconInSquare(squareX, squareY, squareSize, module.getCategory());
//
//        // 小方块发光效果
//        if (arrayListMod.glowEffect.getCurrentValue() && arrayListMod.better.getCurrentValue()) {
//            drawSimpleSquareGlow(squareX, squareY, squareSize, radius, module, positionY, index);
//        }
//    }
//
//    /**
//     * 小方块的简单发光效果
//     */
//    private void drawSimpleSquareGlow(float x, float y, float size, float radius, Module module, float positionY, int index) {
//        float intensity = arrayListMod.glowIntensity.getCurrentValue();
//        int glowColor = getModuleGlowColor(module, positionY, index);
//
//        float glowSize = 0.8f * intensity;
//        int glowAlpha = (int) (40 * intensity);
//
//        RenderUtils.drawRoundedRect(
//                x - glowSize,
//                y - glowSize,
//                size + glowSize * 2,
//                size + glowSize * 2,
//                radius + glowSize,
//                RenderUtils.reAlpha(glowColor, glowAlpha / 255f)
//        );
//    }
//
//    private void drawIconInSquare(float squareX, float squareY, float squareSize, Category category) {
//        FontManager fontManager = BlinkFix.getInstance().getFontManager();
//        GlyphPageFontRenderer iconFont = fontManager.icons20;
//        String icon = IconManager.getIconForCategory(category);
//        int iconWidth = iconFont.getStringWidth(icon);
//        int iconHeight = 14;
//
//        float iconX = squareX + (squareSize - iconWidth) / 2-1;
//        float iconY = squareY + (squareSize - iconHeight) / 2+3;
//        int iconColor = new Color(255, 255, 255, 200).getRGB();
//        iconFont.drawString(icon, iconX, iconY, iconColor);
//    }
//
//    private void drawRoundedGlowEffectForSquare(float x, float y, float size, float radius, int color) {
//        float intensity = arrayListMod.glowIntensity.getCurrentValue();
//        int layers = (int) (2 * intensity);
//
//        for (int i = 0; i < layers; i++) {
//            float glowSize = i * 0.3f * intensity;
//            int glowAlpha = (int) (30 * (1.0f - i * 0.4f) * intensity);
//
//            RenderUtils.drawRoundedRect(
//                    x - glowSize,
//                    y - glowSize,
//                    size + glowSize * 2,
//                    size + glowSize * 2,
//                    radius + glowSize,
//                    RenderUtils.reAlpha(color, glowAlpha / 255f)
//            );
//        }
//    }
//    private static class ModuleInfo {
//        Module module;
//        String text;
//        int stringWidth;
//        float moduleHeight;
//        float positionY;
//        float animationValue;
//
//        ModuleInfo(Module module, String text, int stringWidth, float moduleHeight, float positionY, float animationValue) {
//            this.module = module;
//            this.text = text;
//            this.stringWidth = stringWidth;
//            this.moduleHeight = moduleHeight;
//            this.positionY = positionY;
//            this.animationValue = animationValue;
//        }
//    }
//
//    public void drawShader() {
//        draw(true);
//    }
//}