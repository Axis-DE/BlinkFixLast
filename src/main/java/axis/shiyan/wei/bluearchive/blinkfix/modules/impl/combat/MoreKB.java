package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat;

import axis.shiyan.wei.bluearchive.blinkfix.events.EventLivingUpdate;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventAttack;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventWorldUnload;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import java.security.SecureRandom;

@ModuleInfo(name = "MoreKB", description = "", category = Category.COMBAT)
public class MoreKB extends Module {
    public static boolean sprint = true, wTap;
    private boolean isHit = false;
    private long delay = 0L;
    public TimeHelper attackTimer = new TimeHelper();
    private int ticks = 0;

    public ModeValue mode = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(1)
            .setModes("Sprint", "LegitFast")
            .build()
            .getModeValue();


    @Override
    public void onDisable() {
        sprint = true;

    }

    private void resetAll() {
        isHit = false;
        delay = 0L;
        ticks = 0;
    }
    @EventTarget
    public void onWorld(EventWorldUnload eventWorld) {
        resetAll();
    }
    @Override
    public boolean onEnable() {
        sprint = true;
        resetAll();
        return false;
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        wTap = Math.random() * 100 < 100;
    }
    public static SecureRandom secureRandom = new SecureRandom();
    @EventTarget
    public void onUpdate(EventLivingUpdate e){
        if (mode.isCurrentMode("LegitFast")){
            if (isHit){
                mc.thePlayer.sprintingTicksLeft = 0;
            }
        }
        setSuffix(mode.getCurrentMode());
        
    }
    @EventTarget
    public void onEventAttack(EventAttack e) {
        Entity entity = e.getTarget();

        if (entity instanceof EntityLivingBase) {
            ticks = 2;
        }

        if (entity != null) {
            double x = mc.thePlayer.posX - entity.posX;
            double z = mc.thePlayer.posZ - entity.posZ;
            float calcYaw = (float) (MathHelper.clamp(z, x) * 180.0 / Math.PI - 90.0);
            float diffY = Math.abs(MathHelper.wrapAngleTo180_float(calcYaw - entity.getRotationYawHead()));
            if (!(diffY > 120.0F)) {
                if (mode.getCurrentMode().equals("LegitFast")) {
                    if (!isHit) {
                        isHit = true;
                        attackTimer.reset();
                        delay = (long) randomBetween(150, 200);
                    }
                }

            }
        }
    }
    public static double randomBetween(final double min, final double max) {
        return min + (secureRandom.nextDouble() * (max - min));
    }
    @EventTarget
    public void onPre(EventMotion event) {
        if (event.getType() == EventType.PRE) {
            if (mc.thePlayer.moveForward > 0 && mc.thePlayer.serverSprintState == mc.thePlayer.isSprinting()) {
                sprint = !mc.thePlayer.serverSprintState;
            }
        }

    }
}
