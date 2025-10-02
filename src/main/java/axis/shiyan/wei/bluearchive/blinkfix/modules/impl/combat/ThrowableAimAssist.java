package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.Teams;
import axis.shiyan.wei.bluearchive.blinkfix.utils.Colors;
import axis.shiyan.wei.bluearchive.blinkfix.utils.Render3DUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RotationUtils;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.util.vector.Vector2f;

import java.util.Comparator;
import java.util.Optional;

@ModuleInfo(name = "AimAssist", description = "Aim assist for bow, throwable and fish rod.", category = Category.COMBAT)
public class ThrowableAimAssist extends Module {
    private final BooleanValue bow = ValueBuilder.create(this, "Bow").setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue throwable = ValueBuilder.create(this, "Throwable").setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue fishRod = ValueBuilder.create(this, "Fish Rod").setDefaultBooleanValue(true).build().getBooleanValue();

    private final FloatValue range = ValueBuilder.create(this, "Range").setDefaultFloatValue(50).setFloatStep(1).setMinFloatValue(10).setMaxFloatValue(100).build().getFloatValue();
    private final FloatValue fov = ValueBuilder.create(this, "FoV").setDefaultFloatValue(60).setFloatStep(1).setMinFloatValue(0).setMaxFloatValue(180).build().getFloatValue();
    private final FloatValue predict = ValueBuilder.create(this, "Predict").setDefaultFloatValue(0.8f).setFloatStep(0.01f).setMinFloatValue(0).setMaxFloatValue(2).build().getFloatValue();

    private EntityLivingBase target;
    public Vector2f rotation;

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            target = null;
            rotation = null;

            ItemStack currentItem = mc.thePlayer.inventory.getCurrentItem();

            if (currentItem != null) {
                boolean isHoldingThrowable = currentItem.getItem() == Items.snowball || currentItem.getItem() == Items.egg;
                boolean isHoldingBow = currentItem.getItem() == Items.bow;
                boolean isHoldingFishRod = currentItem.getItem() == Items.fishing_rod;
                if ((isHoldingThrowable && throwable.getCurrentValue()) ||
                        (isHoldingBow && bow.getCurrentValue()) ||
                        (isHoldingFishRod && fishRod.getCurrentValue())) {
                    target = getTarget();
                    if (target != null) {
                        rotation = getPlayerRotations(target);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRender(EventRender e) {
        if (target != null) {
            Entity entity = target;
            double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) e.renderPartialTicks - mc.getRenderManager().renderPosX;
            double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) e.renderPartialTicks - mc.getRenderManager().renderPosY;
            double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) e.renderPartialTicks - mc.getRenderManager().renderPosZ;

            Render3DUtils.drawSoiledEntityESP(posX, posY, posZ, target.width / 2, target.height, Colors.RED.c);
        }
    }

