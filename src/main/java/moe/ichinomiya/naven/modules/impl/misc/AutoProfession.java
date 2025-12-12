package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import java.util.LinkedList;
import java.util.Queue;

@ModuleInfo(name = "AutoProfession", description = "Automatically changes your profession", category = Category.MISC)
public class AutoProfession extends Module {
    ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("击退者", "垂钓者", "开拓者", "弓箭手", "控制者", "燃烧者", "附魔师", "黄金战士").build().getModeValue();

    private final Queue<C0EPacketClickWindow> queue = new LinkedList<>();
    private final TimeHelper timer = new TimeHelper();
    boolean selected = false;
    int professionWindowId = 0;

    @EventTarget
    public void onRespawn(EventRespawn e) {
        timer.reset();
        selected = false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            while (!queue.isEmpty()) {
                mc.getNetHandler().getNetworkManager().sendPacket(queue.poll());
            }

            if (!selected) {
                int slot = -1;

                for (int i = 36; i < mc.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    if (stack != null && stack.getItem() instanceof ItemEnderEye && stack.getDisplayName().contains("职业选择")) {
                        slot = i - 36;
                        break;
                    }
                }

                if (slot != -1 && timer.delay(1000, true)) {
                    if (mc.thePlayer.inventory.currentItem != slot) {
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(slot));
                    }

                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));

                    if (mc.thePlayer.inventory.currentItem != slot) {
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    }

                    selected = true;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S2DPacketOpenWindow) {
            S2DPacketOpenWindow packet = (S2DPacketOpenWindow) e.getPacket();
            String title = packet.getWindowTitle().getUnformattedText();
            if (title.contains("选择你的职业")) {
                professionWindowId = packet.getWindowId();
                e.setCancelled(true);
            }
        }

        if (e.getPacket() instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot packet = (S2FPacketSetSlot) e.getPacket();

            if (packet.getWindowId() == professionWindowId && packet.getSlot() < 10 && packet.getItemStack() != null) {
                String displayName = packet.getItemStack().getDisplayName();
                if (displayName.contains(mode.getCurrentMode())) {
                    queue.offer(new C0EPacketClickWindow(professionWindowId, packet.getSlot(), 0, 0, packet.getItemStack(), (short) 0));
                }
            }
        }
    }
}
