package moe.ichinomiya.naven.commands.impl;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.commands.Command;
import moe.ichinomiya.naven.commands.CommandInfo;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventKey;
import moe.ichinomiya.naven.exceptions.NoSuchModuleException;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.utils.ChatUtils;
import org.lwjgl.input.Keyboard;

@CommandInfo(name = "bind", description = "Bind a command to a key", aliases = {"b"})
public class CommandBind extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            String moduleName = args[0];
            try {
                Module module = BlinkFix.getInstance().getModuleManager().getModule(moduleName);

                if (module != null) {
                    ChatUtils.addChatMessage("Press a key to bind " + moduleName + " to.");

                    BlinkFix.getInstance().getEventManager().register(new Object() {
                        @EventTarget
                        public void onKey(EventKey e) {
                            if (e.isState()) {
                                module.setKey(e.getKey());
                                String keyName = Keyboard.getKeyName(e.getKey());

                                ChatUtils.addChatMessage("Bound " + moduleName + " to " + keyName + ".");
                                BlinkFix.getInstance().getEventManager().unregister(this);

                                BlinkFix.getInstance().getFileManager().save();
                            }
                        }
                    });
                } else {
                    ChatUtils.addChatMessage("Invalid module.");
                }
            } catch (NoSuchModuleException e) {
                ChatUtils.addChatMessage("Invalid module.");
            }
        } else if (args.length == 2) {
            String moduleName = args[0];
            String keyName = args[1];

            try {
                Module module = BlinkFix.getInstance().getModuleManager().getModule(moduleName);

                if (module != null) {
                    if (keyName.equalsIgnoreCase("none")) {
                        module.setKey(Keyboard.KEY_NONE);
                        ChatUtils.addChatMessage("Unbound " + moduleName + ".");
                        BlinkFix.getInstance().getFileManager().save();
                    } else {
                        int key = Keyboard.getKeyIndex(keyName.toUpperCase());

                        if (key != Keyboard.KEY_NONE) {
                            module.setKey(key);
                            ChatUtils.addChatMessage("Bound " + moduleName + " to " + keyName + ".");
                            BlinkFix.getInstance().getFileManager().save();
                        } else {
                            ChatUtils.addChatMessage("Invalid key.");
                        }
                    }
                } else {
                    ChatUtils.addChatMessage("Invalid module.");
                }
            } catch (NoSuchModuleException e) {
                ChatUtils.addChatMessage("Invalid module.");
            }
        } else {
            ChatUtils.addChatMessage("Usage: .bind <module> [key]");
        }
    }

    @Override
    public String[] onTab(String[] args) {
        return BlinkFix.getInstance().getModuleManager().getModules().stream().map(Module::getName).filter(name -> name.toLowerCase().startsWith(args.length == 0 ? "" : args[0].toLowerCase())).toArray(String[]::new);
    }
}
