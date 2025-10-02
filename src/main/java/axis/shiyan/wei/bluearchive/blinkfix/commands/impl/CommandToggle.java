package axis.shiyan.wei.bluearchive.blinkfix.commands.impl;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import axis.shiyan.wei.bluearchive.blinkfix.commands.Command;
import axis.shiyan.wei.bluearchive.blinkfix.commands.CommandInfo;
import axis.shiyan.wei.bluearchive.blinkfix.exceptions.NoSuchModuleException;
import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
import axis.shiyan.wei.bluearchive.blinkfix.utils.ChatUtils;

@CommandInfo(name = "toggle", description = "Toggle a module", aliases = {"t"})
public class CommandToggle extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            String moduleName = args[0];
            try {
                Module module = BlinkFix.getInstance().getModuleManager().getModule(moduleName);

                if (module != null) {
                    module.toggle();
                } else {
                    ChatUtils.addChatMessage("Invalid module.");
                }
            } catch (NoSuchModuleException e) {
                ChatUtils.addChatMessage("Invalid module.");
            }
        }
    }

    @Override
    public String[] onTab(String[] args) {
        return BlinkFix.getInstance().getModuleManager().getModules().stream().map(Module::getName).filter(name -> name.toLowerCase().startsWith(args.length == 0 ? "" : args[0].toLowerCase())).toArray(String[]::new);
    }
}
