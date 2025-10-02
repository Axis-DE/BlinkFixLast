package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.RenderUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.StencilUtils;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.ModeValue;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
@ModuleInfo(name = "ContainerStealer", description = "Steals containers!", category = Category.MISC)
public class ContainerStealer extends Module {
    private static final ConcurrentHashMap<Integer, ItemStack> items = new ConcurrentHashMap<>();
    private static final TimeHelper timer = new TimeHelper();
    private static final TimeHelper stoleItems = new TimeHelper();
    private static final ConcurrentLinkedQueue<C0EPacketClickWindow> clickPackets = new ConcurrentLinkedQueue<>();
    private final static int headerColor = new Color(150, 45, 45, 255).getRGB();
    private final static int bodyColor = new Color(0, 0, 0, 190).getRGB();
    private static int windowId = -999;
    private static boolean resolved = true;

    ModeValue mode = ValueBuilder.create(this, "Mode").setModes("Packet", "GUI").setDefaultModeIndex(0).build().getModeValue();

    BooleanValue chest = ValueBuilder.create(this, "Chest").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue furnace = ValueBuilder.create(this, "Furnace").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue brewingStand = ValueBuilder.create(this, "Brewing Stand").setDefaultBooleanValue(true).build().getBooleanValue();

    FloatValue delay = ValueBuilder.create(this, "Delay").setMinFloatValue(0).setMaxFloatValue(1000).setFloatStep(50).setDefaultFloatValue(0).setVisibility(() -> mode.isCurrentMode("GUI")).build().getFloatValue();

    int slots;
    short actionNumber = 0;
    int stoleTimes = 0;
    ScaledResolution resolution;
    BlockPos chestPosition;
    double[] offset;

    public static boolean isStealing() {
        return BlinkFix.getInstance().getModuleManager().getModule(ContainerStealer.class).isEnabled() && windowId != -999;
    }

    @Override
    public boolean onEnable() {
        windowId = -999;
        items.clear();
        clickPackets.clear();
        return false;
    }

    @Override
    public void onDisable() {
        windowId = -999;
        items.clear();
        clickPackets.clear();
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        windowId = -999;
        items.clear();
        clickPackets.clear();
    }

    @EventTarget
    public void onRender(EventShader e) {
        if (windowId != -999 && resolution != null && offset != null) {
            float left = (float) offset[0] / resolution.getScaleFactor() - (170 / 2f);
            float top = (float) offset[1] / resolution.getScaleFactor() - 90;

            RenderUtils.drawBoundRoundedRect(left, top, 170, 62, 5f, 0xFFFFFFFF);
        }
    }

