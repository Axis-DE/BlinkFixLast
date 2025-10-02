package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;


import java.util.List;

@ModuleInfo(name = "AutoPot", description = "Automatically throw beneficial splash potions", category = Category.MISC)
public class AutoPot extends Module {
    private final TimeHelper delayTimer = new TimeHelper();
    private final Minecraft mc = Minecraft.getMinecraft();
    FloatValue delay = ValueBuilder.create(this, "Delay").setDefaultFloatValue(250).setFloatStep(50).setMinFloatValue(0).setMaxFloatValue(1000).build().getFloatValue();
    FloatValue healthb = ValueBuilder.create(this, "Health").setDefaultFloatValue(10).setFloatStep(1).setMinFloatValue(1).setMaxFloatValue(20).build().getFloatValue();
    FloatValue slot = ValueBuilder.create(this, "Slot").setDefaultFloatValue(6).setFloatStep(1).setMinFloatValue(0).setMaxFloatValue(8).build().getFloatValue();

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Check if we should throw a potion
        if (!delayTimer.delay(delay.getCurrentValue())) return;
        if (mc.thePlayer.isUsingItem()) return;

        int potionSlot = findPotionSlot();
        if (potionSlot == -1) return;

        // Prepare to throw the potion
        int oldSlot = mc.thePlayer.inventory.currentItem;

        // Move potion to hotbar if needed
        if (potionSlot >= 36) {
            mc.playerController.windowClick(
                    mc.thePlayer.inventoryContainer.windowId,
                    potionSlot,
                    (int) slot.getCurrentValue(),
                    2,
                    mc.thePlayer
            );
            mc.thePlayer.inventory.currentItem = (int) slot.getCurrentValue();
        } else {
            mc.thePlayer.inventory.currentItem = potionSlot - 36;
        }
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemPotion) {
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(
                    new BlockPos(-1, -1, -1),
                    255,
                    heldItem,
                    0.5f, 0.5f, 0.5f
            ));
        }


        mc.thePlayer.inventory.currentItem = oldSlot;
        delayTimer.reset();
    }

    private int findPotionSlot() {
        for (int i = 9; i < mc.thePlayer.inventoryContainer.getInventory().size(); i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack == null || !(stack.getItem() instanceof ItemPotion)) continue;
            if (!ItemPotion.isSplash(stack.getMetadata())) continue;

            List<PotionEffect> effects = ((ItemPotion) stack.getItem()).getEffects(stack);
            if (effects == null || effects.isEmpty()) continue;

            // Check if any effect is beneficial and not already active
            for (PotionEffect effect : effects) {
                if (isBeneficial(effect) && !hasEffectAlready(effect)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isBeneficial(PotionEffect effect) {
        String effectName = effect.getEffectName();
        float health = mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount();

        return "effect.moveSpeed".equals(effectName) ||
                "effect.jump".equals(effectName) ||
                ("effect.regeneration".equals(effectName) && health <= healthb.getCurrentValue()) ||
                "effect.nightVision".equals(effectName) ||
                "effect.invisibility".equals(effectName) ||
                "effect.resistance".equals(effectName) ||
                "effect.fireResistance".equals(effectName) ||
                ("effect.heal".equals(effectName) && health <= healthb.getCurrentValue());
    }

    private boolean hasEffectAlready(PotionEffect effect) {
        for (PotionEffect activeEffect : mc.thePlayer.getActivePotionEffects()) {
            if (activeEffect.getEffectName().equals(effect.getEffectName())) {
                return true;
            }
        }
        return false;
    }
}