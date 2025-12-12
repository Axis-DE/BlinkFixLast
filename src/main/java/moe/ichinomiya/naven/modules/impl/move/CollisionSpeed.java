package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.MoveUtils;
import moe.ichinomiya.naven.utils.RotationUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "CollisionSpeed", description = "Speeds up entities", category = Category.MOVEMENT)
public class CollisionSpeed extends Module {

    BooleanValue noscaffold = ValueBuilder.create(this, "No Scaffold").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue nogapple = ValueBuilder.create(this, "No AutoGapple but MoveGapple has use CollisionSpeed").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue boostvalue = ValueBuilder.create(this, "Boost Amount").setDefaultFloatValue(0.04F).setFloatStep(0.01F).setMinFloatValue(0.01F).setMaxFloatValue(0.07F).build().getFloatValue();

    @EventTarget
    public void onMotion(EventMotion e) {
        if (nogapple.getCurrentValue()) {
            Module autoGappleModule = BlinkFix.getInstance().getModuleManager().getModule("AutoGapple");
            if (autoGappleModule != null && autoGappleModule.isEnabled()) {
                try {
                    java.lang.reflect.Field stuckField = autoGappleModule.getClass().getDeclaredField("stuck");
                    stuckField.setAccessible(true);
                    BooleanValue stuckValue = (BooleanValue) stuckField.get(autoGappleModule);
                    if (stuckValue != null && stuckValue.getCurrentValue()) {
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (noscaffold.getCurrentValue()) {
            Module scaffoldModule = BlinkFix.getInstance().getModuleManager().getModule("Scaffold");
            if (scaffoldModule != null && scaffoldModule.isEnabled()) {
                return;
            }
        }
        if (e.getType() == EventType.PRE && ViaLoadingBase.getInstance().getTargetVersion().getVersion() >= 47) {
            if (MoveUtils.isMoving()) {
                AxisAlignedBB player = mc.thePlayer.getEntityBoundingBox().expand(1, 1, 1);

                long entities = mc.theWorld.loadedEntityList.stream()
                        .filter(entity -> entity != mc.thePlayer)
                        .filter(entity -> entity.getEntityId() > 0).filter(entity -> !entity.isDead)
                        .filter(entity -> entity instanceof EntityLivingBase)
                        .filter(entity -> !(entity instanceof EntityArmorStand))
                        .filter(entity -> !(entity instanceof EntityOtherPlayerMP) || !((EntityOtherPlayerMP) entity).isFakePlayer())
                        .filter(entity -> entity.getEntityBoundingBox().intersectsWith(player)).count();

                float realYaw = mc.thePlayer.rotationYaw;

                if (mc.gameSettings.keyBindBack.pressed) {
                    realYaw += 180.0F;
                    if (mc.gameSettings.keyBindLeft.pressed) {
                        realYaw += 45.0F;
                    } else if (mc.gameSettings.keyBindRight.pressed) {
                        realYaw -= 45.0F;
                    }
                } else if (mc.gameSettings.keyBindForward.pressed) {
                    if (mc.gameSettings.keyBindLeft.pressed) {
                        realYaw -= 45.0F;
                    } else if (mc.gameSettings.keyBindRight.pressed) {
                        realYaw += 45.0F;
                    }
                } else if (mc.gameSettings.keyBindRight.pressed) {
                    realYaw += 90.0F;
                } else if (mc.gameSettings.keyBindLeft.pressed) {
                    realYaw -= 90.0F;
                }

                double yaw = Math.toRadians(realYaw);
                double boost = boostvalue.getCurrentValue() * entities;

                if (TargetStrafe.target != null && BlinkFix.getInstance().getModuleManager().getModule(TargetStrafe.class).isEnabled() && (!TargetStrafe.getJumpKeyOnly() || mc.gameSettings.keyBindJump.pressed)) {
                    float diff = (float) ((boost / (TargetStrafe.getRange() * Math.PI * 2)) * 360) * TargetStrafe.direction;
                    float[] rotation = RotationUtils.getNeededRotations(new Vec3(TargetStrafe.target.posX, TargetStrafe.target.posY, TargetStrafe.target.posZ), new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));

                    rotation[0] += diff;
                    float dir = rotation[0] * (float) (Math.PI / 180F);

                    double x = TargetStrafe.target.posX - Math.sin(dir) * TargetStrafe.getRange();
                    double z = TargetStrafe.target.posZ + Math.cos(dir) * TargetStrafe.getRange();

                    yaw = RotationUtils.getNeededRotations(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vec3(x, TargetStrafe.target.posY, z))[0] * (float) (Math.PI / 180F);
                }

                mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
            }
        }
    }
}