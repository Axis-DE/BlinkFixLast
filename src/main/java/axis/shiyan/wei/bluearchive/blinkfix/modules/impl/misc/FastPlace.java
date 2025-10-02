package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;

import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventTick;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.utils.TimeHelper;
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
