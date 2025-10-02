package axis.shiyan.wei.bluearchive.blinkfix.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ItemUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final int[][] armors = new int[][]{
            {item(Items.leather_helmet), item(Items.chainmail_helmet), item(Items.iron_helmet), item(Items.golden_helmet), item(Items.diamond_helmet)}, // Helmet
            {item(Items.leather_chestplate), item(Items.chainmail_chestplate), item(Items.iron_chestplate), item(Items.golden_chestplate), item(Items.diamond_chestplate)}, // Chest plate
            {item(Items.leather_leggings), item(Items.chainmail_leggings), item(Items.iron_leggings), item(Items.golden_leggings), item(Items.diamond_leggings)}, // Leggings
            {item(Items.leather_boots), item(Items.chainmail_boots), item(Items.iron_boots), item(Items.golden_boots), item(Items.diamond_boots)} // Boots
    };

    private static int item(Item item) {
        return Item.getIdFromItem(item);
    }

    public static int findEmptyHotbarSlot(int priority) {
        if (mc.thePlayer.inventory.mainInventory[priority] == null)
            return priority;

        return findEmptyHotbarSlot();
    }

    public static int findEmptyHotbarSlot() {
        for (int i = 0; i < 8; i++) {
            if (mc.thePlayer.inventory.mainInventory[i] == null)
                return i;
        }

        return mc.thePlayer.inventory.currentItem + (mc.thePlayer.inventory.getCurrentItem() == null ? 0 : ((mc.thePlayer.inventory.currentItem < 8) ? 4 : -1));
    }

    public static boolean isHeldingSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public static float getSwordDamage(ItemStack itemStack) {
        float damage = 0f;

        Optional<AttributeModifier> attributeModifier = itemStack.getAttributeModifiers().values().stream().findFirst();

        if (attributeModifier.isPresent()) {
            damage = (float) attributeModifier.get().getAmount();
        }

        return damage + EnchantmentHelper.getModifierForCreature(itemStack, EnumCreatureAttribute.UNDEFINED);
    }

    public static boolean isPotionNegative(ItemStack itemStack) {
        ItemPotion potion = (ItemPotion) itemStack.getItem();
        List<PotionEffect> potionEffectList = potion.getEffects(itemStack);
        return potionEffectList.stream().map(potionEffect -> Potion.potionTypes[potionEffect.getPotionID()]).anyMatch(Potion::isBadEffect);
    }

    public static boolean isBestSword(ContainerChest c, ItemStack item) {
        float itemDmg1 = getSwordDamage(item);
        float itemDmg2 = 0f;
        for (int i = 0; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                float tempDmg = getSwordDamage(mc.thePlayer.inventoryContainer.getSlot(i).getStack());
                if (tempDmg >= itemDmg2)
                    itemDmg2 = tempDmg;
            }
        }
        for (int i = 0; i < c.getLowerChestInventory().getSizeInventory(); ++i) {
            if (c.getLowerChestInventory().getStackInSlot(i) != null) {
                float tempDmg = getSwordDamage(c.getLowerChestInventory().getStackInSlot(i));
                if (tempDmg >= itemDmg2)
                    itemDmg2 = tempDmg;
            }
        }
        return itemDmg1 == itemDmg2;
    }

    public static float getArmorScore(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemArmor))
            return -1;

        ItemArmor itemArmor = (ItemArmor) itemStack.getItem();
        float score = 0;

        score += itemArmor.damageReduceAmount;

        if (EnchantmentHelper.getEnchantments(itemStack).size() <= 0)
            score -= 0.1;

        int protection = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack);

        score += protection * 0.2;

        return score;
    }

    public static int armorSlotToNormalSlot(int armorSlot) {
        return 8 - armorSlot;
    }

    public static boolean isBestArmor(ContainerChest c, ItemStack item) {
        float itemProtection1 = ((ItemArmor) item.getItem()).damageReduceAmount;
        float itemProtection2 = 0f;

        for (int[] armor : armors) {
            if (isContain(armor, Item.getIdFromItem(item.getItem()))) {
                for (int i = 0; i < 45; ++i) {
                    if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack() && isContain(armor, Item.getIdFromItem(mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem()))) {
                        float temp = ((ItemArmor) mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem()).damageReduceAmount;
                        if (temp > itemProtection2)
                            itemProtection2 = temp;
                    }
                }

                for (int i = 0; i < c.getLowerChestInventory().getSizeInventory(); ++i) {
                    if (c.getLowerChestInventory().getStackInSlot(i) != null && isContain(armor, Item.getIdFromItem(c.getLowerChestInventory().getStackInSlot(i).getItem()))) {
                        float temp = ((ItemArmor) c.getLowerChestInventory().getStackInSlot(i).getItem()).damageReduceAmount;
                        if (temp > itemProtection2)
                            itemProtection2 = temp;
                    }
                }
            }
        }

        return itemProtection1 == itemProtection2;
    }

    public static double getSwordAttackDamage(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemSword))
            return 0;

        ItemSword itemSword = (ItemSword) itemStack.getItem();

        double result = 1.0;

        result += itemSword.getDamageVsEntity();

        result += 1.25 * EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack);
        result += 0.5 * EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack);

        return result;
    }

    public static double getBowAttackDamage(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemBow))
            return 0;

        return EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemStack)
                + (EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemStack) * 0.1)
                + (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemStack) * 0.1);
    }

    public static double getToolEfficiency(ItemStack itemStack) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemTool))
            return 0;

        ItemTool sword = (ItemTool) itemStack.getItem();

        return EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack) + sword.efficiencyOnProperMaterial;
    }

    public static List<ItemStack> getInventoryItems() {
        List<ItemStack> result = new ArrayList<>(Arrays.asList(mc.thePlayer.inventory.mainInventory).subList(0, 35));
        for (int i = 0; i < 4; i++) {
            result.add(mc.thePlayer.inventory.armorItemInSlot(i));
        }
        return result;
    }

    public static List<ItemStack> getHotbarItems() {
        return new ArrayList<>(Arrays.asList(mc.thePlayer.inventory.mainInventory).subList(0, 9));
    }

    public static final int pickaxeSlot = 37;
    public static final int axeSlot = 38;
    public static final int shovelSlot = 39;

    public static void getBestPickaxe() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (isBestPickaxe(is) && pickaxeSlot != i) {
                    if (!isBestWeapon(is)) {
                        if (!mc.thePlayer.inventoryContainer.getSlot(pickaxeSlot).getHasStack()) {
                            PlayerUtils.clickSlot(i, pickaxeSlot - 36, 2);
                        } else if (!isBestPickaxe(mc.thePlayer.inventoryContainer.getSlot(pickaxeSlot).getStack())) {
                            PlayerUtils.clickSlot(i, pickaxeSlot - 36, 2);
                        }
                    }
                }
            }
        }
    }

    public static void getBestShovel() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (isBestShovel(is) && shovelSlot != i) {
                    if (!isBestWeapon(is)) {
                        if (!mc.thePlayer.inventoryContainer.getSlot(shovelSlot).getHasStack()) {
                            PlayerUtils.clickSlot(i, shovelSlot - 36, 2);
                        } else if (!isBestShovel(mc.thePlayer.inventoryContainer.getSlot(shovelSlot).getStack())) {
                            PlayerUtils.clickSlot(i, shovelSlot - 36, 2);
                        }
                    }
                }
            }
        }
    }

    public static void getBestAxe() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (isBestAxe(is) && axeSlot != i) {
                    if (!isBestWeapon(is)) {
                        if (!mc.thePlayer.inventoryContainer.getSlot(axeSlot).getHasStack()) {
                            PlayerUtils.clickSlot(i, axeSlot - 36, 2);
                        } else if (!isBestAxe(mc.thePlayer.inventoryContainer.getSlot(axeSlot).getStack())) {
                            PlayerUtils.clickSlot(i, axeSlot - 36, 2);
                        }
                    }
                }
            }
        }
    }

    public static boolean isBestPickaxe(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemPickaxe))
            return false;
        float value = getToolEffect(stack);
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemPickaxe) {
                    return false;
                }

            }
        }
        return true;
    }

    public static boolean isBestShovel(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemSpade))
            return false;
        float value = getToolEffect(stack);
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemSpade) {
                    return false;
                }

            }
        }
        return true;
    }

    public static boolean isBestAxe(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemAxe))
            return false;
        float value = getToolEffect(stack);

        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getToolEffect(is) > value && is.getItem() instanceof ItemAxe && !isBestWeapon(stack)) {
                    return false;
                }

            }
        }

        return true;
    }

    public static float getToolEffect(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemTool))
            return 0;
        String name = item.getUnlocalizedName();
        ItemTool tool = (ItemTool) item;
        float value = 1;
        if (item instanceof ItemPickaxe) {
            value = tool.getStrVsBlock(stack, Blocks.stone);
            if (name.toLowerCase().contains("gold")) {
                value -= 5;
            }
        } else if (item instanceof ItemSpade) {
            value = tool.getStrVsBlock(stack, Blocks.dirt);
            if (name.toLowerCase().contains("gold")) {
                value -= 5;
            }
        } else if (item instanceof ItemAxe) {
            value = tool.getStrVsBlock(stack, Blocks.log);
            if (name.toLowerCase().contains("gold")) {
                value -= 5;
            }
        } else
            return 1f;
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 0.0075D;
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) / 100d;
        return value;
    }

    public static boolean isBestWeapon(ItemStack stack) {
        float damage = getDamage(stack);
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (getDamage(is) > damage && (is.getItem() instanceof ItemSword))
                    return false;
            }
        }

        return stack.getItem() instanceof ItemSword;
    }

    public static float getDamage(ItemStack stack) {
        float damage = 0;
        Item item = stack.getItem();
        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            damage += tool.damageVsEntity;
        }
        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword) item;
            damage += sword.getDamageVsEntity();
        }
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f
                + EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.01f;
        return damage;
    }

    public static float getAttackDamage(ItemStack stack) {
        float damage = 1;

        if (stack == null) {
            return damage;
        }

        Item item = stack.getItem();

        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            damage += tool.damageVsEntity;
        }

        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword) item;
            damage += 4 + sword.getDamageVsEntity();
        }

        return damage;
    }

    public static float getSharpnessDamage(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f;
    }

    public static boolean isContain(int[] arr, int targetValue) {
        return ArrayUtils.contains(arr, targetValue);
    }
}