    @EventTarget
    public void onRender(EventRender e) {
        offset = null;
        if (chestPosition != null) {
            double renderX = chestPosition.getX() + 0.5f - mc.getRenderManager().renderPosX;
            double renderY = chestPosition.getY() + 0.5f - mc.getRenderManager().renderPosY;
            double renderZ = chestPosition.getZ() + 0.5f - mc.getRenderManager().renderPosZ;

            double[] convertTo2D = RenderUtils.convertTo2D(renderX, renderY, renderZ);
            if (convertTo2D != null) {
                if ((convertTo2D[2] >= 0.0D) && (convertTo2D[2] < 1.0D)) {
                    offset = new double[]{convertTo2D[0], convertTo2D[1], Math.abs(RenderUtils.convertTo2D(renderX, renderY + 1.0D, renderZ, chestPosition)[1] - RenderUtils.convertTo2D(renderX, renderY, renderZ, chestPosition)[1]), convertTo2D[2]};
                }
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        resolution = e.getResolution();

        if (offset != null) {
            GlStateManager.pushMatrix();
            float left = (float) offset[0] / resolution.getScaleFactor() - (170 / 2f);
            float top = (float) offset[1] / resolution.getScaleFactor() - 90;

            int count = 0;
            int x = 0, y = 0;

            if (windowId != -999) {
                StencilUtils.write(false);
                RenderUtils.drawBoundRoundedRect(left, top, 170, 62, 5f, 0xFFFFFFFF);
                StencilUtils.erase(true);
                RenderUtils.drawRectBound(left, top, 170, 3, headerColor);
                RenderUtils.drawRectBound(left, top + 3, 170, 59, bodyColor);
                StencilUtils.dispose();

                for (int slot = 0; slot < slots; slot++) {
                    if (items.containsKey(slot)) {
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

                        ItemStack item = items.get(slot);
                        int x1 = (int) (left + x + 5);
                        int y1 = (int) (top + 5 + y);
                        mc.getRenderItem().renderItemIntoGUI(item, x1, y1);
                        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, item, x1, y1);
                        mc.getRenderItem().zLevel = 0.0F;
                        GlStateManager.enableAlpha();
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.popMatrix();
                    }

                    x += 18;
                    if (++count >= 9) {
                        x = 0;
                        y += 18;
                        count = 0;
                    }
                }
            }
            GlStateManager.popMatrix();
        }
    }


    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            InventoryManager manager = (InventoryManager) BlinkFix.getInstance().getModuleManager().getModule(InventoryManager.class);

            if (mode.isCurrentMode("GUI")) {
                if (mc.thePlayer.openContainer != null) {
                    if (mc.thePlayer.openContainer instanceof ContainerChest) {
                        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                        String displayName = chest.getLowerChestInventory().getDisplayName().getUnformattedText();

                        if (!displayName.equals(StatCollector.translateToLocal("container.chest")) && !displayName.equals(StatCollector.translateToLocal("container.chestDouble")) && !displayName.contains("Chest")) {
                            return;
                        }

                        if (isChestEmpty(chest)) {
                            mc.thePlayer.closeScreen();
                        }

                        for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); ++i) {
                            ItemStack stack = chest.getLowerChestInventory().getStackInSlot(i);
                            if (stack != null && timer.delay(delay.getCurrentValue()) && (!manager.shouldDrop(stack) || stack.getItem() instanceof ItemArmor)) {
                                mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                                timer.reset();
                            }
                        }
                    } else if (mc.thePlayer.openContainer instanceof ContainerFurnace) {
                        ContainerFurnace container = (ContainerFurnace) mc.thePlayer.openContainer;
                        if (isFurnaceEmpty(container)) {
                            mc.thePlayer.closeScreen();
                        }

                        for (int i = 0; i < container.tileFurnace.getSizeInventory(); ++i) {
                            ItemStack stack = container.tileFurnace.getStackInSlot(i);
                            if (stack != null && timer.delay(delay.getCurrentValue()) && (!manager.shouldDrop(stack) || stack.getItem() instanceof ItemArmor)) {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                                timer.reset();
                            }
                        }
                    } else if (mc.thePlayer.openContainer instanceof ContainerBrewingStand) {
                        ContainerBrewingStand container = (ContainerBrewingStand) mc.thePlayer.openContainer;
                        if (isBrewingStandEmpty(container)) {
                            mc.thePlayer.closeScreen();
                        }

                        for (int i = 0; i < container.tileBrewingStand.getSizeInventory(); ++i) {
                            ItemStack stack = container.tileBrewingStand.getStackInSlot(i);
                            if (stack != null && timer.delay(delay.getCurrentValue()) && (!manager.shouldDrop(stack) || stack.getItem() instanceof ItemArmor)) {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                                timer.reset();
                            }
                        }
                    }
                }

