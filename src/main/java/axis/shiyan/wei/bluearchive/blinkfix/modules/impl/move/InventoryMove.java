package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
import axis.shiyan.wei.bluearchive.blinkfix.events.impl.EventRunTicks;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;

@ModuleInfo(name = "InventoryMove", description = "Allows you to move while your inventory is open", category = Category.MOVEMENT)
public class InventoryMove extends Module {
    @EventTarget
    public void onMotion(EventRunTicks e) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward);
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight);
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump);
            mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint) || BlinkFix.getInstance().getModuleManager().getModule(Sprint.class).isEnabled();
        }
    }
}
