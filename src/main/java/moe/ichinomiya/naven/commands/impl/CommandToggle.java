package moe.ichinomiya.naven.commands.impl;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.commands.Command;
import moe.ichinomiya.naven.commands.CommandInfo;
import moe.ichinomiya.naven.exceptions.NoSuchModuleException;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.utils.ChatUtils;

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
