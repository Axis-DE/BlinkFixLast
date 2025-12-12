package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.events.impl.*;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.move.NoSlow;
import moe.ichinomiya.naven.ui.clickgui.ClientClickGUI;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "OffhandFeatures", description = "Allows you to use your offhand", category = Category.MISC)
public class OffhandFeatures extends Module {
    public ItemStack stack = null;
    public int useItemTicks = 0;

    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClientClickGUI) {
            super.toggle();
        }
    }

    @EventTarget
    public void onKey(EventKey e) {
        if (e.getKey() == getKey() && e.isState()) {
            mc.getNetHandler().getNetworkManager().sendPacket(new CPacketSwapItemWithOffHand());
            useItemTicks = 0;
        }
    }

    @EventTarget
    public void onSlowdown(EventSlowdown e) {
        Module module = BlinkFix.getInstance().getModuleManager().getModule(NoSlow.class);

        if (stack != null && stack.getItem() instanceof ItemAppleGold && !e.isSlowdown() && useItemTicks > 0) {
            if (!module.isEnabled()) {
                e.setSlowdown(true);
            }
            if (useItemTicks >= 30 || useItemTicks <= 2) {
                e.setSlowdown(true);
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        if (stack != null) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            mc.getRenderItem().zLevel = -150.0F;

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();

            ScaledResolution sr = e.getResolution();
            mc.getRenderItem().renderItemIntoGUI(stack, sr.getScaledWidth() / 2f - 120, sr.getScaledHeight() - 19);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, sr.getScaledWidth() / 2f - 120, sr.getScaledHeight() - 19);
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.enableAlpha();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot packet = (S2FPacketSetSlot) e.getPacket();
            if (packet.getWindowId() == 0 && packet.getSlot() == 45) {
                stack = packet.getItemStack();
            }
        } else if (e.getPacket() instanceof S30PacketWindowItems) {
            S30PacketWindowItems packet = (S30PacketWindowItems) e.getPacket();
            if (packet.getWindowId() == 0) {
                stack = packet.getItemStacks()[45];
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (stack != null && stack.getItem() instanceof ItemAppleGold) {
                ItemStack heldItem = mc.thePlayer.getHeldItem();
                if (Mouse.isButtonDown(mc.gameSettings.keyBindUseItem.getKeyCode() + 100) && heldItem != null && (heldItem.getItem() instanceof ItemSword || heldItem.getItem() instanceof ItemTool)) {
                    if (useItemTicks-- <= 0) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new CPacketPlayerTryUseItem(1));
                        useItemTicks = 32;
                    }
                } else if (useItemTicks > 0) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    useItemTicks = 0;
                }

                Module module = BlinkFix.getInstance().getModuleManager().getModule(NoSlow.class);
                if (module.isEnabled() && useItemTicks > 0) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C0EPacketClickWindow(0, 36, 0, 2, new ItemStack(Blocks.bedrock), (short) 0));
                }
            }
        }
    }
}
