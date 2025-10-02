package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat.Aura;
import axis.shiyan.wei.bluearchive.blinkfix.utils.MoveUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
@ModuleInfo(name = "TargetStrafe", description = "Strafes towards the target", category = Category.MOVEMENT)
public class TargetStrafe extends Module {
    private final TimeHelper timer = new TimeHelper();
    private final BooleanValue jumpKeyOnly = ValueBuilder.create(this, "Jump Key Only").setDefaultBooleanValue(true).build().getBooleanValue();
    private final FloatValue range = ValueBuilder.create(this, "Range").setMinFloatValue(0.1f).setMaxFloatValue(2).setDefaultFloatValue(0.5f).setFloatStep(0.1f).build().getFloatValue();
    private final FloatValue switchDelay = ValueBuilder.create(this, "Switch Delay").setMinFloatValue(100).setMaxFloatValue(5000).setDefaultFloatValue(1000).setFloatStep(100).build().getFloatValue();
    public static int direction = 1;
    public static EntityLivingBase target;
    private final TimeHelper switchTimer = new TimeHelper();

    public static float getRange() {
        TargetStrafe targetStrafe = (TargetStrafe) BlinkFix.getInstance().getModuleManager().getModule(TargetStrafe.class);
        return targetStrafe.range.getCurrentValue();
    }

    public static boolean getJumpKeyOnly() {
        TargetStrafe targetStrafe = (TargetStrafe) BlinkFix.getInstance().getModuleManager().getModule(TargetStrafe.class);
        return targetStrafe.jumpKeyOnly.getCurrentValue();
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (Aura.target == null) {
                target = null;
            } else {
                if (switchTimer.delay(switchDelay.getCurrentValue()) || target == null) {
                    ArrayList<EntityLivingBase> targets = new ArrayList<>(Aura.targets);

                    targets.sort((o1, o2) -> {
                        float distance1 = mc.thePlayer.getDistanceToEntity(o1);
                        float distance2 = mc.thePlayer.getDistanceToEntity(o2);
                        return Float.compare(distance1, distance2);
                    });

                    if (!targets.isEmpty()) {
                        target = targets.get(0);
                        switchTimer.reset();
                    }
                }
            }


            AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox();

            boolean isInVoid = MoveUtils.isVecOverVoid(boundingBox.minX, boundingBox.minY, boundingBox.minZ) ||
                    MoveUtils.isVecOverVoid(boundingBox.minX, boundingBox.minY, boundingBox.maxZ) ||
                    MoveUtils.isVecOverVoid(boundingBox.maxX, boundingBox.minY, boundingBox.minZ) ||
                    MoveUtils.isVecOverVoid(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);

            if ((isInVoid || mc.thePlayer.isCollidedHorizontally) && timer.delay(500, true)) {
                direction = direction * -1;
            }
        }
    }
}
