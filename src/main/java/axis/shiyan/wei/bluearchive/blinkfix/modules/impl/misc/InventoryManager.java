package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat.AutoGapple;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Blink;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.HYTUtils;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Scaffold;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.utils.ViaUtils;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.OBFUSCATED;

@ModuleInfo(name = "InventoryManager", description = "Automatically manage your items", category = Category.MISC)
public class InventoryManager extends Module {
    public static List<Block> blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava,
            Blocks.flowing_lava, Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane,
            Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
            Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.chest, Blocks.trapped_chest,
            Blocks.torch, Blocks.anvil, Blocks.trapped_chest, Blocks.noteblock, Blocks.jukebox, Blocks.tnt,
            Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore,
            Blocks.redstone_ore, Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate,
            Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_button,
            Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook,
            Blocks.rail, Blocks.waterlily, Blocks.red_flower, Blocks.red_mushroom, Blocks.brown_mushroom,
            Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand,
            Blocks.cactus, Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.web,
            Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall, Blocks.oak_fence, Blocks.redstone_torch);

    static TimeHelper timer = new TimeHelper(), swapTimer = new TimeHelper();
    BooleanValue drop = ValueBuilder.create(this, "Auto Drop").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue delay = ValueBuilder.create(this, "Delay").setDefaultFloatValue(100).setMinFloatValue(0).setMaxFloatValue(1000).setFloatStep(100).build().getFloatValue();
    BooleanValue openinv = ValueBuilder.create(this, "Open Inv").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue keepRods = ValueBuilder.create(this, "Keep Rods").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue keepBuckets = ValueBuilder.create(this, "Keep Buckets").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue keepOres = ValueBuilder.create(this, "Keep Ores").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue keepSticks = ValueBuilder.create(this, "Keep Sticks").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue keepCompass = ValueBuilder.create(this, "Keep Compass").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue keepBadPotion = ValueBuilder.create(this, "Keep Negative Potion").setDefaultBooleanValue(false).build().getBooleanValue();

    BooleanValue keepSword = ValueBuilder.create(this, "Keep Sword").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue swordSlot = ValueBuilder.create(this, "Sword Slot").setDefaultFloatValue(1).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepSword.getCurrentValue()).build().getFloatValue();

    BooleanValue keepPickaxe = ValueBuilder.create(this, "Keep Pickaxe").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue pickaxeSlot = ValueBuilder.create(this, "Pickaxe Slot").setDefaultFloatValue(2).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepPickaxe.getCurrentValue()).build().getFloatValue();

    BooleanValue keepAxe = ValueBuilder.create(this, "Keep Axe").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue axeSlot = ValueBuilder.create(this, "Axe Slot").setDefaultFloatValue(3).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepAxe.getCurrentValue()).build().getFloatValue();

    BooleanValue keepShovel = ValueBuilder.create(this, "Keep Shovel").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue shovelSlot = ValueBuilder.create(this, "Shovel Slot").setDefaultFloatValue(4).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepShovel.getCurrentValue()).build().getFloatValue();

    BooleanValue keepBow = ValueBuilder.create(this, "Keep Bow").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue bowSlot = ValueBuilder.create(this, "Bow Slot").setDefaultFloatValue(5).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepBow.getCurrentValue()).build().getFloatValue();
    FloatValue keepArrows = ValueBuilder.create(this, "Keep Arrows").setDefaultFloatValue(128).setMinFloatValue(0).setMaxFloatValue(512).setFloatStep(64).setVisibility(() -> keepBow.getCurrentValue()).build().getFloatValue();

    BooleanValue keepHead = ValueBuilder.create(this, "Keep Head").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue headSlot = ValueBuilder.create(this, "Head Slot").setDefaultFloatValue(6).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepHead.getCurrentValue()).build().getFloatValue();

    BooleanValue keepGApple = ValueBuilder.create(this, "Keep GApple").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue gAppleSlot = ValueBuilder.create(this, "GApple Slot").setDefaultFloatValue(7).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepGApple.getCurrentValue()).build().getFloatValue();

    FloatValue keepBlocks = ValueBuilder.create(this, "Keep Blocks").setDefaultFloatValue(256).setMinFloatValue(64).setMaxFloatValue(512).setFloatStep(64).build().getFloatValue();
    BooleanValue swapBlock = ValueBuilder.create(this, "Swap Blocks").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue blockSlot = ValueBuilder.create(this, "Block Slot").setDefaultFloatValue(8).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> swapBlock.getCurrentValue()).build().getFloatValue();

    BooleanValue keepThrowable = ValueBuilder.create(this, "Keep Throwable").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue throwableSlot = ValueBuilder.create(this, "Throwable Slot").setDefaultFloatValue(9).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepThrowable.getCurrentValue()).build().getFloatValue();

    BooleanValue keepEnderPearl = ValueBuilder.create(this, "Swap Ender Pearl").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue enderPearlSlot = ValueBuilder.create(this, "Ender Pearl Slot").setDefaultFloatValue(9).setMinFloatValue(1).setMaxFloatValue(9).setFloatStep(1).setVisibility(() -> keepEnderPearl.getCurrentValue()).build().getFloatValue();

    private static boolean containKBBall() {
        for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
            ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(slotIndex).getStack();

            if (HYTUtils.isKBBall(stack)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containGodAxe() {
        for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
            ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(slotIndex).getStack();

            if (HYTUtils.isGodAxe(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldSwapSword(ItemStack stack) {
        PreferWeapon module = (PreferWeapon) BlinkFix.getInstance().getModuleManager().getModule(PreferWeapon.class);

        if (module.isEnabled()) {
            if (module.weapon.isCurrentMode("KB Ball") && containKBBall()) {
                return  HYTUtils.isKBBall(stack);
            }

            if (module.weapon.isCurrentMode("God Axe") && containGodAxe()) {
                return HYTUtils.isGodAxe(stack);
            }
        }

        return isBestSword(stack);
    }

    public static boolean isBestSword(ItemStack stack) {
        final float damage = getDamage(stack);

        if (stack.getDisplayName().contains("时装管理")) {
            return false;
        }

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack is = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (getDamage(is) > damage && is.getItem() instanceof ItemSword) {
                    return false;
                }
            }
        }

        return stack.getItem() instanceof ItemSword;
    }

    private static float getDamage(ItemStack stack) {
        float damage = 0;
        final Item item = stack.getItem();

        if (item instanceof ItemTool) {
            damage += ((ItemTool) item).getDamage();
        } else if (item instanceof ItemSword) {
            damage += ((ItemSword) item).getAttackDamage();
        }

        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25F + EnchantmentHelper.getEnchantmentLevel(
                Enchantment.fireAspect.effectId, stack) * 0.01F;
        return damage;
    }

    public static int getPickaxeCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() instanceof ItemPickaxe && isBestPickaxe(stack)) count += stack.stackSize;
            }
        }

        return count;
    }

    public static boolean isBestPickaxe(ItemStack stack) {
        final Item item = stack.getItem();

        if (!(item instanceof ItemPickaxe)) {
            return false;
        }

        final float value = getToolEffect(stack);

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack slotStack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (getToolEffect(slotStack) > value && slotStack.getItem() instanceof ItemPickaxe) return false;
            }
        }

        return true;
    }

    public static boolean isBestShovel(ItemStack stack) {
        final Item item = stack.getItem();

        if (!(item instanceof ItemSpade)) {
            return false;
        }

        final float value = getToolEffect(stack);

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack is = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemSpade) return false;
            }
        }

        return true;
    }

    private static float getToolEffect(ItemStack stack) {
        final Item item = stack.getItem();

        if (!(item instanceof ItemTool)) {
            return 0;
        }

        final String name = item.getUnlocalizedName();
        final ItemTool tool = (ItemTool) item;
        float value;

        if (item instanceof ItemPickaxe) {
            value = tool.getStrVsBlock(stack, Blocks.stone);
            if (name.toLowerCase().contains("gold")) value -= 5;
        } else if (item instanceof ItemSpade) {
            value = tool.getStrVsBlock(stack, Blocks.dirt);
            if (name.toLowerCase().contains("gold")) value -= 5;
        } else if (item instanceof ItemAxe) {
            value = tool.getStrVsBlock(stack, Blocks.log);
            value += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 10;
            if (name.toLowerCase().contains("gold")) value -= 5;
        } else return 1f;

        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 0.0075D;
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 100.0D;

        return value;
    }

    private static float getBowEffect(ItemStack stack) {
        float effect = (1 + EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack) + EnchantmentHelper.getEnchantmentLevel(
                Enchantment.flame.effectId, stack) + EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) + EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack) * 5);

        if (ViaUtils.isCrossbow(stack)) {
            effect = 100;
        }

        return effect;
    }

    public static boolean isBestBow(ItemStack stack) {
        final Item item = stack.getItem();
        if (!(item instanceof ItemBow)) return false;

        final float value = getBowEffect(stack);

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack slotStack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (getBowEffect(slotStack) > value && slotStack.getItem() instanceof ItemBow) return false;
            }
        }

        return true;
    }

    @EventTarget(Priority.HIGH)
    public void onPreUpdate(EventMotion event) {
        if (event.getType() == EventType.PRE) {
            if (mc.thePlayer.ticksExisted < 20) {
                return;
            }

            if (HYTUtils.isInLobby()) {
                return;
            }

            if (ContainerStealer.isStealing()) {
                return;
            }

            if (AutoArmor.isWorking()) {
                return;
            }

            //if (BlinkFix.getInstance().getModuleManager().getModule(Blink.class).isEnabled()) {
            //   return;
            // }
            if (BlinkFix.getInstance().getModuleManager().getModule(AutoGapple.class).isEnabled()) return;

            if (mc.thePlayer.isUsingItem()) {
                return;
            }
            if (openinv.getCurrentValue() && !(mc.currentScreen instanceof GuiInventory) ){
                return;
            }

            ItemStack lastItem = mc.thePlayer.inventoryContainer.getSlot(44).getStack();
            if (lastItem != null && lastItem.getItem() instanceof ItemBed) {
                return;
            }

            if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) {
                final int swordSlot = (int) (this.swordSlot.getCurrentValue() - 1), pickAxeSlot = (int) (this.pickaxeSlot.getCurrentValue() - 1),
                        bowSlot = (int) (this.bowSlot.getCurrentValue() - 1), shovelSlot = (int) (this.shovelSlot.getCurrentValue() - 1),
                        axeSlot = (int) (this.axeSlot.getCurrentValue() - 1), headSlot = (int) (this.headSlot.getCurrentValue() - 1), gappleSlot = (int) (this.gAppleSlot.getCurrentValue() - 1),
                        blockSlot = (int) this.blockSlot.getCurrentValue() - 1, enderPearlSlot = (int) this.enderPearlSlot.getCurrentValue() - 1, throwableSlot = (int) this.throwableSlot.getCurrentValue() - 1;

                boolean pickAxe = keepPickaxe.getCurrentValue(), shovel = keepShovel.getCurrentValue(),
                        axe = keepAxe.getCurrentValue(), sword = keepSword.getCurrentValue(),
                        bow = keepBow.getCurrentValue(), head = keepHead.getCurrentValue(),
                        gapple = keepGApple.getCurrentValue(), enderPearl = keepEnderPearl.getCurrentValue(), throwable = keepThrowable.getCurrentValue();

                int tickDelay = (int) delay.getCurrentValue();

                for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
                    ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(slotIndex).getStack();
                    if (stack != null) {
                        if (!BlinkFix.getInstance().getModuleManager().getModule(Blink.class).isEnabled()) {
                            if (swapTimer.check(tickDelay)) {
                                if (shouldSwapSword(stack) && sword && shouldSwap(swordSlot)[0]) {
                                    mc.thePlayer.swap(slotIndex, swordSlot);
                                    swapTimer.reset();
                                } else if (isBestPickaxe(stack) && pickAxe && shouldSwap(pickAxeSlot)[2]) {
                                    mc.thePlayer.swap(slotIndex, pickAxeSlot);
                                    swapTimer.reset();
                                } else if (isBestAxe(stack) && axe && shouldSwap(axeSlot)[1]) {
                                    mc.thePlayer.swap(slotIndex, axeSlot);
                                    swapTimer.reset();
                                } else if (isBestBow(stack) && bow && shouldSwap(bowSlot)[5] && !stack.getDisplayName().toLowerCase().contains("kit selector")) {
                                    mc.thePlayer.swap(slotIndex, bowSlot);
                                    swapTimer.reset();
                                } else if (isHead(stack) && head && shouldSwap(headSlot)[4]) {
                                    mc.thePlayer.swap(slotIndex, headSlot);
                                    swapTimer.reset();
                                } else if (isBestShovel(stack) && shovel && shouldSwap(shovelSlot)[3]) {
                                    mc.thePlayer.swap(slotIndex, shovelSlot);
                                    swapTimer.reset();
                                } else if (isGoldenApple(stack) && gapple && shouldSwap(gappleSlot)[6]) {
                                    mc.thePlayer.swap(slotIndex, gappleSlot);
                                    swapTimer.reset();
                                } else if (isEnderPearl(stack) && enderPearl && shouldSwap(enderPearlSlot)[7]) {
                                    mc.thePlayer.swap(slotIndex, enderPearlSlot);
                                    swapTimer.reset();
                                } else if (isThrowable(stack) && throwable && shouldSwap(throwableSlot)[8]) {
                                    mc.thePlayer.swap(slotIndex, throwableSlot);
                                    swapTimer.reset();
                                }
                            }
                        }

                        if (swapBlock.getCurrentValue()) {
                            Slot inventorySlotBlock = mc.thePlayer.getSlotFromPlayerContainer(blockSlot + 36);
                            if (!inventorySlotBlock.getHasStack() || !Scaffold.isValidStack(inventorySlotBlock.getStack()) || inventorySlotBlock.getStack().stackSize < 1) {
                                if (Scaffold.isValidStack(stack)) {
                                    mc.thePlayer.swap(slotIndex, blockSlot);
                                    swapTimer.reset();
                                }
                            }
                        }
                    }
                }

                for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
                    if (!mc.thePlayer.getSlotFromPlayerContainer(slotIndex).getHasStack()) {
                        continue;
                    }

                    ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(slotIndex).getStack();

                    if (stack != null) {
                        if (shouldDrop(stack) && drop.getCurrentValue()) {
                            if (timer.delay(tickDelay)) {
                                if (mc.thePlayer.inventory.getItemStack() != null) {
                                    mc.playerController.windowClick(0, -999, 0, 0, mc.thePlayer);
                                    mc.thePlayer.inventory.setItemStack(null);
                                    return;
                                }

                                mc.thePlayer.drop(slotIndex);
                                timer.reset();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isThrowable(ItemStack stack) {
        return stack.getItem() instanceof ItemSnowball || stack.getItem() instanceof ItemEgg;
    }

    private boolean isHead(ItemStack stack) {
        return stack.getItem() instanceof ItemSkull && (stack.getDisplayName().contains("Head") || stack.getDisplayName().contains("金头"));
    }

    private boolean isGoldenApple(ItemStack stack) {
        return stack.getItem() instanceof ItemAppleGold;
    }

    private boolean[] shouldSwap(int slot) {
        return new boolean[]{
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !shouldSwapSword(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isBestAxe(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isBestPickaxe(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isBestShovel(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isHead(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isBestBow(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isGoldenApple(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isEnderPearl(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack()),
                !mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getHasStack() || !isThrowable(mc.thePlayer.getSlotFromPlayerContainer(slot + 36).getStack())
        };
    }

    private boolean isEnderPearl(ItemStack stack) {
        return stack.getItem() instanceof ItemEnderPearl;
    }

    private int getBlocksCounter() {
        int blockCount = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                final Item item = stack.getItem();

                if (stack.getItem() instanceof ItemBlock && !blacklistedBlocks.contains(((ItemBlock) item).getBlock())) {
                    blockCount += stack.stackSize;
                }
            }
        }

        return blockCount;
    }

    private int getArrowsCounter() {
        int arrowCount = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack is = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (is.getItem() == Items.arrow) arrowCount += is.stackSize;
            }
        }

        return arrowCount;
    }

    private int getIronIngotsCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() == Items.iron_ingot) count += stack.stackSize;
            }
        }

        return count;
    }

    private int getCoalCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() == Items.coal) count += stack.stackSize;
            }
        }

        return count;
    }

    private int getSwordsCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() instanceof ItemSword && isBestSword(stack)) count += stack.stackSize;
            }
        }

        return count;
    }

    private int getBowsCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() instanceof ItemBow && isBestBow(stack)) count += stack.stackSize;
            }
        }

        return count;
    }

    private int getAxesCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() instanceof ItemAxe && isBestAxe(stack)) count += stack.stackSize;
            }
        }

        return count;
    }

    private int getHeadsCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() instanceof ItemSkull && isBestShovel(stack)) count += stack.stackSize;
            }
        }

        return count;
    }

    private int getShovelsCounter() {
        int count = 0;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack stack = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (stack.getItem() instanceof ItemSpade && isBestShovel(stack)) count += stack.stackSize;
            }
        }

        return count;
    }

    public boolean shouldDrop(ItemStack stack) {
        final Item item = stack.getItem();
        final String displayName = stack.getDisplayName();
        final int idFromItem = Item.getIdFromItem(item);

        if (HYTUtils.isGodAxe(stack) || HYTUtils.isKBBall(stack) || HYTUtils.isFireEnchanted(stack)) {
            return false;
        }

        if (idFromItem == 58 || displayName.toLowerCase().contains(OBFUSCATED + "||") // @off
                || (stack.getTagCompound() != null && stack.getTagCompound().toString().contains("点击传送至"))
                || displayName.contains("职业选择")
                || displayName.contains("金头")
                || displayName.contains("商店")
                || displayName.contains("菜单")
                || displayName.contains("战绩")
                || displayName.contains("玩法选择")
                || displayName.contains("商城大使")
                || displayName.contains("技能")
                || displayName.contains("隐藏玩家")
                || displayName.contains("道具")
                || displayName.contains("活动与事件")
                || displayName.contains("解锁装备")
                || displayName.contains("点击使用")
                || displayName.contains("离开")
                || displayName.contains("选择游戏")
                || displayName.contains("时装管理")
                || displayName.contains("个人空间")
                || displayName.contains("游戏指南")
                || displayName.contains("道具和特效管理")
                || displayName.contains("退出观战")
                || displayName.contains("再来一局")
                || (item == Items.fishing_rod && keepRods.getCurrentValue())
                || idFromItem == 116 || idFromItem == 145
                || (idFromItem == 15 || idFromItem == 14) && keepOres.getCurrentValue()
                || idFromItem == 259 || idFromItem == 46) { // @on
            return false;
        }

        if (keepBuckets.getCurrentValue()) {
            if (item == Items.bucket || item == Items.lava_bucket || item == Items.water_bucket) {
                return false;
            }
        }

        if (item == Items.sign) {
            return false;
        }

        // 保留梯子
        if (idFromItem == 65) {
            return false;
        }

        // 保留不死图腾
        if (ViaUtils.isTotemOfUndying(stack)) {
            return false;
        }

        // 保留末影水晶
        if (ViaUtils.isEndCrystal(stack)) {
            return false;
        }

        // 保留指南针
        if (keepCompass.getCurrentValue() && item == Items.compass) {
            return false;
        }

        // 保留雪球和鸡蛋
        if (keepThrowable.getCurrentValue() && (item == Items.egg || item == Items.snowball)) {
            return false;
        }

        boolean pickAxe = keepPickaxe.getCurrentValue(), shovel = keepShovel.getCurrentValue(),
                axe = keepAxe.getCurrentValue(), sword = keepSword.getCurrentValue(),
                bow = keepBow.getCurrentValue(), head = keepHead.getCurrentValue();

        final int swordSlot = (int) (this.swordSlot.getCurrentValue() - 1),
                pickAxeSlot = (int) (this.pickaxeSlot.getCurrentValue() - 1),
                bowSlot = (int) (this.bowSlot.getCurrentValue() - 1),
                shovelSlot = (int) (this.shovelSlot.getCurrentValue() - 1),
                axeSlot = (int) (this.axeSlot.getCurrentValue() - 1),
                headSlot = (int) (this.headSlot.getCurrentValue() - 1);

        if ((isBestShovel(stack) && getShovelsCounter() < 2 || stack.getItem() instanceof ItemSpade && stack == mc.thePlayer.inventory.getStackInSlot(shovelSlot)) && shovel ||
                (isBestBow(stack) && getBowsCounter() < 2 || stack.getItem() instanceof ItemBow && stack == mc.thePlayer.inventory.getStackInSlot(bowSlot)) && bow ||
                (isHead(stack) && getHeadsCounter() < 2 || stack.getItem() instanceof ItemSkull && stack == mc.thePlayer.inventory.getStackInSlot(headSlot)) && head ||
                (isBestAxe(stack) && getAxesCounter() < 2 || stack.getItem() instanceof ItemAxe && stack == mc.thePlayer.inventory.getStackInSlot(axeSlot)) && axe ||
                (isBestPickaxe(stack) && getPickaxeCounter() < 2 || stack.getItem() instanceof ItemPickaxe && stack == mc.thePlayer.inventory.getStackInSlot(pickAxeSlot)) && pickAxe ||
                (isBestSword(stack) && getSwordsCounter() < 2 || stack.getItem() instanceof ItemSword && stack == mc.thePlayer.inventory.getStackInSlot(swordSlot)) && sword) {
            return false;
        }

        if (item instanceof ItemArmor) {
            if (AutoArmor.isBestArmor(stack)) return false;
        }

        if (item instanceof ItemBlock && (getBlocksCounter() > keepBlocks.getCurrentValue() // @off
                || blacklistedBlocks.contains(((ItemBlock) item).getBlock()))
                || item instanceof ItemPotion && isBadPotion(stack) && !keepBadPotion.getCurrentValue() || item instanceof ItemFood
                && !(item instanceof ItemAppleGold) && item != Items.bread && item
                != Items.pumpkin_pie && item != Items.baked_potato && item != Items.cooked_chicken
                && item != Items.carrot && item != Items.apple && item != Items.beef
                && item != Items.cooked_beef && item != Items.porkchop && item != Items.cooked_porkchop
                && item != Items.mushroom_stew && item != Items.cooked_fish && item != Items.melon
                || item instanceof ItemHoe || item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemArmor) { // @on
            return true;
        }

        final String unlocalizedName = item.getUnlocalizedName();

        return !keepSticks.getCurrentValue() && unlocalizedName.contains("stick") || unlocalizedName.contains("egg") // @off
                || getIronIngotsCounter() > 64 && item == Items.iron_ingot || getCoalCounter() > 64 && item == Items.coal
                || unlocalizedName.contains("string") || unlocalizedName.contains("flint")
                || unlocalizedName.contains("compass") || unlocalizedName.contains("dyePowder")
                || unlocalizedName.contains("feather")
                || unlocalizedName.contains("chest") && !displayName.toLowerCase().contains("collect")
                || unlocalizedName.contains("snow") || unlocalizedName.contains("torch")
                || unlocalizedName.contains("seeds") || unlocalizedName.contains("leather")
                || unlocalizedName.contains("reeds") || unlocalizedName.contains("record")
                || unlocalizedName.contains("snowball") || item instanceof ItemGlassBottle
                || item instanceof ItemSlab || idFromItem == 113 || idFromItem == 106
                || idFromItem == 325 || idFromItem == 326 && !keepBuckets.getCurrentValue() || idFromItem == 327
                || idFromItem == 111 || idFromItem == 85 || idFromItem == 188
                || idFromItem == 189 || idFromItem == 190 || idFromItem == 191
                || idFromItem == 401 || idFromItem == 192 || idFromItem == 81
                || idFromItem == 32 || unlocalizedName.contains("gravel")
                || unlocalizedName.contains("flower") || unlocalizedName.contains("tallgrass")
                || item instanceof ItemBow || item == Items.arrow && getArrowsCounter() > (keepBow.getCurrentValue() ? keepArrows.getCurrentValue() : 0) || idFromItem == 175
                || idFromItem == 340 || idFromItem == 339 || idFromItem == 160
                || idFromItem == 101 || idFromItem == 102 || idFromItem == 321
                || idFromItem == 323 || idFromItem == 389 || idFromItem == 416
                || idFromItem == 171 || idFromItem == 139 || idFromItem == 23
                || idFromItem == 25 || idFromItem == 69 || idFromItem == 70
                || idFromItem == 72 || idFromItem == 77
                || idFromItem == 96 || idFromItem == 107 || idFromItem == 123
                || idFromItem == 131 || idFromItem == 143 || idFromItem == 147
                || idFromItem == 148 || idFromItem == 151 || idFromItem == 152
                || idFromItem == 154 || idFromItem == 158 || idFromItem == 167
                || idFromItem == 403 || idFromItem == 183 || idFromItem == 184
                || idFromItem == 185 || idFromItem == 186 || idFromItem == 187
                || idFromItem == 331 || idFromItem == 356 || idFromItem == 404
                || idFromItem == 27 || idFromItem == 28 || idFromItem == 66
                || idFromItem == 76 || idFromItem == 157
                || idFromItem == 328
                || idFromItem == 342 || idFromItem == 343 || idFromItem == 398
                || idFromItem == 407 || idFromItem == 408 || idFromItem == 138
                || idFromItem == 352 || idFromItem == 385
                || idFromItem == 386 || idFromItem == 395 || idFromItem == 402
                || idFromItem == 418 || idFromItem == 419
                || idFromItem == 281 || idFromItem == 289 || idFromItem == 337
                || idFromItem == 336 || idFromItem == 348 || idFromItem == 353
                || idFromItem == 369 || idFromItem == 372 || idFromItem == 405
                || idFromItem == 406 || idFromItem == 409 || idFromItem == 410
                || idFromItem == 415 || idFromItem == 370 || idFromItem == 376
                || idFromItem == 377 || idFromItem == 378 || idFromItem == 379
                || idFromItem == 380 || idFromItem == 382 || idFromItem == 414
                || idFromItem == 346 || idFromItem == 347 || idFromItem == 420
                || idFromItem == 397 || idFromItem == 421 || idFromItem == 341
                || unlocalizedName.contains("sapling") || unlocalizedName.contains("stairs")
                || unlocalizedName.contains("door") || unlocalizedName.contains("monster_egg")
                || unlocalizedName.contains("sand") || unlocalizedName.contains("piston"); // @on
    }

    private boolean isBestAxe(ItemStack stack) {
        final Item item = stack.getItem();

        if (!(item instanceof ItemAxe)) {
            return false;
        }

        if (HYTUtils.isGodAxe(stack)) {
            return false;
        }

        final float value = getToolEffect(stack);

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.getSlotFromPlayerContainer(i).getHasStack()) {
                final ItemStack is = mc.thePlayer.getSlotFromPlayerContainer(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemAxe && !isBestSword(stack)) return false;
            }
        }

        return true;
    }

    private boolean isBadPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            final ItemPotion potion = (ItemPotion) stack.getItem();
            return potion.getEffects(stack) == null || isBadPotionEffect(stack, potion);
        }

        return false;
    }

    public boolean isBadPotionEffect(ItemStack stack, ItemPotion pot) {
        for (final PotionEffect effect : pot.getEffects(stack)) {
            final int potionID = effect.getPotionID();
            final Potion potion = Potion.potionTypes[effect.getPotionID()];

            if (potion.isBadEffect()) {
                return true;
            }
        }

        return false;
    }
}
