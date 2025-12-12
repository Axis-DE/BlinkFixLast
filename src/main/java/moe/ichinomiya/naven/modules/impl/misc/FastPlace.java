package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventTick;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.utils.TimeHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FastPlace", description = "Place blocks faster", category = Category.MISC)
public class FastPlace extends Module {
    TimeHelper timer = new TimeHelper();

    @EventTarget
    public void onTicks(EventTick e) {
        if (Mouse.isButtonDown(mc.gameSettings.keyBindUseItem.getKeyCode() + 100) && timer.delay(1000 / 15f)) {
            if (isHoldingBlock() && mc.currentScreen == null) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

                timer.reset();
            }
        }
    }

    public boolean isHoldingBlock() {
        return mc.thePlayer != null && mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock);
    }
}
