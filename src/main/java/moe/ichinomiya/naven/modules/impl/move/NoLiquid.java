package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.manager.NotificationLevel;
import moe.ichinomiya.naven.utils.MoveUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "NoLiquid", description = "Allows you to walk faster on liquid", category = Category.MOVEMENT)
public class NoLiquid extends Module {
    public static boolean shouldCancelLiquid = false;

    @Override
    public void setEnabled(boolean enabled) {
        if (mc.thePlayer == null || MoveUtils.getSpeed() == 0 || (!mc.thePlayer.isInWater() && !mc.thePlayer.isInLava())) {
            super.setEnabled(enabled);
        } else {
            BlinkFix.getInstance().getNotificationManager().addNotification(new Notification(NotificationLevel.ERROR, "You can't toggle this module now!", 3000));
        }
    }

    @Override
    public boolean onEnable() {
        shouldCancelLiquid = false;
        return false;
    }

    @Override
    public boolean onDisable() {
        shouldCancelLiquid = false;
        return false;
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        shouldCancelLiquid = false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && mc.thePlayer != null && mc.thePlayer.ticksExisted > 50) {
            shouldCancelLiquid = false;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                        if (block == Blocks.water || block == Blocks.lava) {
                            shouldCancelLiquid = true;
                            mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, EnumFacing.UP));
                            mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
                        }
                    }
                }
            }
        }
    }
}
