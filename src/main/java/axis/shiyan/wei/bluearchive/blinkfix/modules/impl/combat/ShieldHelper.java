package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRespawn;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.Disabler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "ShieldHelper", description = "Automatically uses shields", category = Category.COMBAT)
public class ShieldHelper extends Module {
    public static boolean needDisabler = false;

    @EventTarget
    public void onRespawn(EventRespawn e) {
        needDisabler = false;
    }

    @Override
    public void onDisable() {
        needDisabler = false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            needDisabler = false;
        }

        if (mc.thePlayer.getHeldItem() != null) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem.getDisplayName().equals("§8防爆盾§8")) {
                if (e.getType() == EventType.PRE) {
                    needDisabler = true;
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                } else {
                    if (Disabler.disabled) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    }
                }
            }
        }
    }
}
