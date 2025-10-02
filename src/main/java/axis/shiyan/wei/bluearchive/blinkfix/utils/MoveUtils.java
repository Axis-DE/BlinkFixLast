package axis.shiyan.wei.bluearchive.blinkfix.utils;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMove;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMoveInput;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.TargetStrafe;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MoveUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static double getJumpBoostModifier(double baseJumpHeight) {
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            baseJumpHeight += (float) (amplifier + 1) * 0.1F;
        }

        return baseJumpHeight;
    }

    public static double defaultSpeed() {
        return defaultSpeed(mc.thePlayer);
    }

    public static double defaultSpeed(EntityLivingBase entity) {
        return defaultSpeed(entity, 0.2);
    }

    public static double defaultSpeed(EntityLivingBase entity, double effectBoost) {
        double baseSpeed = 0.2873D;
        if (entity.isPotionActive(Potion.moveSpeed)) {
            int amplifier = entity.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + effectBoost * (amplifier + 1));
        }
        return baseSpeed;
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static void strafe(EventMove e) {
        strafe(e, getSpeed());
    }

    public static void strafe(final double d) {
        if (!isMoving()) return;

        final double yaw = getDirection();
        mc.thePlayer.motionX = -Math.sin(yaw) * d;
        mc.thePlayer.motionZ = Math.cos(yaw) * d;
    }

    public static void strafe(EventMove e, final double d) {
        if (!isMoving()) return;

        final double yaw = getDirection();
        e.setX(mc.thePlayer.motionX = -Math.sin(yaw) * d);
        e.setZ(mc.thePlayer.motionZ = Math.cos(yaw) * d);
    }

    public static void doStrafe(double speed) {
        if (!isMoving()) return;

        final double yaw = getYaw(true);
        mc.thePlayer.motionX = -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ = Math.cos(yaw) * speed;
    }

    private static float getDirection(float forward, float strafe) {
        float direction = mc.thePlayer.rotationYaw;
        boolean isMovingForward = forward > 0.0F;
        boolean isMovingBack = forward < 0.0F;
        boolean isMovingRight = strafe > 0.0F;
        boolean isMovingLeft = strafe < 0.0F;
        boolean isMovingSideways = isMovingRight || isMovingLeft;
        boolean isMovingStraight = isMovingForward || isMovingBack;

        if (forward != 0.0F || strafe != 0.0F) {
            if (isMovingBack && !isMovingSideways) {
                return direction + 180.0F;
            } else if (isMovingForward && isMovingLeft) {
                return direction + 45.0F;
            } else if (isMovingForward && isMovingRight) {
                return direction - 45.0F;
            } else if (!isMovingStraight && isMovingLeft) {
                return direction + 90.0F;
            } else if (!isMovingStraight && isMovingRight) {
                return direction - 90.0F;
            } else if (isMovingBack && isMovingLeft) {
                return direction + 135.0F;
            } else if (isMovingBack) {
                return direction - 135.0F;
            }
        }

        return direction;
    }

    public static double getDirection() {
        float rotationYaw = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;
        if (mc.thePlayer.moveForward < 0F) forward = -0.5F;
        else if (mc.thePlayer.moveForward > 0F) forward = 0.5F;

        if (mc.thePlayer.moveStrafing > 0F) rotationYaw -= 90F * forward;

        if (mc.thePlayer.moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static float getMovementDirection(final float forward, final float strafing, float yaw) {
        if (forward == 0.0F && strafing == 0.0F) return yaw;

        boolean reversed = forward < 0.0f;
        float strafingYaw = 90.0f * (forward > 0.0f ? 0.5f : reversed ? -0.5f : 1.0f);

        if (reversed) yaw += 180.0f;
        if (strafing > 0.0f) yaw -= strafingYaw;
        else if (strafing < 0.0f) yaw += strafingYaw;

        return yaw;
    }

    public static boolean isBoundingBoxOverVoid(AxisAlignedBB bb) {
        return isVecOverVoid(bb.minX, bb.minY, bb.minZ) && isVecOverVoid(bb.minX, bb.minY, bb.maxZ) && isVecOverVoid(bb.maxX, bb.minY, bb.minZ) && isVecOverVoid(bb.maxX, bb.minY, bb.maxZ);
    }

    public static boolean isVecOverVoid(double posX, double posY, double posZ) {
        while (posY > 0) {
            Vec3 posBefore = new Vec3(posX, posY, posZ);
            Vec3 posAfter = new Vec3(posX, posY - 1, posZ);

            MovingObjectPosition position = mc.theWorld.rayTraceBlocks(posBefore, posAfter);
            if (position != null) {
                return false;
            }

            posY -= 1;
        }

        return true;
    }

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }


    public static void fixMovement(final EventMoveInput event, final float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();

        float rotationYaw = mc.thePlayer.rotationYaw;

        if (TargetStrafe.target != null && BlinkFix.getInstance().getModuleManager().getModule(TargetStrafe.class).isEnabled() && (!TargetStrafe.getJumpKeyOnly() || mc.gameSettings.keyBindJump.pressed)) {
            float diff = (float) ((getBaseMoveSpeed() / (TargetStrafe.getRange() * Math.PI * 2)) * 360) * TargetStrafe.direction;
            float[] rotation = RotationUtils.getNeededRotations(new Vec3(TargetStrafe.target.posX, TargetStrafe.target.posY, TargetStrafe.target.posZ), new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));

            rotation[0] += diff;
            float dir = rotation[0] * (float) (Math.PI / 180F);

            double x = TargetStrafe.target.posX - Math.sin(dir) * TargetStrafe.getRange();
            double z = TargetStrafe.target.posZ + Math.cos(dir) * TargetStrafe.getRange();

            rotationYaw = (float) Math.toDegrees(RotationUtils.getNeededRotations(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), new Vec3(x, TargetStrafe.target.posY, z))[0] * (float) (Math.PI / 180F));
        }

        final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(rotationYaw, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        int closestForward = 0, closestStrafe = 0;
        float closestDifference = Float.MAX_VALUE;

        for (int predictedForward = -1; predictedForward <= 1; predictedForward += 1) {
            for (int predictedStrafe = -1; predictedStrafe <= 1; predictedStrafe += 1) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
    }

    public static double direction() {
        float rotationYaw = mc.thePlayer.movementYaw;

        if (mc.thePlayer.moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.thePlayer.moveForward < 0) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (mc.thePlayer.moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static void stop() {
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionZ = 0;
    }

    public static double[] getMotion(final double speed, final float strafe, final float forward, final float yaw) {
        final float friction = (float)speed;
        final float f1 = MathHelper.sin(yaw * 3.1415927f / 180.0f);
        final float f2 = MathHelper.cos(yaw * 3.1415927f / 180.0f);
        final double motionX = strafe * friction * f2 - forward * friction * f1;
        final double motionZ = forward * friction * f2 + strafe * friction * f1;
        return new double[] { motionX, motionZ };
    }

    public final void doStrafe() {
        doStrafe(getSpeed());
    }

    public static boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }

    public static float getSpeed() {
        return (float) Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static double getSpeed(Entity entity) {
        double motionX = entity.lastTickPosX - entity.posX;
        double motionZ = entity.lastTickPosZ - entity.posZ;
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2875D;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            baseSpeed *= 1.0D + 0.2D * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        return baseSpeed;
    }

    public static double getBaseMoveSpeed(EntityPlayer entity) {
        double baseSpeed = 0.2875D;
        if (entity.isPotionActive(Potion.moveSpeed))
            baseSpeed *= 1.0D + 0.2D * (entity.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        return baseSpeed;
    }

    public static final List<Double> frictionValues = new ArrayList<>();

    public static double calculateFriction(final double moveSpeed, final double lastDist, final double baseMoveSpeedRef) {
        frictionValues.clear();
        frictionValues.add(lastDist - lastDist / 159.9999985);
        frictionValues.add(lastDist - (moveSpeed - lastDist) / 33.3);
        final double materialFriction = mc.thePlayer.isInWater() ? 0.8899999856948853 : (mc.thePlayer.isInLava() ? 0.5350000262260437 : 0.9800000190734863);
        frictionValues.add(lastDist - baseMoveSpeedRef * (1.0 - materialFriction));
        return Collections.min((Collection<? extends Double>) frictionValues);
    }

    public static final double getYaw(boolean strafing) {
        float rotationYaw = mc.thePlayer.rotationYawHead;
        float forward = 1F;

        final double moveForward = mc.thePlayer.movementInput.moveForward;
        final double moveStrafing = mc.thePlayer.movementInput.moveStrafe;
        final float yaw = mc.thePlayer.rotationYaw;

        if (moveForward < 0) {
            rotationYaw += 180F;
        }

        if (moveForward < 0) {
            forward = -0.5F;
        } else if (moveForward > 0) {
            forward = 0.5F;
        }

        if (moveStrafing > 0) {
            rotationYaw -= 90F * forward;
        } else if (moveStrafing < 0) {
            rotationYaw += 90F * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static boolean isBlockNearBy(double distance) {
        double smallX = Math.min(mc.thePlayer.posX - distance, mc.thePlayer.posX + distance);
        double smallY = Math.min(mc.thePlayer.posY, mc.thePlayer.posY);
        double smallZ = Math.min(mc.thePlayer.posZ - distance, mc.thePlayer.posZ + distance);
        double bigX = Math.max(mc.thePlayer.posX - distance, mc.thePlayer.posX + distance);
        double bigY = Math.max(mc.thePlayer.posY, mc.thePlayer.posY);
        double bigZ = Math.max(mc.thePlayer.posZ - distance, mc.thePlayer.posZ + distance);
        int x = (int) smallX;
        while ((double) x <= bigX) {
            int y = (int) smallY;
            while ((double) y <= bigY) {
                int z = (int) smallZ;
                while ((double) z <= bigZ) {
                    if (!checkPositionValidity(new Vec3(x, y, z)) && checkPositionValidity(new Vec3(x, y + 1, z))) {
                        return true;
                    }
                    ++z;
                }
                ++y;
            }
            ++x;
        }
        return false;
    }

    public static boolean checkPositionValidity(Vec3 vec3) {
        BlockPos pos = new BlockPos(vec3);
        if (isBlockSolid(pos) || isBlockSolid(pos.add(0, 1, 0))) {
            return false;
        }
        return isSafeToWalkOn(pos.add(0, -1, 0));
    }

    private static boolean isBlockSolid(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return block instanceof BlockSlab || block instanceof BlockStairs || block instanceof BlockCactus || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSkull || block instanceof BlockPane || block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockGlass || block instanceof BlockPistonBase || block instanceof BlockPistonExtension || block instanceof BlockPistonMoving || block instanceof BlockStainedGlass || block instanceof BlockTrapDoor;
    }

    private static boolean isSafeToWalkOn(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return !(block instanceof BlockFence) && !(block instanceof BlockWall);
    }


    public static void setMotion(double speed) {
        setMotion(speed, mc.thePlayer.rotationYaw);
    }

    public static void setMotion(EventMove e, double speed, float yaw) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        if ((forward == 0.0D) && (strafe == 0.0D)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            e.setX(0);
            e.setZ(0);
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }
            double cos = Math.cos(Math.toRadians(yaw + 90.0F));
            double sin = Math.sin(Math.toRadians(yaw + 90.0F));

            mc.thePlayer.motionX = forward * speed * cos + strafe * speed * sin;
            mc.thePlayer.motionZ = forward * speed * sin - strafe * speed * cos;
            e.setX(mc.thePlayer.motionX);
            e.setZ(mc.thePlayer.motionZ);
        }
    }

    public static void setMotion(EventMove e, double speed) {
        setMotion(e, speed, mc.thePlayer.rotationYaw);
    }

    public static void setMotion(double speed, float yaw) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        if ((forward == 0.0D) && (strafe == 0.0D)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }
            mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F));
            mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        }
    }

    public static boolean checkTeleport(double x, double y, double z, double distBetweenPackets) {
        double distx = mc.thePlayer.posX - x;
        double disty = mc.thePlayer.posY - y;
        double distz = mc.thePlayer.posZ - z;
        double dist = Math.sqrt(mc.thePlayer.getDistanceSq(x, y, z));
        double distanceEntreLesPackets = distBetweenPackets;
        double nbPackets = Math.round(dist / distanceEntreLesPackets + 0.49999999999) - 1;

        double xtp = mc.thePlayer.posX;
        double ytp = mc.thePlayer.posY;
        double ztp = mc.thePlayer.posZ;
        for (int i = 1; i < nbPackets; i++) {
            double xdi = (x - mc.thePlayer.posX) / (nbPackets);
            xtp += xdi;

            double zdi = (z - mc.thePlayer.posZ) / (nbPackets);
            ztp += zdi;

            double ydi = (y - mc.thePlayer.posY) / (nbPackets);
            ytp += ydi;
            AxisAlignedBB bb = new AxisAlignedBB(xtp - 0.3, ytp, ztp - 0.3, xtp + 0.3, ytp + 1.8, ztp + 0.3);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return false;
            }

        }
        return true;
    }


    public static boolean isOnGround(double height) {
        if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isOnGround(Entity entity, double height) {
        if (!mc.theWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static int getJumpEffect() {
        if (mc.thePlayer.isPotionActive(Potion.jump))
            return mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1;
        else return 0;
    }

    public static int getSpeedEffect() {
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            return mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
        else return 0;
    }

    public static int getSpeedEffect(EntityPlayer player) {
        if (player.isPotionActive(Potion.moveSpeed))
            return player.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
        else return 0;
    }

    public static Block getBlockUnderPlayer(EntityPlayer inPlayer, double height) {
        return mc.theWorld.getBlockState(new BlockPos(inPlayer.posX, inPlayer.posY - height, inPlayer.posZ)).getBlock();
    }

    public static Block getBlockAtPosC(double x, double y, double z) {
        EntityPlayer inPlayer = mc.thePlayer;
        return mc.theWorld.getBlockState(new BlockPos(inPlayer.posX + x, inPlayer.posY + y, inPlayer.posZ + z)).getBlock();
    }

    public static Block getBlockatPosSpeed(final EntityPlayer inPlayer, final float x, final float z) {
        final double posX = inPlayer.posX + inPlayer.motionX * x;
        final double posZ = inPlayer.posZ + inPlayer.motionZ * z;
        return BlockUtils.getBlock(new BlockPos(posX, inPlayer.posY, posZ));
    }


    public static float getDistanceToGround(Entity e) {
        if (mc.thePlayer.isCollidedVertically && mc.thePlayer.onGround) {
            return 0.0F;
        }
        for (float a = (float) e.posY; a > 0.0F; a -= 1.0F) {
            int[] stairs = {53, 67, 108, 109, 114, 128, 134, 135, 136, 156, 163, 164, 180};
            int[] exemptIds = {6, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 63, 65, 66, 68, 69, 70, 72, 75, 76, 77, 83, 92, 93, 94, 104, 105, 106, 115, 119, 131, 132, 143, 147, 148, 149, 150, 157, 171, 175, 176, 177};
            Block block = mc.theWorld.getBlockState(new BlockPos(e.posX, a - 1.0F, e.posZ)).getBlock();
            if (!(block instanceof BlockAir)) {
                if ((Block.getIdFromBlock(block) == 44) || (Block.getIdFromBlock(block) == 126)) {
                    return (float) (e.posY - a - 0.5D) < 0.0F ? 0.0F : (float) (e.posY - a - 0.5D);
                }
                int[] arrayOfInt1;
                int j = (arrayOfInt1 = stairs).length;
                for (int i = 0; i < j; i++) {
                    int id = arrayOfInt1[i];
                    if (Block.getIdFromBlock(block) == id) {
                        return (float) (e.posY - a - 1.0D) < 0.0F ? 0.0F : (float) (e.posY - a - 1.0D);
                    }
                }
                j = (arrayOfInt1 = exemptIds).length;
                for (int i = 0; i < j; i++) {
                    int id = arrayOfInt1[i];
                    if (Block.getIdFromBlock(block) == id) {
                        return (float) (e.posY - a) < 0.0F ? 0.0F : (float) (e.posY - a);
                    }
                }
                return (float) (e.posY - a + block.getBlockBoundsMaxY() - 1.0D);
            }
        }
        return 0.0F;
    }


    public static float[] getRotationsBlock(BlockPos block, EnumFacing face) {
        double x = block.getX() + 0.5 - mc.thePlayer.posX + (double) face.getFrontOffsetX() / 2;
        double z = block.getZ() + 0.5 - mc.thePlayer.posZ + (double) face.getFrontOffsetZ() / 2;
        double y = (block.getY() + 0.5);
        double d1 = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - y;
        double d3 = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (Math.atan2(d1, d3) * 180.0D / Math.PI);
        if (yaw < 0.0F) {
            yaw += 360f;
        }
        return new float[]{yaw, pitch};
    }

    public static boolean isBlockAboveHead() {
        AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ + 0.3, mc.thePlayer.posX + 0.3, mc.thePlayer.posY + 2.5, mc.thePlayer.posZ - 0.3);
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty();
    }

    public static boolean isCollidedH(double dist) {
        AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3, mc.thePlayer.posY + 2, mc.thePlayer.posZ + 0.3, mc.thePlayer.posX + 0.3, mc.thePlayer.posY + 3, mc.thePlayer.posZ - 0.3);
        if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.3 + dist, 0, 0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(-0.3 - dist, 0, 0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0, 0, 0.3 + dist)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0, 0, -0.3 - dist)).isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isRealCollidedH(double dist) {
        AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX - 0.3, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ + 0.3, mc.thePlayer.posX + 0.3, mc.thePlayer.posY + 1.9, mc.thePlayer.posZ - 0.3);
        if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0.3 + dist, 0, 0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(-0.3 - dist, 0, 0)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0, 0, 0.3 + dist)).isEmpty()) {
            return true;
        } else if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb.offset(0, 0, -0.3 - dist)).isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isOnLiquid() {
        if (mc.thePlayer == null) {
            return false;
        }
        boolean onLiquid = false;
        final int y = (int) mc.thePlayer.boundingBox.offset(0.0, -0.0, 0.0).minY;
        for (int x = MathHelper.floor_double(mc.thePlayer.boundingBox.minX); x < MathHelper.floor_double(mc.thePlayer.boundingBox.maxX) + 1; ++x) {
            for (int z = MathHelper.floor_double(mc.thePlayer.boundingBox.minZ); z < MathHelper.floor_double(mc.thePlayer.boundingBox.maxZ) + 1; ++z) {
                final Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }
}
