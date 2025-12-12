package moe.ichinomiya.naven.modules.impl.move;


import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.combat.Aura;
import moe.ichinomiya.naven.utils.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Map;
@ModuleInfo(name = "FastLadder", description = "Fall Fast When You On Ladder", category = Category.MOVEMENT)
public class FastLadder extends Module {


    @Override
    public boolean onEnable() {
        return false;
    }


    @EventTarget
    public void onUpdate(final EventMotion event) {
        if (Aura.target != null)
            return;


        if (mc.thePlayer.isOnLadder() && mc.gameSettings.keyBindJump.pressed) {
            if (mc.thePlayer.motionY >= 0.0) {
                mc.thePlayer.motionY = 0.1786;
            }
        }
        Map<BlockPos, Block> searchBlock = BlockUtils.searchBlocks(2);


        for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
            if (mc.thePlayer.isOnLadder() && !mc.gameSettings.keyBindJump.pressed) {
                if (mc.theWorld.getBlockState(block.getKey()).getBlock() instanceof BlockLadder) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                    mc.theWorld.setBlockToAir(block.getKey());
                }
            }
        }

    }


}
