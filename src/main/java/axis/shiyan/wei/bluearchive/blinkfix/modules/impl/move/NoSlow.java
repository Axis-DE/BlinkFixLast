package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import io.netty.buffer.Unpooled;
import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventPacket;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventSlowdown;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc.Disabler;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.HYTUtils;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "NoSlow", description = "Prevents you from slowing down when eating", category = Category.MOVEMENT)
public class NoSlow extends Module {
    BooleanValue sword = ValueBuilder.create(this, "Sword").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue food = ValueBuilder.create(this, "Food").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue bow = ValueBuilder.create(this, "Bow").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue foodUsingStateFix = ValueBuilder.create(this, "Fix Food State").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue bedwars = ValueBuilder.create(this, "BW Run Eat").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue hypixel = ValueBuilder.create(this, "Hypixel").setDefaultBooleanValue(true).build().getBooleanValue();
    boolean serverSetSlot = false;
    private boolean needsend = false;

    @EventTarget
    public void onSlowdown(EventSlowdown e) {
        if (e.isSlowdown()) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            boolean isBedwarsValid = bedwars.getCurrentValue() && serverSetSlot && !HYTUtils.isEnchantedGoldenApple(heldItem);
            if (heldItem != null && heldItem.getItem() instanceof ItemFood && food.getCurrentValue() && heldItem.stackSize >= 2 && !HYTUtils.isEnchantedGoldenApple(heldItem) && serverSetSlot && !bedwars.getCurrentValue() && !hypixel.getCurrentValue()) {
                e.setSlowdown(false);
            } else if (heldItem != null && heldItem.getItem() instanceof ItemSword && sword.getCurrentValue()) {
                e.setSlowdown(false);
            } else if (heldItem != null && heldItem.getItem() instanceof ItemBow && bow.getCurrentValue() && !mc.thePlayer.isSneaking() && !hypixel.getCurrentValue()) {
                e.setSlowdown(false);
            }else if (heldItem != null && heldItem.getItem() instanceof ItemFood  && food.getCurrentValue() && bedwars.getCurrentValue() && !HYTUtils.isEnchantedGoldenApple(heldItem) && serverSetSlot && !hypixel.getCurrentValue()) {
                e.setSlowdown(false);
            }
            else if (heldItem != null && heldItem.getItem() instanceof ItemPotion && isBedwarsValid) {
                if (!ItemPotion.isSplash(heldItem.getMetadata())) {
                    e.setSlowdown(false);
                }
            }
        }
    }

    @EventTarget
    public void onPre(EventMotion e) {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (!hypixel.getCurrentValue()) {

            if (e.getType() == EventType.PRE && mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemBow && bow.getCurrentValue() && !mc.thePlayer.isSneaking()) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new CPacketPlayerTryUseItem(1));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C17PacketCustomPayload("郭光洲死了", new PacketBuffer(Unpooled.wrappedBuffer(new byte[]{1}))));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            }

            if (sword.getCurrentValue()) {
                if (BlinkFix.getInstance().getModuleManager().getModule(Disabler.class).isEnabled()) {
                    if (e.getType() == EventType.PRE) {
                        if (mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemSword) {
                            mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }
                    } else {
                        if (mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemSword) {
                            if (Disabler.disabled) {
                                mc.getNetHandler().getNetworkManager().sendPacket(new CPacketPlayerTryUseItem(1));
                            }
                        }
                    }
                } else {
                    if (e.getType() == EventType.PRE && mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemSword) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new CPacketPlayerTryUseItem(1));
                    }
                }
            }

            if (e.getType() == EventType.PRE && foodUsingStateFix.getCurrentValue()) {
                if (mc.thePlayer.isUsingItem() && !mc.thePlayer.serverUsingItem && mc.thePlayer.getItemInUseCount() < 25) {
                    mc.thePlayer.stopUsingItem();
                }
            }
        }
        if (hypixel.getCurrentValue()){
            if (e.getType() == EventType.PRE){
                if (mc.thePlayer.getCurrentEquippedItem() == null) return;
                final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();
            }

        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        Packet<?> packet = e.getPacket();

        if (mc.thePlayer == null) {
            return;
        }
        boolean isBedwarsValid = bedwars.getCurrentValue() && serverSetSlot;
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemFood && food.getCurrentValue()) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement currentPacket = (C08PacketPlayerBlockPlacement) packet;
                if (currentPacket.getPlacedBlockDirection() == 255 && currentPacket.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a) && heldItem.stackSize >= 2 && !HYTUtils.isEnchantedGoldenApple(heldItem)) {
                    serverSetSlot = false;
                    if (!BlinkFix.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()) {
                        if (bedwars.getCurrentValue()) return;
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                }
                if ( mc.thePlayer != null && currentPacket.getPlacedBlockDirection() == 255 && currentPacket.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a) && NoSlow.mc.thePlayer.getHeldItem() != null && (NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood )) {
                    if (bedwars.getCurrentValue()) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C01PacketChatMessage("/lizi open"));
                    }
                }
                if (heldItem.getItem() instanceof ItemPotion && isBedwarsValid) {
                    if (!ItemPotion.isSplash(heldItem.getMetadata())) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C01PacketChatMessage("/lizi open"));;
                    }
                }
            }

            if (packet instanceof S30PacketWindowItems && mc.thePlayer.isUsingItem()) {
                e.setCancelled(true);
            }
            if (bedwars.getCurrentValue() && NoSlow.mc.thePlayer.getHeldItem() != null && (NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && packet instanceof S2DPacketOpenWindow) {
                e.setCancelled(true);
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent( new C0DPacketCloseWindow(((S2DPacketOpenWindow) packet).getWindowId()));
            }
            if (bedwars.getCurrentValue() && NoSlow.mc.thePlayer.getHeldItem() != null && NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && packet instanceof S2DPacketOpenWindow) {
                e.setCancelled(true);
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent( new C0DPacketCloseWindow(((S2DPacketOpenWindow) packet).getWindowId()));
            }
        }

            if (packet instanceof S2FPacketSetSlot) {
                serverSetSlot = true;
                if (mc.thePlayer.isUsingItem()) {
                    e.setCancelled(true);
                }
            }
        }


}

