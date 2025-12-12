package moe.ichinomiya.naven.utils;

import lombok.Getter;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.util.*;

import static moe.ichinomiya.naven.utils.Utils.mc;


@Getter
public class FallingPlayer {
    public static boolean isBlockUnder(final double height) {
        for (int offset = 0; offset < height; offset += 2) {
            final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);

            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEating() {
        return mc.thePlayer.isEating() &&
                (mc.thePlayer.getCurrentEquippedItem() != null &&
                        (mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemFood ||
                                mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemPotion));
    }

    public static boolean isBlockUnder(final double height, final EntityLivingBase entity) {
        for (int offset = 0; offset < height; offset += 2) {
            final AxisAlignedBB bb = entity.getEntityBoundingBox().offset(0, -offset, 0);

            if (!mc.theWorld.getCollidingBoundingBoxes(entity, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlockUnder(BlockPos blockPos) {
        for (int i = (int) (blockPos.getY() - 1.0); i > 0; --i) {
            BlockPos pos = new BlockPos(blockPos.getX(), i, blockPos.getZ());
            if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)
                continue;
            return true;
        }
        return false;
    }
    private double x;
    private double y;
    private double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private float yaw;
    private float strafe;
    private float forward;
    private float jumpMovementFactor;

    public FallingPlayer(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float strafe, float forward, float jumpMovementFactor) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.strafe = strafe;
        this.forward = forward;
        this.jumpMovementFactor = jumpMovementFactor;
    }

    public FallingPlayer(EntityPlayer player) {
        this(player.posX, player.posY, player.posZ, player.motionX, player.motionY, player.motionZ, player.rotationYaw, player.moveStrafing, player.moveForward, player.jumpMovementFactor);
    }

    private void calculateForTick() {
        float sr = strafe * 0.9800000190734863f;
        float fw = forward * 0.9800000190734863f;
        float v = sr * sr + fw * fw;
        if (v >= 0.0001f) {
            v = MathHelper.sqrt_float(v);
            if (v < 1.0f) {
                v = 1.0f;
            }
            float fixedJumpFactor = jumpMovementFactor;
            if (mc.thePlayer.isSprinting()) {
                fixedJumpFactor = fixedJumpFactor * 1.3f;
            }
            v = fixedJumpFactor / v;
            sr *= v;
            fw *= v;
            float f1 = MathHelper.sin(yaw * (float) Math.PI / 180.0f);
            float f2 = MathHelper.cos(yaw * (float) Math.PI / 180.0f);
            motionX += sr * f2 - fw * f1;
            motionZ += fw * f2 + sr * f1;
        }
        motionY -= 0.08;
        motionY *= 0.9800000190734863;
        x += motionX;
        y += motionY;
        z += motionZ;
        motionX *= 0.91;
        motionZ *= 0.91;
    }

    public void calculate(int ticks) {
        for (int i = 0; i < ticks; i++) {
            calculateForTick();
        }
    }

    public BlockPos findCollision(int ticks) {
        for (int i = 0; i < ticks; i++) {
            Vec3 start = new Vec3(x, y, z);
            calculateForTick();
            Vec3 end = new Vec3(x, y, z);
            BlockPos raytracedBlock;
            float w = mc.thePlayer.width / 2f;
            if ((raytracedBlock = rayTrace(start, end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(w, 0.0, w), end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(-w, 0.0, w), end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(w, 0.0, -w), end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(-w, 0.0, -w), end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(w, 0.0, w / 2f), end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(-w, 0.0, w / 2f), end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(w / 2f, 0.0, w), end)) != null) return raytracedBlock;
            if ((raytracedBlock = rayTrace(start.addVector(w / 2f, 0.0, -w), end)) != null) return raytracedBlock;
        }
        return null;
    }

    private BlockPos rayTrace(Vec3 start, Vec3 end) {
        MovingObjectPosition result = mc.theWorld.rayTraceBlocks(start, end, true);
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && result.sideHit == EnumFacing.UP) {
            return result.getBlockPos();
        } else {
            return null;
        }
    }

}