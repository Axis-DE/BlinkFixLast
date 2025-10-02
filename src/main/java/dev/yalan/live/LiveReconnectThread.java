package dev.yalan.live;

import net.minecraft.client.Minecraft;

public class LiveReconnectThread extends Thread {
    @Override
    public void run() {
        final Minecraft mc = Minecraft.getMinecraft();
        final LiveClient live = LiveClient.INSTANCE;

        while (mc.running && !isInterrupted()){
            if (!live.isOpen()) {
                live.connect();
            }

            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
