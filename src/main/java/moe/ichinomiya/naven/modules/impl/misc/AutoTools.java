package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.protocols.HuaYuTing.HYTUtils;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.BlockWeb;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "AutoTools", description = "Automatically switches to the best tool", category = Category.MISC)
public class AutoTools extends Module {
    BooleanValue switchSword = ValueBuilder.create(this, "Switch Sword").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue swordCheck = ValueBuilder.create(this, "Sword Check").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue switchBack = ValueBuilder.create(this, "Switch Back").setDefaultBooleanValue(false).build().getBooleanValue();
    boolean shouldSwitchSword = false;

    @EventTarget
    public void onAttack(EventPacket e) {
        if (switchSword.getCurrentValue() && (e.getPacket() instanceof C02PacketUseEntity) && ((C02PacketUseEntity) e.getPacket()).getAction().equals(C02PacketUseEntity.Action.ATTACK)) {
            if (!mc.thePlayer.isEating() && (mc.thePlayer.getHeldItem() == null || (!HYTUtils.isGodAxe(mc.thePlayer.getHeldItem()) && !HYTUtils.isKBBall(mc.thePlayer.getHeldItem())))) {
                shouldSwitchSword = true;
            }
        }
    }

    private int oldSlot;
    private int tick;

    @EventTarget
    public void onPre(EventMotion event) {
        if (event.getType() == EventType.PRE && !mc.gameSettings.keyBindUseItem.pressed) {
            if (mc.playerController.isBreakingBlock()) {
                ItemStack itemStack = mc.thePlayer.getHeldItem();

                if (itemStack != null && itemStack.getItem() instanceof ItemSword) {
                    if (swordCheck.getCurrentValue()) {
                        return;
                    }
                }

                tick ++;

                if (tick == 1) {
                    oldSlot = mc.thePlayer.inventory.currentItem;
                }

                if ((mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) && !mc.thePlayer.capabilities.isCreativeMode) {
                    BlockPos blockPosHit = mc.objectMouseOver.getBlockPos();
                    if (blockPosHit != null) {
                        mc.thePlayer.inventory.currentItem = getBestTool(blockPosHit);
                    }
                }
            } else if (tick > 0) {
                if (switchBack.getCurrentValue()) {
                    mc.thePlayer.inventory.currentItem = oldSlot;
                }

                tick = 0;
            }

            if (shouldSwitchSword) {
                bestSword();
                shouldSwitchSword = false;
            }
        }
    }

    public void bestSword() {
        int bestSlot = 0;
        for (int i1 = 36; i1 < 45; i1++) {
            if (mc.thePlayer.inventoryContainer.inventorySlots.toArray()[i1] != null) {
                ItemStack curSlot = mc.thePlayer.inventoryContainer.getSlot(i1).getStack();
                if (curSlot != null && InventoryManager.shouldSwapSword(curSlot)) {
                    bestSlot = i1 - 36;
                }
            }
        }

        mc.thePlayer.inventory.currentItem = bestSlot;
        mc.playerController.updateController();
    }

    private int getBestTool(BlockPos pos) {
        final Block block = mc.theWorld.getBlockState(pos).getBlock();
        int slot = 0;
        float dmg = 1;

        for (int index = 36; index < 45; index ++) {
            ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();

            if (HYTUtils.isGodAxe(itemStack)) {
                continue;
            }

            if (itemStack != null && block != null && (!(itemStack.getItem() instanceof ItemSword) || block instanceof BlockWeb)) {
                float strVsBlock = itemStack.getItem().getStrVsBlock(itemStack, block);

                if (strVsBlock > 1 && !(block instanceof BlockOre || block instanceof BlockRedstoneOre)) {
                    int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
                    if (i > 0) {
                        strVsBlock += (float) (i * i + 1);
                    }
                }

                if (strVsBlock > dmg) {
                    slot = index - 36;
                    dmg = strVsBlock;
                }
            }
        }

        if (dmg > 1F) {
            return slot;
        }

        return mc.thePlayer.inventory.currentItem;
    }
}
