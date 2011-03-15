/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Messaging {
    public static final String[] colors = { "<black>", "<navy>", "<green>", "<teal>", "<red>", "<purple>", "<gold>", "<silver>", "<gray>", "<blue>", "<lime>",
                                           "<aqua>", "<rose>", "<pink>", "<yellow>", "<white>" };

    public static final String[] colorCodes = { "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f" };

    public static String colorize(String msg) {
        String colored = replaceByList(msg, colors, colorCodes);
        colored = colored.replaceAll("(&([a-z0-9]))", "§$2").replace("&&", "&");
        return colored;
    }

    public static String replaceByList(String msg, String[] originals, String[] replacements) {
        if (originals.length != replacements.length)
            return msg;

        String replaced = msg;
        for (int i = 0; i < originals.length; i++) {
            replaced.replace(originals[i], replacements[i]);
        }

        return replaced;
    }
    
    public static void send(CommandSender sender, String msg) {
        sender.sendMessage(colorize(msg));
    }
    
    public static void broadcast(JavaPlugin plugin, String msg) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(colorize(msg));
        }
    }
}
