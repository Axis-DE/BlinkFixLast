package com.BlueArchive.Sunaookami.Shiroko.Yukari.commands;

import com.BlueArchive.Sunaookami.Shiroko.Yukari.Yukari;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.impl.CommandBind;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.impl.CommandHacker;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.impl.CommandTestWidth;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.impl.CommandToggle;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.impl.CommandConfig;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.events.api.EventTarget;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.events.impl.EventClientChat;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.utils.ChatUtils;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    public final static String PREFIX = ".";

    public final Map<String, Command> aliasMap = new HashMap<>();

    public CommandManager() {
        try {
            initCommands();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Yukari.getInstance().getEventManager().register(this);
    }

    private void initCommands() {
        registerCommand(new CommandBind());
        registerCommand(new CommandToggle());
        registerCommand(new CommandHacker());
        registerCommand(new CommandTestWidth());
        registerCommand(new CommandConfig());
    }

    private void registerCommand(Command command) {
        command.initCommand();

        aliasMap.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            aliasMap.put(alias.toLowerCase(), command);
        }
    }

    @EventTarget
    public void onChat(EventClientChat e) {
        if (e.getMessage().startsWith(PREFIX)) {
            e.setCancelled(true);

            String chatMessage = e.getMessage().substring(PREFIX.length());

            String[] arguments = chatMessage.split(" ");

            if (arguments.length < 1) {
                ChatUtils.addChatMessage("Invalid command.");
                return;
            }

            String alias = arguments[0].toLowerCase();
            Command command = aliasMap.get(alias);

            if (command == null) {
                ChatUtils.addChatMessage("Invalid command.");
                return;
            }

            String[] args = new String[arguments.length - 1];
            System.arraycopy(arguments, 1, args, 0, args.length);
            command.onCommand(args);
        }
    }
}
