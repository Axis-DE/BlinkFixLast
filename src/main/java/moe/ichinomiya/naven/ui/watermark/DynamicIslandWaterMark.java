//package axis.shiyan.wei.bluearchive.blinkfix.ui.watermark;
//
//import moe.ichinomiya.naven.BlinkFix;
//import utils.moe.ichinomiya.naven.RenderUtils;
//import utils.moe.ichinomiya.naven.StencilUtils;
//import font.utils.moe.ichinomiya.naven.FontManager;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.ScaledResolution;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//public class DynamicIslandWaterMark extends WaterMark {
//    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//    private final int backgroundColor = 0xCC000000;
//    private final int accentColor = 0xFF007AFF;
//    private final int textColor = 0xFFFFFFFF;
//
//    // 动画状态
//    private int currentWidth = 0;
//    private int targetWidth = 180;
//    private long lastUpdateTime = 0;
//
//    // 尺寸配置
//    private final int height = 25;
//    private final int cornerRadius = 12;
//
//    @Override
//    public void render() {
//        if (shouldSkipRender()) return;
//
//        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
//        int screenWidth = sr.getScaledWidth();
//        int x = screenWidth / 2; // 屏幕中央
//        int y = 15; // 距离顶部15像素
//
//        // 平滑宽度动画
//        updateWidthAnimation();
//
//        // 绘制灵动岛
//        drawDynamicIsland(x, y);
//    }
//
//    private void drawDynamicIsland(int centerX, int y) {
//        int width = currentWidth;
//        int x = centerX - width / 2;
//
//        // 使用模板缓冲实现圆角效果
//        StencilUtils.write(false);
//        RenderUtils.drawBoundRoundedRect(x, y, width, height, cornerRadius, 0xFFFFFFFF);
//        StencilUtils.erase(true);
//
//        // 绘制黑色半透明背景
//        RenderUtils.drawRoundedRect(x, y, width, height, cornerRadius, backgroundColor);
//
//        // 绘制顶部装饰线（类似iPhone灵动岛）
//        RenderUtils.drawRect(x, y, width, 2, accentColor);
//
//        // 绘制内容
//        drawContent(x, y, width, height);
//
//        StencilUtils.dispose();
//    }
//
//    private void drawContent(int x, int y, int width, int height) {
//        FontManager fontManager = BlinkFix.getInstance().getFontManager();
//        int textY = y + (height - 8) / 2; // 垂直居中
//
//        StringBuilder content = new StringBuilder();
//
//        // 客户端名称
//        content.append("BlinkFix");
//
//        // FPS显示
//        content.append(" | ").append(Minecraft.getDebugFPS()).append(" FPS");
//
//        // 时间显示
//        content.append(" | ").append(timeFormat.format(new Date()));
//
//        String text = content.toString();
//        int textWidth = fontManager.poppinsregular15.getStringWidth(text);
//        int textX = x + (width - textWidth) / 2; // 水平居中
//
//        fontManager.poppinsregular15.drawString(text, textX, textY, textColor);
//    }
//
//    private void updateWidthAnimation() {
//        long currentTime = System.currentTimeMillis();
//        long delta = currentTime - lastUpdateTime;
//
//        if (delta > 16) { // 约60FPS
//            // 计算目标宽度基于内容
//            String content = "BlinkFix | " + Minecraft.getDebugFPS() + " FPS | " + timeFormat.format(new Date());
//            FontManager fontManager = BlinkFix.getInstance().getFontManager();
//            int contentWidth = fontManager.poppinsregular15.getStringWidth(content) + 20; // 加上边距
//
//            targetWidth = Math.max(120, Math.min(300, contentWidth)); // 限制在120-300之间
//
//            if (currentWidth < targetWidth) {
//                currentWidth = Math.min(currentWidth + 5, targetWidth);
//            } else if (currentWidth > targetWidth) {
//                currentWidth = Math.max(currentWidth - 5, targetWidth);
//            }
//            lastUpdateTime = currentTime;
//        }
//    }
//
//    private boolean shouldSkipRender() {
//        return Minecraft.getMinecraft().gameSettings.hideGUI ||
//                Minecraft.getMinecraft().gameSettings.showDebugInfo;
//    }
//
//    @Override
//    public void renderShader() {
//        if (shouldSkipRender()) return;
//
//        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
//        int screenWidth = sr.getScaledWidth();
//        int x = screenWidth / 2 - currentWidth / 2;
//        int y = 15;
//
//        RenderUtils.drawBoundRoundedRect(x, y, currentWidth, height, cornerRadius, 0xFFFFFFFF);
//    }
//}