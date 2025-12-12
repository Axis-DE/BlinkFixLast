package moe.ichinomiya.naven.modules.impl.misc;

import de.florianmichael.vialoadingbase.ViaLoadingBase;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.api.types.Priority;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.TimeHelper;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.Optional;

@ModuleInfo(name = "AutoArmor", description = "Automatically equips the best armor", category = Category.MISC)
public class AutoArmor extends Module {
    static TimeHelper timer = new TimeHelper();
    FloatValue delay = ValueBuilder.create(this, "Equip Delay").setDefaultFloatValue(100).setMinFloatValue(0).setMaxFloatValue(1000).setFloatStep(10).build().getFloatValue();
    BooleanValue openinv = ValueBuilder.create(this,"Open Inv").setDefaultBooleanValue(false).build().getBooleanValue();
    FloatValue openDelay = ValueBuilder.create(this, "Open Delay").setDefaultFloatValue(100).setMinFloatValue(0).setMaxFloatValue(200).setFloatStep(10).setVisibility(() -> openinv.getCurrentValue()).build().getFloatValue();
    private final TimeHelper openDelayTimer = new TimeHelper();
    private boolean waitingForOpenDelay = false;
    public static boolean isWorking() {
        return !timer.check(300 + ((AutoArmor) BlinkFix.getInstance().getModuleManager().getModule(AutoArmor.class)).delay.getCurrentValue());
    }
    @Override
    public boolean onDisable(){
        waitingForOpenDelay = false;
        return false;
    }

    public static boolean isBestArmor(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        if (!(stack.getItem() instanceof ItemArmor)) {
            return false;
        }

        Optional<ItemStack> bestArmor = mc.thePlayer.inventoryContainer.inventorySlots
                .stream()
                .filter(Slot::getHasStack)
                .map(Slot::getStack)
                .filter(slotStack -> slotStack.getItem() instanceof ItemArmor)
                .filter(slotStack -> ((ItemArmor) slotStack.getItem()).armorType == ((ItemArmor) stack.getItem()).armorType)
                .min((stack1, stack2) -> {
                    float valence1 = getProtection(stack1);
                    float valence2 = getProtection(stack2);
                    return Float.compare(valence2, valence1);
                });

        return bestArmor.map(itemStack -> itemStack.equals(stack)).orElse(true);
    }

    public static float getProtection(ItemStack itemStack) {
        int valence = 0;

        if (itemStack == null) {
            return 0;
        }

        if (itemStack.getItem() instanceof ItemArmor) {
            valence += ((ItemArmor) itemStack.getItem()).damageReduceAmount * 100;
        }

        valence += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack);

        return valence;
    }

    @EventTarget(Priority.HIGHEST)
    public void onEvent(EventMotion event) {
        if (event.getType() == EventType.PRE) {
            if (openinv.getCurrentValue() && waitingForOpenDelay) {
                if (openDelayTimer.delay(openDelay.getCurrentValue())) {
                    waitingForOpenDelay = false;
                } else {
                    return;
                }
            } else if (openinv.getCurrentValue()) {
                waitingForOpenDelay = true;
                openDelayTimer.reset();
                return;
            }
            if (ContainerStealer.isStealing()) {
                return;
            }
            if (openinv.getCurrentValue() && !(mc.currentScreen instanceof GuiInventory)){
                return;
            }

            if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) {
                for (int type = 1; type < 5; type++) {
                    if (mc.thePlayer.inventoryContainer.getSlot(4 + type).getHasStack()) {
                        ItemStack slotStack = mc.thePlayer.inventoryContainer.getSlot(4 + type).getStack();
                        if (isBestArmor(slotStack)) {
                            continue;
                        } else {
                            mc.thePlayer.drop(4 + type);
                        }
                    }

                    for (int i = 9; i < 45; i++) {
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                            ItemStack slotStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                            if (isBestArmor(slotStack) && getProtection(slotStack) > 0) {
                                if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() == 340) {
                                    if (timer.check(delay.getCurrentValue())) {
                                        mc.playerController.windowClick(0, i, 0, 0, mc.thePlayer);

                                        if (slotStack.getUnlocalizedName().contains("helmet")) {
                                            mc.playerController.windowClick(0, 5, 0, 0, mc.thePlayer);
                                            timer.reset();
                                        } else if (slotStack.getUnlocalizedName().contains("chestplate")) {
                                            mc.playerController.windowClick(0, 6, 0, 0, mc.thePlayer);
                                            timer.reset();
                                        } else if (slotStack.getUnlocalizedName().contains("leggings")) {
                                            mc.playerController.windowClick(0, 7, 0, 0, mc.thePlayer);
                                            timer.reset();
                                        } else if (slotStack.getUnlocalizedName().contains("boots")) {
                                            mc.playerController.windowClick(0, 8, 0, 0, mc.thePlayer);
                                            timer.reset();
                                        }
                                    }
                                } else {
                                    if (timer.check(delay.getCurrentValue())) {
                                        mc.thePlayer.shiftClick(i);
                                        timer.reset();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
