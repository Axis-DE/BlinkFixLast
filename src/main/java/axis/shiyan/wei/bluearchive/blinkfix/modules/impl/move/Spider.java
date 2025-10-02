package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventBondingBoxSet;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Spider", description = "Allows you to climb up walls", category = Category.MOVEMENT)
public class Spider extends Module {
    public static boolean shouldCancelClick = false;
    List<BlockPos> positions = new ArrayList<>();

    @Override
    public void onDisable() {
        shouldCancelClick = false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            shouldCancelClick = false;
            for (BlockPos position : positions) {
                shouldCancelClick = true;
                for (int i = 0; i < 2; i++) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, position.up(i), EnumFacing.DOWN));
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, position.up(i), EnumFacing.DOWN));
                }
            }

            positions.clear();
        }
    }

    @EventTarget
    public void onBB(EventBondingBoxSet e) {
        if (e.getPos().getY() > mc.thePlayer.posY && e.getBoundingBox() != null) {
            if (e.getBlock() != Blocks.bedrock && e.getBlock() != Blocks.barrier) {
                positions.add(new BlockPos(e.getBoundingBox().minX, e.getBoundingBox().minY, e.getBoundingBox().minZ));
                e.setCancelled(true);
            }
        }
    }
}
