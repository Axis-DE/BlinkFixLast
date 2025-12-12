//package axis.shiyan.wei.bluearchive.blinkfix.ui.arraylist;
//
//import moe.ichinomiya.naven.BlinkFix;
//import modules.moe.ichinomiya.naven.Category;
//import modules.moe.ichinomiya.naven.ModuleManager;
//import render.impl.modules.moe.ichinomiya.naven.ArrayListMod;
//import font.utils.moe.ichinomiya.naven.FontManager;
//import font.utils.moe.ichinomiya.naven.GlyphPageFontRenderer;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.GlStateManager;
//import utils.moe.ichinomiya.naven.RenderUtils;
//import utils.moe.ichinomiya.naven.SmoothAnimationTimer;
//import modules.moe.ichinomiya.naven.Module;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//public class NavenArrayList {
//    private final ArrayListMod arrayListMod;
//    List<Module> modules;
//
//    public NavenArrayList(ArrayListMod arrayListMod) {
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
//                font.drawStringWithShadow(info.text, 0, 1, color);
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
//        if (arrayListMod.colorMode.isCurrentMode("Rainbow")) {
//            return RenderUtils.getRainbowOpaque(
//                    (int) (module.hashCode() * arrayListMod.rainbowOffset.getCurrentValue()),
//                    0.8f, 0.6f, (21 - arrayListMod.rainbowSpeed.getCurrentValue()) * 1000
//            );
//        } else if (arrayListMod.colorMode.isCurrentMode("White")) {
//            return new Color(255, 255, 255, 50).getRGB();
//        } else if (arrayListMod.colorMode.isCurrentMode("Custom")) {
//            int r1 = (int) arrayListMod.customR.getCurrentValue();
//            int g1 = (int) arrayListMod.customG.getCurrentValue();
//            int b1 = (int) arrayListMod.customB.getCurrentValue();
//
//            int r2 = (int) arrayListMod.customR2.getCurrentValue();
//            int g2 = (int) arrayListMod.customG2.getCurrentValue();
//            int b2 = (int) arrayListMod.customB2.getCurrentValue();
//            float time = System.currentTimeMillis() / 1000f;
//            float speed = arrayListMod.colorSpeed.getCurrentValue();
//            float blend = (float) ((Math.sin(time * speed * 2.0f) + 1.0f) / 2.0f);
//            int r = (int)(r1 * (1 - blend) + r2 * blend);
//            int g = (int)(g1 * (1 - blend) + g2 * blend);
//            int b = (int)(b1 * (1 - blend) + b2 * blend);
//            r = Math.max(0, Math.min(255, r));
//            g = Math.max(0, Math.min(255, g));
//            b = Math.max(0, Math.min(255, b));
//            return 0x4c000000 | (r << 16) | (g << 8) | b;
//        }
//
//        return new Color(100, 150, 255, 50).getRGB();
//    }
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
//            int glowColor = getModuleGlowColor(info.module, info.positionY, i);
//            if (arrayListMod.bloomEffect.getCurrentValue()) {
//                drawBloomEffect(bgXOffset - padding, bgY, moduleBgWidth, bgHeight, cornerRadius, glowColor);
//            } else if (arrayListMod.glowEffect.getCurrentValue()) {
//                if (arrayListMod.better.getCurrentValue()) {
//                    drawRoundedGlowEffect(bgXOffset - padding, bgY, moduleBgWidth, bgHeight, cornerRadius, glowColor);
//                } else {
//                    drawRectGlowEffect(bgXOffset - padding, bgY, moduleBgWidth, bgHeight, glowColor);
//                }
//            }
//
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
//        }
//    }
//
//    private void drawRoundedGlowEffect(float x, float y, float width, float height, float radius, int color) {
//        float intensity = arrayListMod.glowIntensity.getCurrentValue();
//        int layers = (int) (3 * intensity);
//
//        for (int i = 0; i < layers; i++) {
//            float glowSize = i * 0.5f * intensity;
//            int glowAlpha = (int) (50 * (1.0f - i * 0.3f) * intensity);
//
//            RenderUtils.drawRoundedRect(
//                    x - glowSize,
//                    y - glowSize,
//                    width + glowSize * 2,
//                    height + glowSize * 2,
//                    radius + glowSize,
//                    RenderUtils.reAlpha(color, glowAlpha / 255f)
//            );
//        }
//    }
//
//    private void drawRectGlowEffect(float x, float y, float width, float height, int color) {
//        float intensity = arrayListMod.glowIntensity.getCurrentValue();
//        int layers = (int) (3 * intensity);
//
//        for (int i = 0; i < layers; i++) {
//            float glowSize = i * 0.5f * intensity;
//            int glowAlpha = (int) (50 * (1.0f - i * 0.3f) * intensity);
//
//            RenderUtils.drawRect(
//                    x - glowSize,
//                    y - glowSize,
//                    x + width + glowSize * 2,
//                    y + height + glowSize * 2,
//                    RenderUtils.reAlpha(color, glowAlpha / 255f)
//            );
//        }
//    }
//
//    private void drawBloomEffect(float x, float y, float width, float height, float radius, int color) {
//        float intensity = arrayListMod.glowIntensity.getCurrentValue();
//        int bloomLayers = (int) (5 * intensity);
//
//        for (int i = 0; i < bloomLayers; i++) {
//            float bloomSize = i * 0.8f * intensity;
//            int bloomAlpha = (int) (30 * (1.0f - i * 0.2f) * intensity);
//
//            if (arrayListMod.better.getCurrentValue()) {
//                RenderUtils.drawRoundedRect(
//                        x - bloomSize,
//                        y - bloomSize,
//                        width + bloomSize * 2,
//                        height + bloomSize * 2,
//                        radius + bloomSize,
//                        RenderUtils.reAlpha(color, bloomAlpha / 255f)
//                );
//            } else {
//                RenderUtils.drawRect(
//                        x - bloomSize,
//                        y - bloomSize,
//                        x + width + bloomSize * 2,
//                        y + height + bloomSize * 2,
//                        RenderUtils.reAlpha(color, bloomAlpha / 255f)
//                );
//            }
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
//
//    public void drawShader() {
//        draw(true);
//    }
//}