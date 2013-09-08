/*
 * Copyright (c) 2012 David "DThielke" Thielke <dave.thielke@gmail.com>.
 * All rights reserved.
 */

package com.herocraftonline.dthielke.herobounty.command;

import com.herocraftonline.dthielke.herobounty.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHandler {
    private Map<String, Command> commands = new LinkedHashMap<String, Command>();
    private Map<String, Command> identifiers = new HashMap<String, Command>();

    public void addCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String ident : command.getIdentifiers()) {
            identifiers.put(ident.toLowerCase(), command);
        }
    }

    public boolean dispatch(CommandSender sender, String label, String[] args) {
        for (int argsIncluded = args.length; argsIncluded >= 0; argsIncluded--) {
            StringBuilder identifier = new StringBuilder(label);
            for (int i = 0; i < argsIncluded; i++) {
                identifier.append(" ").append(args[i]);
            }

            Command cmd = getCmdFromIdent(identifier.toString(), sender);
            if (cmd == null) {
                continue;
            }

            String[] realArgs = Arrays.copyOfRange(args, argsIncluded, args.length);

            if (!cmd.isInProgress(sender)) {
                if (realArgs.length < cmd.getMinArguments() || realArgs.length > cmd.getMaxArguments()) {
                    displayCommandHelp(cmd, sender);
                    return true;
                } else if (realArgs.length > 0 && realArgs[0].equals("?")) {
                    displayCommandHelp(cmd, sender);
                    return true;
                }
            }

            if (!cmd.getPermission().isEmpty() && !sender.hasPermission(cmd.getPermission())) {
                Messaging.send(sender, "§7[§eBounty§7] Insufficient permission.");
                return true;
            }

            cmd.execute(sender, identifier.toString(), realArgs);
            return true;
        }

        return true;
    }

    private void displayCommandHelp(Command cmd, CommandSender sender) {
        sender.sendMessage("§cCommand:§e " + cmd.getName());
        sender.sendMessage("§cDescription:§e " + cmd.getDescription());
        sender.sendMessage("§cUsage:§e " + cmd.getUsage());
        if (cmd.getNotes() != null) {
            for (String note : cmd.getNotes()) {
                sender.sendMessage("§e" + note);
            }
        }
    }

    public Command getCmdFromIdent(String ident, CommandSender executor) {
        ident = ident.toLowerCase();
        if (identifiers.containsKey(ident)) {
            return identifiers.get(ident);
        }

        for (Command cmd : commands.values()) {
            if (cmd.isIdentifier(executor, ident)) {
                return cmd;
            }
        }

        return null;
    }

    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public List<Command> getCommands() {
        return new ArrayList<Command>(commands.values());
    }

    public void removeCommand(Command command) {
        commands.remove(command.getName().toLowerCase());
        for (String ident : command.getIdentifiers()) {
            identifiers.remove(ident.toLowerCase());
        }
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender.isOp()) {
            return true;
        }

        if (!(sender instanceof Player) || permission == null || permission.isEmpty()) {
            return true;
        }

        Player player = (Player) sender;
        return player.hasPermission(permission);
    }
}
