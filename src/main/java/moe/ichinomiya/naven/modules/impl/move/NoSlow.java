package moe.ichinomiya.naven.modules.impl.move;

import io.netty.buffer.Unpooled;
import moe.ichinomiya.naven.modules.impl.combat.Aura;
import moe.ichinomiya.naven.utils.Managers.BlinkComponent;
import moe.ichinomiya.naven.utils.RotationManager;
import moe.ichinomiya.naven.utils.RotationUtils;
import moe.ichinomiya.naven.values.impl.ModeValue;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventSlowdown;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.misc.Disabler;
import moe.ichinomiya.naven.protocols.HuaYuTing.HYTUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
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
    public ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Grim","Vanilla").build().getModeValue();
    BooleanValue sword = ValueBuilder.create(this, "Sword").setDefaultBooleanValue(true).setVisibility(() -> mode.isCurrentMode("Grim")).build().getBooleanValue();
    BooleanValue food = ValueBuilder.create(this, "Food").setDefaultBooleanValue(true).setVisibility(() -> mode.isCurrentMode("Grim")).build().getBooleanValue();
    BooleanValue bow = ValueBuilder.create(this, "Bow").setDefaultBooleanValue(false).setVisibility(() -> mode.isCurrentMode("Grim")).build().getBooleanValue();
    BooleanValue foodUsingStateFix = ValueBuilder.create(this, "Fix Food State").setDefaultBooleanValue(true).setVisibility(() -> mode.isCurrentMode("Grim")).build().getBooleanValue();
    BooleanValue bedwars = ValueBuilder.create(this, "BW Run Eat").setDefaultBooleanValue(false).setVisibility(() -> mode.isCurrentMode("Grim")).build().getBooleanValue();
    BooleanValue test = ValueBuilder.create(this, "Test").setDefaultBooleanValue(false).setVisibility(() -> mode.isCurrentMode("Grim")).build().getBooleanValue();
    boolean serverSetSlot = false;
    private boolean needsend = false;
    public static boolean b ;


    private float getHitbox(){
        if (mc.thePlayer != null) {
            return mc.thePlayer.getCollisionBorderSize();
        }
        return 0;
    }
    @EventTarget
    public void onSlowdown(EventSlowdown e) {
        if (e.isSlowdown()) {

            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (mode.isCurrentMode("Grim")) {
                boolean isBedwarsValid = bedwars.getCurrentValue() && serverSetSlot && !HYTUtils.isEnchantedGoldenApple(heldItem);
                if (heldItem != null && heldItem.getItem() instanceof ItemFood && food.getCurrentValue() && heldItem.stackSize >= 2 && !HYTUtils.isEnchantedGoldenApple(heldItem) && serverSetSlot && !bedwars.getCurrentValue()) {
                    e.setSlowdown(false);
                } else if (heldItem != null && heldItem.getItem() instanceof ItemSword && sword.getCurrentValue()) {
                    e.setSlowdown(false);
                } else if (heldItem != null && heldItem.getItem() instanceof ItemBow && bow.getCurrentValue() && !mc.thePlayer.isSneaking() ) {
                    e.setSlowdown(false);
                } else if (heldItem != null && heldItem.getItem() instanceof ItemFood && food.getCurrentValue() && bedwars.getCurrentValue() && !HYTUtils.isEnchantedGoldenApple(heldItem) && serverSetSlot) {
                    e.setSlowdown(false);
                } else if (heldItem != null && heldItem.getItem() instanceof ItemPotion && isBedwarsValid) {
                    if (!ItemPotion.isSplash(heldItem.getMetadata())) {
                        e.setSlowdown(false);
                    }
                }
            }
            if (mode.isCurrentMode("Grim Lastest")) {
                if (mc.thePlayer.isEating()) {
                    if (mc.thePlayer.getItemInUseCount() >= 26) {
                        e.setSlowdown(true);
                        BlinkComponent.blinking = false;
                    } else {
                        e.setSlowdown(false);
                        BlinkComponent.blinking = true;
                    }
                } else {
                    if (mc.thePlayer.isEating()) {
                        BlinkComponent.dispatch();
                        BlinkComponent.blinking = true;
                    }
                    e.setSlowdown(false);
                }
            }
            if (mode.isCurrentMode("Vanilla")){
                e.setSlowdown(false);
            }
            }
    }

    @EventTarget
    public void onPre(EventMotion e) {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (mc.thePlayer == null) return;
        if (mode.isCurrentMode("Grim") || Aura.shouldNoslow) {
            if (e.getType() == EventType.PRE && mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemBow && bow.getCurrentValue() && !mc.thePlayer.isSneaking()) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new CPacketPlayerTryUseItem(1));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C17PacketCustomPayload("郭光洲", new PacketBuffer(Unpooled.wrappedBuffer(new byte[]{1}))));
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
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        Packet<?> packet = e.getPacket();

        if (mc.thePlayer == null) {
            return;
        }
        if (mode.isCurrentMode("Grim")) {
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
                    if (mc.thePlayer != null && currentPacket.getPlacedBlockDirection() == 255 && currentPacket.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a) && NoSlow.mc.thePlayer.getHeldItem() != null && (NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood)) {
                        if (bedwars.getCurrentValue()) {
                            mc.getNetHandler().getNetworkManager().sendPacket(new C01PacketChatMessage("/lizi open"));
                        }
                    }
                    if (heldItem.getItem() instanceof ItemPotion && isBedwarsValid) {
                        if (!ItemPotion.isSplash(heldItem.getMetadata())) {
                            mc.getNetHandler().getNetworkManager().sendPacket(new C01PacketChatMessage("/lizi open"));
                            ;
                        }
                    }
                }

                if (packet instanceof S30PacketWindowItems && mc.thePlayer.isUsingItem()) {
                    e.setCancelled(true);
                }

                if (bedwars.getCurrentValue() && NoSlow.mc.thePlayer.getHeldItem() != null && (NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && packet instanceof S2DPacketOpenWindow) {
                    e.setCancelled(true);
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C0DPacketCloseWindow(((S2DPacketOpenWindow) packet).getWindowId()));
                }
                if (bedwars.getCurrentValue() && NoSlow.mc.thePlayer.getHeldItem() != null && NoSlow.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && packet instanceof S2DPacketOpenWindow) {
                    e.setCancelled(true);
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C0DPacketCloseWindow(((S2DPacketOpenWindow) packet).getWindowId()));
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

}

