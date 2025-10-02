package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMoveInput;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRender;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.Render3DUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "HoleHelper", description = "Helps you find holes", category = Category.MOVEMENT)
public class HoleHelper extends Module {
    public static Vec3 targetVec;
    TimeHelper timer = new TimeHelper(), stop = new TimeHelper();

    boolean lastTickOnLadder = false;

    @EventTarget
    public void onRender(EventRender e) {
        if (targetVec != null) {
            Render3DUtils.drawSolidBoundingBox(new AxisAlignedBB(targetVec.xCoord - 0.2, targetVec.yCoord - 0.2, targetVec.zCoord - 0.2, targetVec.xCoord + 0.2, targetVec.yCoord + 0.2, targetVec.zCoord + 0.2), 1, 0, 0, 0.2f);
        }
    }

    @EventTarget
    public void onMove(EventMoveInput e) {
        if (!stop.delay(1000) && mc.thePlayer.isOnLadder() && !mc.thePlayer.onGround) {
            e.setForward(0);
            e.setStrafe(0);
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            targetVec = null;

            if (!lastTickOnLadder && mc.thePlayer.isOnLadder()) {
                stop.reset();
            }
            lastTickOnLadder = mc.thePlayer.isOnLadder();

            if (!mc.thePlayer.isOnLadder()) {
                if (timer.delay(1000)) {
                    for (int x = -2; x <= 2; x ++) {
                        for (int z = -2; z <= 2; z ++) {
                            BlockPos position = mc.thePlayer.getPosition().add(x, -1, z);
                            IBlockState state = mc.theWorld.getBlockState(position);
                            Block block = state.getBlock();

                            if (block == Blocks.ladder && mc.thePlayer.getDistance(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5) <= 1.5) {
                                EnumFacing facing = state.getValue(net.minecraft.block.BlockLadder.FACING);

                                double offset = 0.15;
                                if (facing == EnumFacing.NORTH) {
                                    targetVec = new Vec3(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5 - offset);
                                } else if (facing == EnumFacing.SOUTH) {
                                    targetVec = new Vec3(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5 + offset);
                                } else if (facing == EnumFacing.EAST) {
                                    targetVec = new Vec3(position.getX() + 0.5 + offset, position.getY() + 0.5, position.getZ() + 0.5);
                                } else if (facing == EnumFacing.WEST) {
                                    targetVec = new Vec3(position.getX() + 0.5 - offset, position.getY() + 0.5, position.getZ() + 0.5);
                                }
                            }
                        }
                    }
                }
            } else {
                timer.reset();
            }
        }
    }
}