    private Vector2f getPlayerRotations(Entity target) {
        double distanceToEnt = mc.thePlayer.getDistanceToEntity(target);
        double predictX = target.posX + (target.posX - target.lastTickPosX) * (distanceToEnt * predict.getCurrentValue());
        double predictZ = target.posZ + (target.posZ - target.lastTickPosZ) * (distanceToEnt * predict.getCurrentValue());

        double x = predictX - mc.thePlayer.posX;
        double z = predictZ - mc.thePlayer.posZ;

        float originYaw = (float) (Math.atan2(target.posZ - mc.thePlayer.posZ, target.posX - mc.thePlayer.posX) * 180.0D / Math.PI) - 90.0F;
        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;

        float pitchDifference = 0.0F;
        float motionFactor = 1.5F;
        float motionSlowdown = 0.99F;
        if (mc.thePlayer.getCurrentEquippedItem() != null) {
            Item heldItem = mc.thePlayer.getCurrentEquippedItem().getItem();

            boolean isBow = false;
            float gravity = 0.05F;

            if (heldItem instanceof ItemBow) {
                isBow = true;
                float power = (float) mc.thePlayer.getItemInUseDuration() / 20.0F;
                power = (power * power + power * 2.0F) / 3.0F;

                if (power < 0.1D) {
                    return null;
                }

                if (power > 1.0F) {
                    power = 1.0F;
                }

                motionFactor = power * 3.0F;
            } else if (heldItem instanceof ItemFishingRod) {
                gravity = 0.04F;
                motionSlowdown = 0.92F;
            } else if (heldItem instanceof ItemSnowball || heldItem instanceof ItemEgg) {
                gravity = 0.03F;
            } else {
                return null;
            }

            for (float pitch = 90; pitch > -90; pitch --) {
                double posX = mc.thePlayer.posX - (double) (MathHelper.cos(originYaw / 180.0F * 3.1415927F) * 0.16F);
                double posY = mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight() - 0.10000000149011612D;
                double posZ = mc.thePlayer.posZ - (double) (MathHelper.sin(originYaw / 180.0F * 3.1415927F) * 0.16F);
                double motionX = (double) (-MathHelper.sin(originYaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
                double motionY = (double) (-MathHelper.sin((pitch + pitchDifference) / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
                double motionZ = (double) (MathHelper.cos(originYaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F)) * (isBow ? 1.0D : 0.4D);
                float distance = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                motionX /= distance;
                motionY /= distance;
                motionZ /= distance;
                motionX *= motionFactor;
                motionY *= motionFactor;
                motionZ *= motionFactor;
                boolean hasLanded = false;

                while (!hasLanded && posY > -150.0D) {
                    Vec3 posBefore = new Vec3(posX, posY, posZ);
                    Vec3 posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                    MovingObjectPosition landingPosition = mc.theWorld.rayTraceBlocks(posBefore, posAfter);
                    posBefore = new Vec3(posX, posY, posZ);
                    posAfter = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

                    if (landingPosition != null) {
                        hasLanded = true;
                        posAfter = new Vec3(landingPosition.hitVec.xCoord, landingPosition.hitVec.yCoord, landingPosition.hitVec.zCoord);
                    }

                    if (target.canBeCollidedWith() && target != mc.thePlayer) {
                        AxisAlignedBB targetBoundingBox = target.getEntityBoundingBox();
                        targetBoundingBox = new AxisAlignedBB(
                                targetBoundingBox.minX,
                                targetBoundingBox.minY + target.height / 2f - 0.3f,
                                targetBoundingBox.minZ,
                                targetBoundingBox.maxX,
                                targetBoundingBox.maxY - target.height / 2f + 0.3f,
                                targetBoundingBox.maxZ);
                        MovingObjectPosition possibleEntityLanding = targetBoundingBox.calculateIntercept(posBefore, posAfter);
                        if (possibleEntityLanding != null) {
                            return new Vector2f(yaw, pitch);
                        }
                    }

                    posX += motionX;
                    posY += motionY;
                    posZ += motionZ;

                    BlockPos landedPosition = new BlockPos(posX, posY, posZ);
                    Block block = mc.theWorld.getBlockState(landedPosition).getBlock();

                    if (block.getMaterial() == Material.water) {
                        motionX *= 0.6D;
                        motionY *= 0.6D;
                        motionZ *= 0.6D;
                    } else {
                        motionX *= motionSlowdown;
                        motionY *= motionSlowdown;
                        motionZ *= motionSlowdown;
                    }

                    motionY -= gravity;
                }
            }
        }

        return null;
    }

    private EntityLivingBase getTarget() {
        Optional<EntityPlayer> target = mc.theWorld.playerEntities.stream()
                .filter(e -> !Teams.isSameTeam(e))
                .filter(e -> !AntiBot.isBot(e))
                .filter(e -> !e.isFakePlayer())
                .filter(e -> mc.thePlayer.getHorizonDistanceToEntity(e) <= range.getCurrentValue())
                .filter(mc.thePlayer::canEntityBeSeen)
                .filter(e -> !e.isInvisibleToPlayer(mc.thePlayer))
                .filter(e -> e != mc.thePlayer)
                .filter(e -> RotationUtils.inFoV(e, fov.getCurrentValue()))
                .min(Comparator.comparingDouble(RotationUtils::getYawToEntity));

        return target.orElse(null);
    }
}
