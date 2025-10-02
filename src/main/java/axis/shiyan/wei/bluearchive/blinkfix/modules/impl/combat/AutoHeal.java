package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.combat;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventMotion;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
import axis.shiyan.wei.bluearchive.blinkfix.values.impl.FloatValue;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;

@ModuleInfo(name = "AutoHeal", description = "Automatically heal your self.", category = Category.COMBAT)
public class AutoHeal extends Module {
    BooleanValue speedCheck = ValueBuilder.create(this, "Speed Check").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue regenCheck = ValueBuilder.create(this, "Regen Check").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue head = ValueBuilder.create(this, "Use Head").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue soup = ValueBuilder.create(this, "Use Soup").setDefaultBooleanValue(true).build().getBooleanValue();

    FloatValue delay = ValueBuilder.create(this, "Delay").setDefaultFloatValue(300).setFloatStep(1).setMinFloatValue(50).setMaxFloatValue(1000).build().getFloatValue();
    FloatValue health = ValueBuilder.create(this, "Health Percent").setDefaultFloatValue(0.5f).setFloatStep(0.05f).setMinFloatValue(0f).setMaxFloatValue(1f).build().getFloatValue();

    TimeHelper timer = new TimeHelper();

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE) {
            if (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth() < health.getCurrentValue() && timer.delay(delay.getCurrentValue())) {
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && speedCheck.getCurrentValue()) {
                    return;
                }

                if (mc.thePlayer.isPotionActive(Potion.regeneration) && regenCheck.getCurrentValue()) {
                    return;
                }

                int headSlot = getHeadFromInventory();
                int soupSlot = getSoupFromInventory();

                if (headSlot != -1 && head.getCurrentValue()) {
                    useItem(headSlot);
                }

                if (soupSlot != -1 && soup.getCurrentValue()) {
                    useItem(soupSlot);
                }

                timer.reset();
            }
        }
    }

    private void useItem(int slot) {
        mc.thePlayer.swap(slot, 8);
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(8));
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
    }

    public int getSoupFromInventory() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (stack.getItem() == Items.mushroom_stew) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getHeadFromInventory() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (stack.getItem() instanceof ItemSkull && stack.getDisplayName().contains("金头")) {
                    return i;
                }
            }
        }
        return -1;
    }
}
