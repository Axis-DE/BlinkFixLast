package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.clickgui.ClientClickGUI;
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
