package moe.ichinomiya.naven.utils;

import com.google.common.util.concurrent.AtomicDouble;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventClientInit;
import moe.ichinomiya.naven.events.impl.EventTick;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RawInput extends MouseHelper {
    private static final Logger logger = LogManager.getLogger(RawInput.class);
    private static final Set<Mouse> mice = new HashSet<>();

    private final AtomicDouble dx = new AtomicDouble();
    private final AtomicDouble dy = new AtomicDouble();

    @Override
    public void mouseXYChange() {
        this.deltaX = (int) this.dx.getAndSet(0.0);
        this.deltaY = (int) (-this.dy.getAndSet(0.0));
    }

    @EventTarget
    public void onClientInit(EventClientInit e) {
        logger.warn("RawInput is enabled, this may cause some issues!");
        Minecraft.getMinecraft().mouseHelper = this;
        mice.addAll(this.getMice(ControllerEnvironment.getDefaultEnvironment()));
    }

    @EventTarget
    public void onTicks(EventTick e) {
        if (e.getType() == EventType.PRE) {
            mice.forEach(mouse -> {
                mouse.poll();
                if (Minecraft.getMinecraft().currentScreen == null) {
                    this.dx.addAndGet(mouse.getX().getPollData());
                    this.dy.addAndGet(mouse.getY().getPollData());
                }
            });
        }
    }

    private Set<Mouse> getMice(ControllerEnvironment env) {
        return Arrays.stream(env.getControllers()).filter(Mouse.class::isInstance).map(Mouse.class::cast).collect(Collectors.toSet());
    }
}
