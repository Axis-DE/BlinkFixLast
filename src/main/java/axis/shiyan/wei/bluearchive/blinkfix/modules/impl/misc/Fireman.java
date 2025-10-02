package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import com.google.common.collect.ImmutableMap;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.WorldMonitor;
import net.minecraft.block.BlockFire;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.HashSet;
import java.util.Set;

@ModuleInfo(name = "Fireman", description = "Extinguishes fires", category = Category.MISC)
public class Fireman extends Module {
    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            Set<BlockPos> fires = new HashSet<>(WorldMonitor.getFires());
            fires.removeIf(pos -> mc.thePlayer.getDistance(pos.getX(), pos.getY(), pos.getZ()) > 5);

            for (BlockPos pos : fires) {
                IBlockState state = Blocks.fire.getActualState(mc.theWorld.getBlockState(pos), mc.theWorld, pos);
                ImmutableMap<IProperty, Comparable> properties = state.getProperties();

                boolean north = (boolean) properties.get(BlockFire.NORTH);
                boolean south = (boolean) properties.get(BlockFire.SOUTH);
                boolean east = (boolean) properties.get(BlockFire.EAST);
                boolean west = (boolean) properties.get(BlockFire.WEST);
                boolean flip = (boolean) properties.get(BlockFire.FLIP);

                if (north) {
                    BlockPos newPos = pos.offset(EnumFacing.NORTH);
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, newPos, EnumFacing.SOUTH));
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, newPos, EnumFacing.DOWN));
                } else if (south) {
                    BlockPos newPos = pos.offset(EnumFacing.SOUTH);
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, newPos, EnumFacing.NORTH));
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, newPos, EnumFacing.DOWN));
                } else if (east) {
                    BlockPos newPos = pos.offset(EnumFacing.EAST);
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, newPos, EnumFacing.WEST));
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, newPos, EnumFacing.DOWN));
                } else if (west) {
                    BlockPos newPos = pos.offset(EnumFacing.WEST);
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, newPos, EnumFacing.EAST));
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, newPos, EnumFacing.DOWN));
                } else if (flip) {
                    BlockPos newPos = pos.offset(EnumFacing.UP);
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, newPos, EnumFacing.DOWN));
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, newPos, EnumFacing.DOWN));
                } else {
                    BlockPos newPos = pos.offset(EnumFacing.DOWN);
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, newPos, EnumFacing.UP));
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, newPos, EnumFacing.DOWN));
                }
            }
        }
    }
}
