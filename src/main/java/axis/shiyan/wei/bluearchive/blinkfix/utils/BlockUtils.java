package axis.shiyan.wei.bluearchive.blinkfix.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class BlockUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    public static Block getBlockUnderPlayer(EntityPlayer player) {
        return getBlockUnderPlayer(player, 1d);
    }

    public static Block getBlockUnderPlayer(EntityPlayer player, double yOffset) {
        return getBlock(new BlockPos(player.posX, player.posY - yOffset, player.posZ));
    }

    public static boolean isAirBlock(final BlockPos blockPos) {
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockAir;
    }


    public static boolean isVoid(EntityLivingBase entity, int offsetX, int offsetZ) {
        for (int i = (int) (entity.posY - 1.0); i > 0; --i) {
            BlockPos pos = new BlockPos(entity.posX + offsetX, i, entity.posZ + offsetZ);
            if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isReallyOnGround() {
        EntityPlayer entity = Minecraft.getMinecraft().thePlayer;
        double y = entity.getEntityBoundingBox().offset(0.0D, -0.01D, 0.0D).minY;
        Block block = mc.theWorld.getBlockState(new BlockPos(entity.posX, y, entity.posZ)).getBlock();
        if (block != null && !(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return entity.onGround;
        }
        return false;
    }

    public static boolean isPlayerInLiquid() {
        AxisAlignedBB par1AxisAlignedBB = mc.thePlayer.getEntityBoundingBox().contract(0.001, 0.001, 0.001);

        int minX = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int maxX = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0);

        int minY = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int maxY = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0);

        int minZ = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int maxZ = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0);

        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
                    if (block instanceof BlockLiquid) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isPlayerOnLiquid() {
        AxisAlignedBB axisAlignedBB = Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().offset(0.0, -0.1, 0.0).contract(0.001D, 0.0D, 0.001D);

        int xMin = MathHelper.floor_double(axisAlignedBB.minX);
        int xMax = MathHelper.floor_double(axisAlignedBB.maxX + 1.0);

        int yMin = MathHelper.floor_double(axisAlignedBB.minY);
        int yMax = MathHelper.floor_double(axisAlignedBB.maxY + 1.0);

        int zMin = MathHelper.floor_double(axisAlignedBB.minZ);
        int zMax = MathHelper.floor_double(axisAlignedBB.maxZ + 1.0);

        boolean gotcha = false;

        for (int y = yMin; y < yMax; y++) {
            for (int x = xMin; x < xMax; x++) {
                for (int z = zMin; z < zMax; z++) {
                    Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();

                    if (block instanceof BlockLiquid)
                        gotcha = true;

                    if (!(block instanceof BlockLiquid) && block.getCollisionBoundingBox(Minecraft.getMinecraft().theWorld, new BlockPos(x, y, z), Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z))) != null) {
                        return false;
                    }
                }
            }
        }

        return gotcha;
    }
    public static Map<BlockPos, Block> searchBlocks(final int radius) {
        final Map<BlockPos, Block> blocks = new HashMap<BlockPos, Block>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    final BlockPos blockPos = new BlockPos(mc.thePlayer.lastTickPosX + x, mc.thePlayer.lastTickPosY + y, mc.thePlayer.lastTickPosZ + z);
                    final Block block = getBlock(blockPos);
                    blocks.put(blockPos, block);
                }
            }
        }
        return blocks;
    }
    public static boolean isCollidedWithStairs() {
        return MoveUtils.getBlockAtPosC(0.3100000023841858, 0.0, 0.3100000023841858) instanceof BlockStairs || MoveUtils.getBlockAtPosC(-0.3100000023841858, 0.0, -0.3100000023841858) instanceof BlockStairs || MoveUtils.getBlockAtPosC(0.3100000023841858, 0.0, -0.3100000023841858) instanceof BlockStairs || MoveUtils.getBlockAtPosC(-0.3100000023841858, 0.0, 0.3100000023841858) instanceof BlockStairs || MoveUtils.getBlockatPosSpeed(mc.thePlayer, 1.05f, 1.05f) instanceof BlockStairs;
    }
}
