package moe.ichinomiya.naven.ui.cooldown;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.EventRender2D;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedList;

public class CooldownBarManager {
    private final LinkedList<CooldownBar> bars = new LinkedList<>();

    public void addBar(CooldownBar bar) {
        if (!bars.contains(bar)) {
            bars.addLast(bar);
        }
    }

    @EventTarget(Priority.LOW)
    public void onRender(EventRender2D e) {
        bars.removeIf(CooldownBar::isExpired);

        GlStateManager.pushMatrix();
        ScaledResolution resolution = e.getResolution();
        GlStateManager.translate(resolution.getScaledWidth() / 2f - 50, resolution.getScaledHeight() / 2f - 100, 0);

        int counter = 0;
        for (CooldownBar bar : bars) {
            GlStateManager.pushMatrix();
            bar.getYAnimation().target = counter++ * 20f;
            bar.getYAnimation().update(true);
            GlStateManager.translate(0, bar.getYAnimation().value, 0);
            bar.render();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }
}
