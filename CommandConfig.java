package com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.impl;

import com.BlueArchive.Sunaookami.Shiroko.Yukari.Yukari;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.Command;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.commands.CommandInfo;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.utils.ChatUtils;

import java.io.File;

@CommandInfo(name = "config", description = "load/save config", aliases = {"config","cfg"})
public class CommandConfig extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length < 1) {
            sendHelp();
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "save":
                if (args.length < 2) {
                    ChatUtils.addChatMessage("Usage: .config save <name>");
                    return;
                }
                String saveName = args[1];
                if (Yukari.getInstance().getConfigManager().saveConfig(saveName)) {
                    ChatUtils.addChatMessage("Config saved successfully: " + saveName + ".cfg");
                } else {
                    ChatUtils.addChatMessage("Failed to save config: " + saveName);
                }
                break;

            case "load":
                if (args.length < 2) {
                    ChatUtils.addChatMessage("Usage: .config load <name>");
                    return;
                }
                String loadName = args[1];
                if (Yukari.getInstance().getConfigManager().loadConfig(loadName)) {
                    ChatUtils.addChatMessage("Config loaded successfully: " + loadName);
                } else {
                    ChatUtils.addChatMessage("Config not found: " + loadName);
                }
                break;

            case "list":
                String[] configs = Yukari.getInstance().getConfigManager().getConfigList();
                if (configs.length == 0) {
                    ChatUtils.addChatMessage("No configs found.");
                } else {
                    ChatUtils.addChatMessage("Available configs:");
                    for (String config : configs) {
                        ChatUtils.addChatMessage(" - " + config);
                    }
                }
                break;

            case "folder":
                try {
                    File configFolder = Yukari.getInstance().getConfigManager().getConfigFolder();
                    java.awt.Desktop.getDesktop().open(configFolder);
                    ChatUtils.addChatMessage("Opened config folder");
                } catch (Exception e) {
                    ChatUtils.addChatMessage("Failed to open config folder");
                }
                break;

            case "delete":
                if (args.length < 2) {
                    ChatUtils.addChatMessage("Usage: .config delete <name>");
                    return;
                }
                String deleteName = args[1];
                if (Yukari.getInstance().getConfigManager().deleteConfig(deleteName)) {
                    ChatUtils.addChatMessage("Config deleted: " + deleteName);
                } else {
                    ChatUtils.addChatMessage("Config not found: " + deleteName);
                }
                break;

            default:
                sendHelp();
                break;
        }
    }

    private void sendHelp() {
        ChatUtils.addChatMessage("Config Commands:");
        ChatUtils.addChatMessage(".config save <name> - Save current config");
        ChatUtils.addChatMessage(".config load <name> - Load config");
        ChatUtils.addChatMessage(".config list - List all configs");
        ChatUtils.addChatMessage(".config folder - Open config folder");
        ChatUtils.addChatMessage(".config delete <name> - Delete config");
    }

    @Override
    public String[] onTab(String[] args) {
        if (args.length == 1) {
            return new String[]{"save", "load", "list", "folder", "delete"};
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("load") ||
                    args[0].equalsIgnoreCase("delete")) {
                return Yukari.getInstance().getConfigManager().getConfigList();
            }
        }
        return new String[0];
    }
}