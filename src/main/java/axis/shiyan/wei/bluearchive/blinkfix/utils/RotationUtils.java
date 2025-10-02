package axis.shiyan.wei.bluearchive.blinkfix.utils;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.model.ComparableProtocolVersion;
import lombok.Getter;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.Disabler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import org.lwjgl.util.vector.Vector2f;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RotationUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static Rotation serverRotation = new Rotation(0, 0);

    public static float[] getFixedRotation(float[] rotation, float[] lastRotation) {
        return getFixedRotation(rotation[0], rotation[1], lastRotation[0], lastRotation[1]);
    }

    public static float[] getFixedRotation(float yaw, float pitch, float lastYaw, float lastPitch) {
        final Minecraft mc = Minecraft.getMinecraft();

        final float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        final float gcd = f * f * f * 1.2F;

        final float deltaYaw = yaw - lastYaw;
        final float deltaPitch = pitch - lastPitch;

        final float fixedDeltaYaw = deltaYaw - (deltaYaw % gcd);
        final float fixedDeltaPitch = deltaPitch - (deltaPitch % gcd);

        final float fixedYaw = lastYaw + fixedDeltaYaw;
        final float fixedPitch = lastPitch + fixedDeltaPitch;

        return new float[]{fixedYaw, fixedPitch};
    }

    public static Vec3 getLocation(AxisAlignedBB bb, float yawDiff, float pitchDiff) {
        double yaw = 0.5 + (yawDiff / 2);
        double pitch = 0.5 + (pitchDiff / 2);
        VecRotation rotation = RotationUtils.searchCenter(bb, true);
        return rotation != null ? rotation.getVec() : new Vec3(bb.minX + (bb.maxX - bb.minX) * yaw, bb.minY + (bb.maxY - bb.minY) * pitch, bb.minZ + (bb.maxZ - bb.minZ) * yaw);
    }

    public static float getTrajAngleSolutionLow(float d3, float d1, float velocity, float gravity) {
        float sqrt = velocity * velocity * velocity * velocity - gravity * (gravity * (d3 * d3) + 2.0F * d1 * (velocity * velocity));
        return (float) Math.toDegrees(Math.atan((velocity * velocity - Math.sqrt(sqrt)) / (gravity * d3)));
    }

    public static float[] getRotationFromPosition(final double x, final double z, final double y) {
        final double xDiff = x - Minecraft.getMinecraft().thePlayer.posX;
        final double zDiff = z - Minecraft.getMinecraft().thePlayer.posZ;
        final double yDiff = y - Minecraft.getMinecraft().thePlayer.posY - 1.2;
        final double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        final float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0f;
        final float pitch = (float) (-(Math.atan2(yDiff, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static VecRotation searchCenter(AxisAlignedBB bb, boolean predict) {
        VecRotation vecRotation = null;
        for (double xSearch = 0.15D; xSearch < 0.85D; xSearch += 0.1D) {
            for (double ySearch = 0.15D; ySearch < 1D; ySearch += 0.1D) {
                for (double zSearch = 0.15D; zSearch < 0.85D; zSearch += 0.1D) {
                    Vec3 vec3 = new Vec3(bb.minX + (bb.maxX - bb.minX) * xSearch, bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch);
                    Rotation rotation = toRotation(vec3, predict);
                    VecRotation currentVec = new VecRotation(vec3, rotation);
                    if (vecRotation == null || (getRotationDifference(currentVec.getRotation()) < getRotationDifference(vecRotation.getRotation())))
                        vecRotation = currentVec;
                }
            }
        }

        return vecRotation;
    }

    public static Rotation toRotation(Vec3 vec, boolean predict) {
        Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (predict)
            eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        double diffX = vec.xCoord - eyesPos.xCoord;
        double diffY = vec.yCoord - eyesPos.yCoord;
        double diffZ = vec.zCoord - eyesPos.zCoord;

        float[] rotation = diffCalc(diffX, diffY, diffZ);
        return new Rotation(rotation[0], rotation[1]);
    }

    public static double getRotationDifference(Rotation rotation) {
        return getRotationDifference(rotation, serverRotation);
    }

    public static double getRotationDifference(Rotation a, Rotation b) {
        return Math.hypot(getAngleDifference(a.getYaw(), b.getYaw()), (a.getPitch() - b.getPitch()));
    }

    public static float getAngleDifference(float a, float b) {
        return ((a - b) % 360.0F + 540.0F) % 360.0F - 180.0F;
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle3 = Math.abs((angle1 - angle2)) % 360.0f;
        if (angle3 > 180.0f) {
            angle3 = 0.0f;
        }
        return angle3;
    }

    public static float[] mouseSens(float yaw, float pitch, final float lastYaw, final float lastPitch) {
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        if (yaw == lastYaw && pitch == lastPitch) {
            return new float[]{yaw, pitch};
        }
        final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f2 = f1 * f1 * f1 * 8.0f;
        final int deltaX = (int) ((6.667 * yaw - 6.667 * lastYaw) / f2);
        final int deltaY = (int) ((6.667 * pitch - 6.667 * lastPitch) / f2) * -1;
        final float f3 = deltaX * f2;
        final float f4 = deltaY * f2;
        yaw = (float) (lastYaw + f3 * 0.15);
        final float f5 = (float) (lastPitch - f4 * 0.15);
        pitch = MathHelper.clamp_float(f5, -90.0f, 90.0f);
        return new float[]{yaw, pitch};
    }

    public static float getDistanceBetweenAngles2(float angle1, float angle2) {
        return (angle1 - angle2) % 360.0f;
    }

    public static float[] getRotations(Entity entity) {
        if (entity == null) {
            return null;
        }

        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;

        double diffY;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase elb = (EntityLivingBase) entity;
            diffY = elb.posY + 0.1 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            diffY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }

        return diffCalc(diffX, diffY, diffZ);
    }

    public static float[] getRotations(Vec3 vec) {
        Vec3 playerVector = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        double x = vec.xCoord - playerVector.xCoord;
        double y = vec.yCoord - playerVector.yCoord;
        double z = vec.zCoord - playerVector.zCoord;
        return diffCalc(x, y, z);
    }

    public static Vector2f getRotationsVector(Vec3 vec) {
        Vec3 playerVector = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        double x = vec.xCoord - playerVector.xCoord;
        double y = vec.yCoord - playerVector.yCoord;
        double z = vec.zCoord - playerVector.zCoord;
        return diffCalcVector(x, y, z);
    }

    public static float[] getRotations(BlockPos pos) {
        Vec3 playerVector = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        double x = pos.getX() - playerVector.xCoord + 0.5;

        double y = pos.getY() - playerVector.yCoord + 0.5;
        double z = pos.getZ() - playerVector.zCoord + 0.5;
        return diffCalc(x, y, z);
    }

    public static float[] getNeededRotations(Vec3 current, Vec3 target) {
        final double diffX = target.xCoord - current.xCoord;
        final double diffY = target.yCoord - current.yCoord;
        final double diffZ = target.zCoord - current.zCoord;
        return diffCalc(diffX, diffY, diffZ);
    }

    public static float getYawToEntity(final Entity e) {
        return ((Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f > 180.0f) ? (360.0f - Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f) : (Math.abs(RotationUtils.getRotations(e)[0] - mc.thePlayer.rotationYaw) % 360.0f));
    }

    public static float[] diffCalc(double diffX, double diffY, double diffZ) {
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch)};
    }

    public static Vector2f diffCalcVector(double diffX, double diffY, double diffZ) {
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new Vector2f(MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch));
    }

    public static boolean inFoV(Entity entity, float fov) {
        return ((Math.abs(RotationUtils.getRotations(entity)[0] - mc.thePlayer.rotationYaw) % 360.0f > 180.0f) ? (360.0f - Math.abs(RotationUtils.getRotations(entity)[0] - mc.thePlayer.rotationYaw) % 360.0f) : (Math.abs(RotationUtils.getRotations(entity)[0] - mc.thePlayer.rotationYaw) % 360.0f)) <= fov;
    }

    public static boolean inFoV(Entity entity, float fov, float currentYaw) {
        return ((Math.abs(RotationUtils.getRotations(entity)[0] - currentYaw) % 360.0f > 180.0f) ? (360.0f - Math.abs(RotationUtils.getRotations(entity)[0] - currentYaw) % 360.0f) : (Math.abs(RotationUtils.getRotations(entity)[0] - currentYaw) % 360.0f)) <= fov;
    }

    public static Vec3 getVectorForRotation(final Rotation rotation) {
        float yawCos = (float) Math.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float yawSin = (float) Math.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float pitchCos = (float) -Math.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = (float) Math.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static float getNewAngle(float angle) {
        angle %= 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    public static Vector2f applySensitivityPatch(final Vector2f rotation) {
        final Vector2f previousRotation = mc.thePlayer.getPreviousRotation();
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.x + (float) (Math.round((rotation.x - previousRotation.x) / multiplier) * multiplier);
        final float pitch = previousRotation.y + (float) (Math.round((rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }

    public static Vector2f applySensitivityPatch(final Vector2f rotation, final Vector2f previousRotation) {
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.x + (float) (Math.round((rotation.x - previousRotation.x) / multiplier) * multiplier);
        final float pitch = previousRotation.y + (float) (Math.round((rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }

    public static Vector2f smooth(final Vector2f lastRotation, final Vector2f targetRotation, final double speed) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;
        final float lastYaw = lastRotation.x;
        final float lastPitch = lastRotation.y;

        if (speed != 0) {
            final float rotationSpeed = (float) speed;

            final double deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.x - lastRotation.x);
            final double deltaPitch = pitch - lastPitch;

            final double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
            final double distributionYaw = Math.abs(deltaYaw / distance);
            final double distributionPitch = Math.abs(deltaPitch / distance);

            final double maxYaw = rotationSpeed * distributionYaw;
            final double maxPitch = rotationSpeed * distributionPitch;

            final float moveYaw = (float) Math.max(Math.min(deltaYaw, maxYaw), -maxYaw);
            final float movePitch = (float) Math.max(Math.min(deltaPitch, maxPitch), -maxPitch);

            yaw = lastYaw + moveYaw;
            pitch = lastPitch + movePitch;

            for (int i = 1; i <= (int) (Minecraft.getDebugFPS() / 20f + Math.random() * 10); ++i) {

                if (Math.abs(moveYaw) + Math.abs(movePitch) > 1) {
                    yaw += (float) ((Math.random() - 0.5) / 1000);
                    pitch -= (float) (Math.random() / 200);
                }

                /*
                 * Fixing GCD
                 */
                final Vector2f rotations = new Vector2f(yaw, pitch);
                final Vector2f fixedRotations = applySensitivityPatch(rotations);

                /*
                 * Setting rotations
                 */
                yaw = fixedRotations.x;
                pitch = Math.max(-90, Math.min(90, fixedRotations.y));
            }
        }

        return new Vector2f(yaw, pitch);
    }

    public static Vector2f resetRotation(final Vector2f rotation) {
        if (rotation == null) {
            return null;
        }

        final float yaw = rotation.x + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation.x);
        final float pitch = mc.thePlayer.rotationPitch;
        return new Vector2f(yaw, pitch);
    }

    public static float[] positionRotation(final double posX, final double posY, final double posZ, final float[] lastRots, final float yawSpeed, final float pitchSpeed, final boolean random) {
        final double x = posX - mc.thePlayer.posX;
        final double y = posY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double z = posZ - mc.thePlayer.posZ;
        final float calcYaw = (float) (MathHelper.atan2(z, x) * 180.0 / 3.141592653589793 - 90.0);
        final float calcPitch = (float) (-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 / 3.141592653589793));
        float yaw = updateRotation(lastRots[0], calcYaw, yawSpeed);
        float pitch = updateRotation(lastRots[1], calcPitch, pitchSpeed);
        if (random) {
            yaw += (float) ThreadLocalRandom.current().nextGaussian();
            pitch += (float) ThreadLocalRandom.current().nextGaussian();
        }
        return new float[]{yaw, pitch};
    }

    public static float[] positionRotation(final double posX, final double posY, final double posZ, final float[] lastRots, final float yawSpeed, final float pitchSpeed, final boolean random, final float partialTicks) {
        final double px = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partialTicks;
        final double py = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * partialTicks;
        final double pz = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partialTicks;
        final double x = posX - px;
        final double y = posY - (py + mc.thePlayer.getEyeHeight());
        final double z = posZ - pz;
        final float calcYaw = (float) (MathHelper.atan2(z, x) * 180.0 / 3.141592653589793 - 90.0);
        final float calcPitch = (float) (-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 / 3.141592653589793));
        float yaw = updateRotation(lastRots[0], calcYaw, yawSpeed);
        float pitch = updateRotation(lastRots[1], calcPitch, pitchSpeed);
        if (random) {
            yaw += (float) ThreadLocalRandom.current().nextGaussian();
            pitch += (float) ThreadLocalRandom.current().nextGaussian();
        }
        return new float[]{yaw, pitch};
    }

    public static float updateRotation(final float current, final float calc, final float maxDelta) {
        float f = MathHelper.wrapAngleTo180_float(calc - current);
        if (f > maxDelta) {
            f = maxDelta;
        }
        if (f < -maxDelta) {
            f = -maxDelta;
        }
        return current + f;
    }

    public float[] positionRotation(final double posX, final double posY, final double posZ, final float currentYaw, final float currentPitch, final boolean random) {
        final double x = posX - mc.thePlayer.posX;
        final double y = posY + 1.53 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double z = posZ - mc.thePlayer.posZ;
        final float calcYaw = (float) (MathHelper.atan2(z, x) * 180.0 / 3.141592653589793 - 90.0);
        final float calcPitch = (float) (-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 / 3.141592653589793));
        float yaw = updateRotation(currentYaw, calcYaw, 180.0f);
        float pitch = updateRotation(currentPitch, calcPitch, 180.0f);
        if (random) {
            yaw += (float) ThreadLocalRandom.current().nextGaussian();
            pitch += (float) ThreadLocalRandom.current().nextGaussian();
        }
        return mouseSens(yaw, pitch, currentYaw, currentPitch);
    }

    public float[] positionRotation(final double posX, final double posY, final double posZ, final float currentYaw, final float currentPitch, final float yawSpeed, final float pitchSpeed, final boolean random) {
        final double x = posX - mc.thePlayer.posX;
        final double y = posY + 1.53 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double z = posZ - mc.thePlayer.posZ;
        final float calcYaw = (float) (MathHelper.atan2(z, x) * 180.0 / 3.141592653589793 - 90.0);
        final float calcPitch = (float) (-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 / 3.141592653589793));
        float yaw = updateRotation(currentYaw, calcYaw, yawSpeed);
        float pitch = updateRotation(currentPitch, calcPitch, pitchSpeed);
        if (random) {
            yaw += (float) ThreadLocalRandom.current().nextGaussian();
            pitch += (float) ThreadLocalRandom.current().nextGaussian();
        }
        return mouseSens(yaw, pitch, currentYaw, currentPitch);
    }

    public static Vec3 getLook(float yaw, float pitch) {
        ComparableProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();

        if (targetVersion.isOlderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
            float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
            float f2 = -MathHelper.cos(-pitch * 0.017453292F);
            float f3 = MathHelper.sin(-pitch * 0.017453292F);
            return new Vec3(f1 * f2, f3, f * f2);
        } else {
            float f = pitch * ((float) Math.PI / 180F);
            float f1 = -yaw * ((float) Math.PI / 180F);
            float f2 = MathHelper.cos(f1);
            float f3 = MathHelper.sin(f1);
            float f4 = MathHelper.cos(f);
            float f5 = MathHelper.sin(f);
            return new Vec3(f3 * f4, -f5, (double) (f2 * f4));
        }
    }

    public static boolean isVecInside(AxisAlignedBB self, Vec3 vec) {
        return vec.xCoord > self.minX && vec.xCoord < self.maxX && (vec.yCoord > self.minY && vec.yCoord < self.maxY && vec.zCoord > self.minZ && vec.zCoord < self.maxZ);
    }


    public static Vector2f getRotations(Vec3 eye, Vec3 target) {
        double x = target.xCoord - eye.xCoord;
        double y = target.yCoord - eye.yCoord;
        double z = target.zCoord - eye.zCoord;

        double diffXZ = Math.sqrt(x * x + z * z);
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(y, diffXZ)));

        return new Vector2f(MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch));
    }

    public static double getMinDistance(Entity target, Vector2f rotation) {
        return getMinDistance(target, target.getCollisionBorderSize(), rotation);
    }

    public static double getMinDistance(Entity target, float hitbox, Vector2f rotation) {
        double minDistance = Double.MAX_VALUE;

        for (double eye : getPossibleEyeHeights()) {
            Vec3 playerPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Vec3 eyePos = playerPosition.addVector(0, eye, 0);

            minDistance = Math.min(minDistance, getDistance(target, eyePos, hitbox, rotation));
        }

        return minDistance;
    }

    public static double getDistance(Entity target, Vec3 eyePos, float hitbox, Vector2f rotations) {
        AxisAlignedBB targetBox = getTargetBoundingBox(target, hitbox);
        Vec3 lookVec = getLook(rotations.x, rotations.y);

        Vec3 endReachPos = eyePos.add(new Vec3(lookVec.xCoord * 6, lookVec.yCoord * 6, lookVec.zCoord * 6));
        MovingObjectPosition position = targetBox.calculateIntercept(eyePos, endReachPos);

        if (position != null) {
            Vec3 intercept = position.hitVec;
            return intercept.distanceTo(eyePos);
        }

        return 1000;
    }

    public static Data getRotationDataToEntity(Entity target, float hitbox) {
        return getRotationDataToEntity(target, hitbox, getPossibleEyeHeights());
    }

    public static MovingObjectPosition getIntercept(AxisAlignedBB targetBox, Vector2f rotations, Vec3 eyePos, double reach) {
        Vec3 lookVec = getLook(rotations.x, rotations.y);
        Vec3 endReachPos = eyePos.add(new Vec3(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach));
        return targetBox.calculateIntercept(eyePos, endReachPos);
    }

    public static MovingObjectPosition getIntercept(AxisAlignedBB targetBox, Vector2f rotations, Vec3 eyePos) {
        return getIntercept(targetBox, rotations, eyePos, 6);
    }

    public static Data getRotationDataToEntity(Entity target, float hitbox, List<Double> eyeHeights) {
        double minDistance = Double.MAX_VALUE;
        Vec3 bestEye = null, bestHitVec = null;
        Vector2f bestRotation = null;

        for (double eye : eyeHeights) {
            Vec3 playerPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Vec3 eyePos = playerPosition.addVector(0, eye, 0);

            AxisAlignedBB targetBox = getTargetBoundingBox(target, hitbox);

            Vec3 closestPoint = getClosestPoint(eyePos, targetBox);

            Vector2f basicRotation = getRotationsVector(new Vec3(target.posX, closestPoint.yCoord, target.posZ));
            MovingObjectPosition basicRotationIntercept = getIntercept(targetBox, basicRotation, eyePos);

            if (isVecInside(targetBox, eyePos)) {
                minDistance = 0;
                bestEye = eyePos;
                bestRotation = basicRotation;
                bestHitVec = basicRotationIntercept.hitVec;
                break;
            }

            if (basicRotationIntercept != null && basicRotationIntercept.hitVec != null) {
                if (basicRotationIntercept.hitVec.distanceTo(eyePos) < 3) {
                    minDistance = basicRotationIntercept.hitVec.distanceTo(eyePos);
                    bestEye = eyePos;
                    bestRotation = basicRotation;
                    bestHitVec = basicRotationIntercept.hitVec;
                    break;
                }
            }

            Vector2f rotations = getRotations(eyePos, closestPoint);
            MovingObjectPosition position = getIntercept(targetBox, rotations, eyePos);

            if (position == null) {
                continue;
            }

            Vec3 intercept = position.hitVec;

            if (isVecInside(targetBox, eyePos)) {
                minDistance = 0;
                bestEye = eyePos;
                bestRotation = rotations;
                bestHitVec = intercept;
                break;
            }

            if (intercept != null) {
                if (intercept.distanceTo(eyePos) < minDistance) {
                    minDistance = intercept.distanceTo(eyePos);
                    bestEye = eyePos;
                    bestRotation = rotations;
                    bestHitVec = intercept;
                }
            }
        }

        return new Data(bestEye, bestHitVec, minDistance, bestRotation);
    }

    @lombok.Data
    public static class Data {
        private final Vec3 eye, hitVec;
        private final double distance;
        private final Vector2f rotation;
    }

    private static AxisAlignedBB getTargetBoundingBox(Entity entity, float hitbox) {
        AxisAlignedBB expand = entity.getEntityBoundingBox().expand(hitbox, hitbox, hitbox);

        if (Disabler.disabled) {
            Double[] nextTickPosition = Disabler.getNextTickPosition(entity);
            if (nextTickPosition != null) {
                expand = expand.offset(nextTickPosition[0] - entity.posX, nextTickPosition[1] - entity.posY, nextTickPosition[2] - entity.posZ);
            }
        }

        return expand;
    }

    public static List<Double> getPossibleEyeHeights() { // We don't return sleeping eye height
        ComparableProtocolVersion targetVersion = ViaLoadingBase.getInstance().getTargetVersion();
        if (targetVersion.isNewerThanOrEqualTo(ProtocolVersion.v1_14)) { // Elytra, sneaking (1.14), standing
            return Arrays.asList(1.62, 1.27, 0.4);
        } else if (targetVersion.isNewerThanOrEqualTo(ProtocolVersion.v1_9)) { // Elytra, sneaking, standing
            return Arrays.asList(1.62, 1.54, 0.4);
        } else { // Only sneaking or standing
            return Arrays.asList((double) (1.62f), (double) (1.62f - 0.08f));
        }
    }

    public static Vec3 getClosestPoint(Vec3 vec, AxisAlignedBB aabb) {
        double closestX = Math.max(aabb.minX, Math.min(vec.xCoord, aabb.maxX));
        double closestY = Math.max(aabb.minY, Math.min(vec.yCoord, aabb.maxY));
        double closestZ = Math.max(aabb.minZ, Math.min(vec.zCoord, aabb.maxZ));

        return new Vec3(closestX, closestY, closestZ);
    }

    public static Rotation getRotationBlock(final BlockPos pos, float predict) {
        return new Rotation(mc.thePlayer.getPositionEyes(predict), new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }
    
    private static float[] getRotationsByVec(Vec3 origin, Vec3 position) {
        Vec3 difference = position.subtract(origin);
        double distance = difference.flat().lengthVector();
        float yaw = (float)Math.toDegrees(Math.atan2(difference.zCoord, difference.xCoord)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(difference.yCoord, distance)));
        return new float[]{yaw, pitch};
    }
    
    public static float[] getRotationBlock(BlockPos pos) {
        return getRotationsByVec(mc.thePlayer.getPositionVector().addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0), new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5));
    }
    @Getter
    public static class VecRotation {
        Vec3 vec;
        Rotation rotation;

        public VecRotation(Vec3 vec, Rotation rotation) {
            this.vec = vec;
            this.rotation = rotation;
        }
    }

    @Setter
    @Getter
    public static class Rotation {
        float yaw, pitch;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public Rotation(Vec3 from, Vec3 to) {
            final Vec3 diff = to.subtract(from);

            this.yaw = MathHelper.wrapDegrees(
                    (float) Math.toDegrees(Math.atan2(diff.zCoord, diff.xCoord)) - 90F
            );
            this.pitch = MathHelper.wrapDegrees(
                    (float) (-Math.toDegrees(Math.atan2(diff.yCoord, Math.sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord))))
            );
        }
        public static float updateRotation(final float current, final float calc, final float maxDelta) {
            float f = MathHelper.wrapAngleTo180_float(calc - current);
            if (f > maxDelta) {
                f = maxDelta;
            }
            if (f < -maxDelta) {
                f = -maxDelta;
            }
            return current + f;
        }

        public void toPlayer(EntityPlayer player) {
            if (Float.isNaN(yaw) || Float.isNaN(pitch))
                return;

            fixedSensitivity(mc.gameSettings.mouseSensitivity);

            player.rotationYaw = yaw;
            player.rotationPitch = pitch;
        }

        public void fixedSensitivity(Float sensitivity) {
            float f = sensitivity * 0.6F + 0.2F;
            float gcd = f * f * f * 1.2F;

            yaw -= yaw % gcd;
            pitch -= pitch % gcd;
        }

        public float rotateToYaw(final float yawSpeed, final float currentYaw, final float calcYaw) {
            float yaw = updateRotation(currentYaw, calcYaw, yawSpeed);
            final double diffYaw = MathHelper.wrapAngleTo180_float(calcYaw - currentYaw);

            if (-yawSpeed > diffYaw || diffYaw > yawSpeed) {
                yaw += (float) Math.sin(RotationUtils.mc.thePlayer.rotationPitch * 3.141592653589793);
            }
            if (yaw == currentYaw) {
                return currentYaw;
            }

            if (mc.gameSettings.mouseSensitivity == 0.5) {
                mc.gameSettings.mouseSensitivity = 0.47887325f;
            }

            final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            final float f2 = f1 * f1 * f1 * 8.0f;
            final int deltaX = (int) ((6.667 * yaw - 6.666666666666667 * currentYaw) / f2);
            final float f3 = deltaX * f2;
            yaw = (float) (currentYaw + f3 * 0.15);
            return yaw;
        }

        public float rotateToYaw(final float yawSpeed, final float[] currentRots, final float calcYaw) {
            float yaw = updateRotation(currentRots[0], calcYaw, yawSpeed);
            if (yaw != calcYaw) {
                yaw += (float) Math.sin(currentRots[1] * 3.141592653589793);
            }
            if (yaw == currentRots[0]) {
                return currentRots[0];
            }

            if (mc.gameSettings.mouseSensitivity == 0.5) {
                mc.gameSettings.mouseSensitivity = 0.47887325f;
            }

            final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            final float f2 = f1 * f1 * f1 * 8.0f;
            final int deltaX = (int) ((6.667 * yaw - 6.6666667 * currentRots[0]) / f2);
            final float f3 = deltaX * f2;

            yaw = (float) (currentRots[0] + f3 * 0.15);
            return yaw;
        }

        public float rotateToPitch(final float pitchSpeed, final float currentPitch, final float calcPitch) {
            float pitch = updateRotation(currentPitch, calcPitch, pitchSpeed);
            if (pitch != calcPitch) {
                pitch += (float) Math.sin(mc.thePlayer.rotationYaw * 3.141592653589793);
            }
            if (mc.gameSettings.mouseSensitivity == 0.5) {
                mc.gameSettings.mouseSensitivity = 0.47887325f;
            }
            final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            final float f2 = f1 * f1 * f1 * 8.0f;
            final int deltaY = (int) ((6.667 * pitch - 6.666667 * currentPitch) / f2) * -1;
            final float f3 = deltaY * f2;
            final float f4 = (float) (currentPitch - f3 * 0.15);
            pitch = MathHelper.clamp_float(f4, -90.0f, 90.0f);
            return pitch;
        }

        public float rotateToPitch(final float pitchSpeed, final float[] currentRots, final float calcPitch) {
            float pitch = updateRotation(currentRots[1], calcPitch, pitchSpeed);
            if (pitch != calcPitch) {
                pitch += (float) Math.sin(currentRots[0] * 3.141592653589793);
            }
            if (mc.gameSettings.mouseSensitivity == 0.5) {
                mc.gameSettings.mouseSensitivity = 0.47887325f;
            }
            final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            final float f2 = f1 * f1 * f1 * 8.0f;
            final int deltaY = (int) ((6.667 * pitch - 6.666667 * currentRots[1]) / f2) * -1;
            final float f3 = deltaY * f2;
            final float f4 = (float) (currentRots[1] - f3 * 0.15);
            pitch = MathHelper.clamp_float(f4, -90.0f, 90.0f);
            return pitch;
        }
    }
}
