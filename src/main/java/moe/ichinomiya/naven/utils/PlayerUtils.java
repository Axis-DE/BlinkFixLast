package moe.ichinomiya.naven.utils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;

import java.util.Arrays;

public class PlayerUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void swapItem(final int slot, final int clickSlot) {
        clickSlot(slot, clickSlot, 2);
    }

    public static void clickSlot(int slot, int clickSlot, boolean shiftClick) {
        clickSlot(slot, clickSlot, shiftClick ? 1 : 0);
    }
    public static int getJumpEffect() {
        if (mc.thePlayer.isPotionActive(Potion.jump))
            return mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1;
        else
            return 0;
    }

    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty();
    }

    public static boolean isOnGround(Entity entity, double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty();
    }

    public static void setSpeed(final double speed) {
        mc.thePlayer.motionX = -(Math.sin(getDirection()) * speed);
        mc.thePlayer.motionZ = Math.cos(getDirection()) * speed;
    }

    public static float getDirection() {
        float yaw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0f) {
            yaw += 180.0f;
        }
        float forward = 1.0f;
        if (mc.thePlayer.moveForward < 0.0f) {
            forward = -0.5f;
        } else if (mc.thePlayer.moveForward > 0.0f) {
            forward = 0.5f;
        }
        if (mc.thePlayer.moveStrafing > 0.0f) {
            yaw -= 90.0f * forward;
        }
        if (mc.thePlayer.moveStrafing < 0.0f) {
            yaw += 90.0f * forward;
        }
        yaw *= 0.017453292f;
        return yaw;
    }

    public static double getBaseMoveSpeed() {
        final EntityPlayerSP player = mc.thePlayer;
        double base = 0.2895;
        final PotionEffect moveSpeed = player.getActivePotionEffect(Potion.moveSpeed);
        final PotionEffect moveSlowness = player.getActivePotionEffect(Potion.moveSlowdown);
        if (moveSpeed != null)
            base *= 1.0 + 0.19 * (moveSpeed.getAmplifier() + 1);

        if (moveSlowness != null)
            base *= 1.0 - 0.13 * (moveSlowness.getAmplifier() + 1);

        if (player.isInWater()) {
            base *= 0.5203619984250619;
            final int depthStriderLevel = EnchantmentHelper.getDepthStriderModifier(mc.thePlayer);

            if (depthStriderLevel > 0) {
                double[] DEPTH_STRIDER_VALUES = new double[]{1.0, 1.4304347400741908, 1.7347825295420374, 1.9217391028296074};
                base *= DEPTH_STRIDER_VALUES[depthStriderLevel];
            }

        } else if (player.isInLava()) {
            base *= 0.5203619984250619;
        }
        return base;
    }

    public static double getSpeed() {
        return getSpeed(mc.thePlayer);
    }

    public static double getSpeed(Entity entity) {
        return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
    }

    public static void clickSlot(int slot, int clickSlot, int mode) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, clickSlot, mode, mc.thePlayer);
    }

    public static double getDistanceToFall() {
        double distance = 0.0;

        for (double i = mc.thePlayer.posY; i > 0.0; --i) {
            Block block = BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, i, mc.thePlayer.posZ));
            if (block.getMaterial() != Material.air && block.isBlockNormalCube() && block.isCollidable()) {
                distance = i;
                break;
            }
        }

        return mc.thePlayer.posY - distance - 1.0;
    }

    public static boolean isFull() {
        return !Arrays.asList(mc.thePlayer.inventory.mainInventory).contains(null);
    }

    public static boolean hasEffect(int potionId) {
        for (PotionEffect item : mc.thePlayer.getActivePotionEffects()) {
            if (item.getPotionID() == potionId)
                return true;
        }
        return false;
    }

    public static boolean isMoving() {
        return ((mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F));
    }

    public static boolean movementInput() {
        return mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown();
    }
}
