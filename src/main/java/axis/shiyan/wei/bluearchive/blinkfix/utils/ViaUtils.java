package axis.shiyan.wei.bluearchive.blinkfix.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ViaUtils {
    public static int getItemId(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("ViaRewind1_8to1_9")) {
            return tag.getCompoundTag("ViaRewind1_8to1_9").getInteger("id");
        }

        return Item.getIdFromItem(stack.getItem());
    }

    public static int getItemData(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("ViaRewind1_8to1_9")) {
            return tag.getCompoundTag("ViaRewind1_8to1_9").getInteger("data");
        }

        return stack.getMetadata();
    }

    public static String getItemDisplayName(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("ViaRewind1_8to1_9")) {
            return tag.getCompoundTag("ViaRewind1_8to1_9").getString("displayName");
        }

        return stack.getDisplayName();
    }

    public static boolean isTotemOfUndying(ItemStack stack) {
        return getItemId(stack) == 418;
    }

    public static boolean isCrossbow(ItemStack stack) {
        return getItemId(stack) == 261 && getItemDisplayName(stack).equals("§f1.14 Crossbow");
    }

    public static boolean isEndCrystal(ItemStack stack) {
        return getItemId(stack) == 426;
    }
}