                return;
            }

            if (!resolved && timer.delay(100)) {
                resolved = true;
                ArrayList<Map.Entry<Integer, ItemStack>> entries = new ArrayList<>(items.entrySet());
                Collections.shuffle(entries);

                for (Map.Entry<Integer, ItemStack> entry : entries) {
                    Item item = entry.getValue().getItem();
                    if (item instanceof ItemArmor) {
                        float chestItem = AutoArmor.getProtection(entry.getValue());
                        float bodyItem;

                        if (entry.getValue().getUnlocalizedName().contains("helmet")) {
                            bodyItem = AutoArmor.getProtection(mc.thePlayer.getSlotFromPlayerContainer(5).getStack());
                        } else if (entry.getValue().getUnlocalizedName().contains("chestplate")) {
                            bodyItem = AutoArmor.getProtection(mc.thePlayer.getSlotFromPlayerContainer(6).getStack());
                        } else if (entry.getValue().getUnlocalizedName().contains("leggings")) {
                            bodyItem = AutoArmor.getProtection(mc.thePlayer.getSlotFromPlayerContainer(7).getStack());
                        } else if (entry.getValue().getUnlocalizedName().contains("boots")) {
                            bodyItem = AutoArmor.getProtection(mc.thePlayer.getSlotFromPlayerContainer(8).getStack());
                        } else {
                            bodyItem = 0;
                        }

                        if (chestItem < bodyItem) {
                            continue;
                        }
                    }

                    if (!manager.shouldDrop(entry.getValue()) || item instanceof ItemArmor) {
                        C0EPacketClickWindow packet = new C0EPacketClickWindow(windowId, entry.getKey(), 0, 1, entry.getValue(), ++actionNumber);
                        clickPackets.add(packet);
                    }
                }
            }

            if (resolved && timer.delay(100)) {
                while (!clickPackets.isEmpty()) {
                    C0EPacketClickWindow poll = clickPackets.poll();
                    mc.getNetHandler().getNetworkManager().sendPacket(poll);
                    items.remove(poll.getSlotId());
                    stoleItems.reset();
                }
            }

            if (resolved && timer.delay(100) && stoleItems.delay(150)) {
                if (clickPackets.isEmpty()) {
                    if (windowId != -999) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow(windowId));
                        windowId = -998;
                    }
                }
            }
        } else {
            if (windowId == -998) {
                windowId = -999;
            }
        }
    }

    private boolean isChestEmpty(ContainerChest c) {
        for (int i = 0; i < c.getLowerChestInventory().getSizeInventory(); ++i) {
            ItemStack stack = c.getLowerChestInventory().getStackInSlot(i);
            if (stack != null) {
                InventoryManager manager = (InventoryManager) BlinkFix.getInstance().getModuleManager().getModule(InventoryManager.class);
                if (!manager.shouldDrop(stack) || stack.getItem() instanceof ItemArmor) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isFurnaceEmpty(ContainerFurnace c) {
        for (int i = 0; i < c.tileFurnace.getSizeInventory(); ++i) {
            ItemStack stack = c.tileFurnace.getStackInSlot(i);
            if (stack != null) {
                InventoryManager manager = (InventoryManager) BlinkFix.getInstance().getModuleManager().getModule(InventoryManager.class);
                if (!manager.shouldDrop(stack) || stack.getItem() instanceof ItemArmor) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isBrewingStandEmpty(ContainerBrewingStand c) {
        for (int i = 0; i < c.tileBrewingStand.getSizeInventory(); ++i) {
            ItemStack stack = c.tileBrewingStand.getStackInSlot(i);
            if (stack != null) {
                InventoryManager manager = (InventoryManager) BlinkFix.getInstance().getModuleManager().getModule(InventoryManager.class);
                if (!manager.shouldDrop(stack) || stack.getItem() instanceof ItemArmor) {
                    return false;
                }
            }
        }

        return true;
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getType() == EventType.RECEIVE && mode.isCurrentMode("Packet")) {
            if (e.getPacket() instanceof S2DPacketOpenWindow) {
                S2DPacketOpenWindow packet = (S2DPacketOpenWindow) e.getPacket();
                String title = packet.getWindowTitle().getUnformattedText();

                boolean isChest = packet.getGuiId().equals("minecraft:chest") && title.equals(StatCollector.translateToLocal("container.chest")) ||
                        title.equals(StatCollector.translateToLocal("container.chestDouble")) || title.contains("Chest");
                boolean isFurnace = packet.getGuiId().equals("minecraft:furnace");
                boolean isBrewingStand = packet.getGuiId().equals("minecraft:brewing_stand");

                if ((isChest && chest.getCurrentValue()) || (isFurnace && furnace.getCurrentValue()) || (isBrewingStand && brewingStand.getCurrentValue())) {
                    windowId = packet.getWindowId();
                    timer.reset();
                    resolved = false;
                    items.clear();
                    slots = packet.getSlotCount() == 164 ? 5 : packet.getSlotCount();
                    actionNumber = 0;
                    stoleTimes = 0;

                    e.setCancelled(true);
                }
            }

            if (e.getPacket() instanceof S2FPacketSetSlot) {
                S2FPacketSetSlot packet = (S2FPacketSetSlot) e.getPacket();
                if (packet.getWindowId() == windowId && packet.getItemStack() != null && !timer.delay(100)) {
                    if (packet.getSlot() < slots) {
                        items.put(packet.getSlot(), packet.getItemStack());
                    }
                    e.setCancelled(true);
                }
            }

            if (e.getPacket() instanceof S30PacketWindowItems) {
                S30PacketWindowItems packet = (S30PacketWindowItems) e.getPacket();

                if (packet.getWindowId() == windowId && !timer.delay(100)) {
                    ItemStack[] stacks = packet.getItemStacks();

                    for (int i = 0; i < stacks.length; i++) {
                        ItemStack stack = stacks[i];

                        if (stack != null && i < slots) {
                            items.put(i, stack);
                        }
                    }
                    e.setCancelled(true);
                }
            }
        } else {
            if (e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) e.getPacket();
                if (!packet.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a) && windowId == -999) {
                    Block block = mc.theWorld.getBlockState(packet.getPosition()).getBlock();
                    if (block == Blocks.chest || block == Blocks.trapped_chest || block == Blocks.furnace || block == Blocks.brewing_stand) {
                        chestPosition = packet.getPosition();
                    }
                }
            }
        }
    }
}
