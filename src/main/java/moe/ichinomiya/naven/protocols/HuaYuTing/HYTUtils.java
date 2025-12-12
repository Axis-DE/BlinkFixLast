package moe.ichinomiya.naven.protocols.HuaYuTing;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 *
 */
public class HYTUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isInLobby() {
        return mc.theWorld.playerEntities.stream().anyMatch(e -> e.getName().contains("问题反馈"));
    }

    public static boolean isHoldingGodAxe(EntityPlayer player) {
        ItemStack holdingItem = player.getEquipmentInSlot(0);

        return isGodAxe(holdingItem);
    }

    public static boolean isGodAxe(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (stack.getItem() != Items.golden_axe) {
            return false;
        }

        int durability = stack.getMaxDamage() - stack.getItemDamage();

        if (durability > 2) {
            return false;
        }

        NBTTagList enchantmentTagList = stack.getEnchantmentTagList();

        if (enchantmentTagList == null) {
            return false;
        }

        for (int i = 0; i < enchantmentTagList.tagCount(); i++) {
            NBTTagCompound nbt = (NBTTagCompound) enchantmentTagList.get(i);
            if (nbt.hasKey("id") && nbt.hasKey("lvl") && nbt.getInteger("id") == 16) {
                int level = nbt.getInteger("lvl");

                if (level >= 666) {
                    return true;
                }
            }
        }

        return false;
    }
    public static boolean isSharpnessAxe(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        NBTTagList enchantmentTagList = stack.getEnchantmentTagList();

        if (enchantmentTagList == null) {
            return false;
        }
        for (int i = 0; i < 9; i++) {
            if (stack.getItem() instanceof ItemAxe) {
                for (int j = 0; j < enchantmentTagList.tagCount(); j++) {
                    NBTTagCompound nbt = enchantmentTagList.getCompoundTagAt(j);
                    if (nbt.hasKey("id") && nbt.hasKey("lvl") && nbt.getInteger("id") == 16) {
                        int level = nbt.getInteger("lvl");
                        if (level == 10) {
                            return true;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < enchantmentTagList.tagCount(); i++) {
            NBTTagCompound nbt = (NBTTagCompound) enchantmentTagList.get(i);
            if (nbt.hasKey("id") && nbt.hasKey("lvl") && nbt.getInteger("id") == 16) {
                int level = nbt.getInteger("lvl");

                if (level >= 666) {
                    return false;
                }
            }
        }

        return false;
    }

    public static boolean isKBBall(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (stack.getItem() != Items.slime_ball) {
            return false;
        }

        NBTTagList enchantmentTagList = stack.getEnchantmentTagList();

        if (enchantmentTagList == null) {
            return false;
        }

        for (int i = 0; i < enchantmentTagList.tagCount(); i++) {
            NBTTagCompound nbt = (NBTTagCompound) enchantmentTagList.get(i);
            if (nbt.hasKey("id") && nbt.hasKey("lvl") && nbt.getInteger("id") == 19) {
                int level = nbt.getInteger("lvl");

                if (level >= 2) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isFireEnchanted(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        NBTTagList enchantmentTagList = stack.getEnchantmentTagList();

        if (enchantmentTagList == null) {
            return false;
        }

        for (int i = 0; i < enchantmentTagList.tagCount(); i++) {
            NBTTagCompound nbt = (NBTTagCompound) enchantmentTagList.get(i);
            if (nbt.hasKey("id") && nbt.hasKey("lvl") && nbt.getInteger("id") == 20) {
                int level = nbt.getInteger("lvl");

                if (level >= 1) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isEnchantedGoldenApple(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (stack.getItem() != Items.golden_apple) {
            return false;
        }

        return stack.hasEffect();
    }

    public static boolean isHoldingEnchantedGoldenApple(EntityPlayer player) {
        return isEnchantedGoldenApple(player.getEquipmentInSlot(0));
    }
}
