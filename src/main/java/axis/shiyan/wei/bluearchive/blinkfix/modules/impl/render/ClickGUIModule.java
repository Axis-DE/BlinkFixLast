package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render;

import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
import axis.shiyan.wei.bluearchive.blinkfix.ui.clickgui.ClientClickGUI;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "ClickGUI", category = Category.RENDER, description = "The ClickGUI")
public class ClickGUIModule extends Module {
    ClientClickGUI clickGUI = null;

    @Override
    protected void initModule() {
        super.initModule();
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public boolean onEnable() {
        if (clickGUI == null) {
            clickGUI = new ClientClickGUI();
        }

        super.onEnable();
        mc.displayGuiScreen(clickGUI);
        this.toggle();
        return false;
    }
}
