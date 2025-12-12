package moe.ichinomiya.naven.ui.widgets;

import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import dsj.smtc.SmtcLoader;
import net.minecraft.client.gui.GuiChat;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MusicWidget extends DraggableWidget {
    final SmoothAnimationTimer height = new SmoothAnimationTimer(0), width = new SmoothAnimationTimer(0);
    final HashMap<String, String> musicInfo = new LinkedHashMap<>();
    boolean shouldRender = false;
    BooleanValue value;
    private Thread musicThread;
    private volatile boolean running = false;
    private volatile String currentTitle = "";
    private volatile long currentPos = 0;
    private volatile long currentDur = 0;
    private volatile int currentB64Len = 0;
    private volatile boolean hasMedia = false;
    public MusicWidget(BooleanValue value) {
        super("Music Info");
        this.value = value;
        startMusicThread();
    }
    private void startMusicThread() {
        running = true;
        musicThread = new Thread(() -> {
            while (running) {
                try {
                    String info = SmtcLoader.getSmtcInfo();
                    String[] parts = info.split("\\|", -1);

                    if (parts.length >= 4 && !"No media".equals(parts[0])) {
                        currentTitle = parts[0];
                        currentPos = Long.parseLong(parts[1]);
                        currentDur = Long.parseLong(parts[2]);
                        String base64 = parts[3];
                        currentB64Len = base64.isEmpty() ? 0 : base64.length();
                        hasMedia = true;
                    } else {
                        hasMedia = false;
                        currentTitle = "No media playing";
                        currentPos = 0;
                        currentDur = 0;
                        currentB64Len = 0;
                    }

                    // 更新间隔
                    Thread.sleep(500);
                } catch (Exception e) {
                    // 忽略异常，继续循环
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        });
        musicThread.setDaemon(true);
        musicThread.setName("MusicInfo-Thread");
        musicThread.start();
    }

    public void onUpdate() {
        musicInfo.clear();

        if (hasMedia) {
            String currentTimeStr = String.format("%02d:%02d", currentPos / 60, currentPos % 60);
            String totalTimeStr = String.format("%02d:%02d", currentDur / 60, currentDur % 60);
            String progress = String.format("%s / %s", currentTimeStr, totalTimeStr);
            int progressPercent = currentDur > 0 ? (int) ((currentPos * 100) / currentDur) : 0;
            String progressBar = buildProgressBar(progressPercent, 15);
            musicInfo.put("Title", currentTitle);
            musicInfo.put("Progress", progress);
            musicInfo.put("Progress Bar", progressBar);
            musicInfo.put("Cover", currentB64Len > 0 ? "✓" : "✗");

            shouldRender = value.getCurrentValue();
        } else {
            musicInfo.put("Status", currentTitle);
            shouldRender = value.getCurrentValue() && (mc.currentScreen instanceof GuiChat);
        }

        if (!shouldRender) {
            height.target = 0;
            width.target = 0;
        }
    }

    private String buildProgressBar(int percent, int length) {
        int filled = (percent * length) / 100;
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("] ");
        bar.append(percent).append("%");
        return bar.toString();
    }

    @Override
    public void renderBody() {
        GlyphPageFontRenderer font = getFontRenderer();
        font.drawStringWithShadow(name, 2, 2, 0xFFFFFFFF);

        if (shouldRender) {
            height.target = 12;
            width.target = font.getStringWidth(name);
            for (HashMap.Entry<String, String> entry : musicInfo.entrySet()) {
                String displayText = entry.getKey() + ": " + entry.getValue();
                int lineWidth = font.getStringWidth(displayText) + 10;
                width.target = Math.max(width.target, lineWidth);
            }
            int yOffset = 12;
            for (HashMap.Entry<String, String> entry : musicInfo.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String displayText = key + ": " + value;

                int color = getColorForInfo(key, value);
                font.drawStringWithShadow(displayText, 2, 2 + yOffset, color);

                yOffset += 10;
                height.target += 10;
            }
        } else {
            height.target = 0;
            width.target = 0;
        }

        height.update(true);
        width.update(true);
    }

    private int getColorForInfo(String key, String value) {
        switch (key) {
            case "Title":
                return 0xFFFFFF00;
            case "Progress":
                return 0xFF00FFFF;
            case "Progress Bar":
                return 0xFF00FF00;
            case "Cover":
                return value.equals("✓") ? 0xFF00FF00 : 0xFFFF0000;
            case "Status":
                return 0xFFFFA500;
            default:
                return 0xFFFFFFFF;
        }
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

    public void onShutdown() {
        running = false;
        if (musicThread != null && musicThread.isAlive()) {
            musicThread.interrupt();
        }
    }

    private GlyphPageFontRenderer getFontRenderer() {
        return null;
    }
}