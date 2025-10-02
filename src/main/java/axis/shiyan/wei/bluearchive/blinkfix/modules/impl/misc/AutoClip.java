package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.PlayerUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;

@ModuleInfo(name = "AutoClip", category = Category.MISC, description = "Automatically clip out of cages!")
public class AutoClip extends Module {
    private final static int mainColor = new Color(150, 45, 45, 255).getRGB();
    public static boolean work = false;
    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Fly", "High Jump").build().getModeValue();
    FloatValue speed = ValueBuilder.create(this, "Speed").setDefaultFloatValue(2.5f).setFloatStep(0.1f).setMinFloatValue(0.1f).setMaxFloatValue(6f).setVisibility(() -> mode.isCurrentMode("Fly")).build().getFloatValue();
    FloatValue height = ValueBuilder.create(this, "Jump Distance").setDefaultFloatValue(50).setFloatStep(1).setMinFloatValue(5).setMaxFloatValue(100).setVisibility(() -> mode.isCurrentMode("High Jump")).build().getFloatValue();
    ScaledResolution resolution;
    SmoothAnimationTimer progress = new SmoothAnimationTimer(0, 0.3f);

    @Override
    public void onDisable() {
        work = false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        work = false;

        progress.target = 1;
        progress.value = 0;
        flyDistance = 0;
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        if (work && mc.thePlayer.ticksExisted < 20) {
            resolution = e.getResolution();

            int x = resolution.getScaledWidth() / 2 - 50;
            int y = resolution.getScaledHeight() / 2 + 15;

            progress.update(true);
            RenderUtils.drawBoundRoundedRect(x, y, 100, 5, 2, 0x80000000);
            RenderUtils.drawBoundRoundedRect(x, y, progress.value, 5, 2, mainColor);
        }
    }

    @EventTarget
    public void onShader(EventShader e) {
        if (work && e.getType() == EventType.SHADOW && mc.thePlayer.ticksExisted < 20) {
            int x = resolution.getScaledWidth() / 2 - 50;
            int y = resolution.getScaledHeight() / 2 + 15;
            RenderUtils.drawBoundRoundedRect(x, y, 100, 5, 2, 0xFFFFFFFF);
        }
    }

    @EventTarget
    public void onMove(EventMove e) {
        if (work && mc.thePlayer.ticksExisted <= 4) {
            e.setX(0);
            e.setY(0);
            e.setZ(0);
        }
    }

    double flyDistance = 0;

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (mc.thePlayer.ticksExisted <= 2) {
                BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ);
                BlockPos higherPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 3, mc.thePlayer.posZ);
                if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.glass || mc.theWorld.getBlockState(pos).getBlock() == Blocks.stained_glass || mc.theWorld.getBlockState(higherPos).getBlock() == Blocks.glass || mc.theWorld.getBlockState(higherPos).getBlock() == Blocks.stained_glass) {
                    work = true;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 6, mc.thePlayer.posZ);
                    progress.value = progress.target = 0;
                }
            }

            if (work && mc.thePlayer.ticksExisted > 4 && mc.thePlayer.ticksExisted < 20) {
                progress.target = mc.thePlayer.ticksExisted / 20f * 100;

                if (mode.isCurrentMode("Fly")) {
                    mc.thePlayer.motionY = 0.0;

                    if (PlayerUtils.isMoving()) {
                        PlayerUtils.setSpeed(speed.getCurrentValue());
                    } else {
                        PlayerUtils.setSpeed(0);
                    }

                    if (mc.gameSettings.keyBindJump.pressed) {
                        mc.thePlayer.motionY = speed.getCurrentValue();
                    }
                } else {
                    if (flyDistance < height.getCurrentValue()) {
                        mc.thePlayer.motionY = 6;
                        flyDistance += 6;
                    } else {
                        mc.thePlayer.motionY = 0;
                    }
                }
            }

            if (work && mc.thePlayer.onGround) {
                work = false;
            }
        }
    }
}
