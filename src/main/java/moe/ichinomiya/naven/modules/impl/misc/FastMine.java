package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.move.Blink;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "FastMine", description = "Mine blocks faster", category = Category.MISC)
public class FastMine extends Module {
    BooleanValue abortBreaking = ValueBuilder.create(this, "Abort Breaking").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue speed = ValueBuilder.create(this, "Speed").setDefaultFloatValue(1.3f).setFloatStep(0.1f).setMinFloatValue(1f).setMaxFloatValue(2f).build().getFloatValue();

    C07PacketPlayerDigging packet;
    float damage;

    @EventTarget
    public void onPacket(EventPacket e) {
        if (!mc.isSingleplayer() && !BlinkFix.getInstance().getModuleManager().getModule(Blink.class).isEnabled()) {
            if (e.getPacket() instanceof C07PacketPlayerDigging) {
                C07PacketPlayerDigging packet = (C07PacketPlayerDigging) e.getPacket();

                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos position = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY - y, mc.thePlayer.posZ + z);
                            if (packet.getPosition().equals(position)) {
                                return;
                            }
                        }
                    }
                }

                C07PacketPlayerDigging.Action action = packet.getStatus();
                if (action == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                    this.packet = packet;
                    damage = 0.0f;

                    if (abortBreaking.getCurrentValue()) {
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(e.getPacket());
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, packet.getPosition(), packet.getFacing()));
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, packet.getPosition(), packet.getFacing()));
                        e.setCancelled(true);
                    }
                } else if (action == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK
                        || action == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                    this.packet = null;
                }
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && packet != null) {
            IBlockState blockState = mc.theWorld.getBlockState(packet.getPosition());
            damage += blockState.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, packet.getPosition()) * speed.getCurrentValue();

            if (damage >= 1.0f) {
                mc.theWorld.setBlockToAir(packet.getPosition());
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, packet.getPosition(), packet.getFacing()));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, packet.getPosition(), packet.getFacing()));
                damage = 0.0f;
                packet = null;
            }
        }
    }
}
