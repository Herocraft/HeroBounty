package com.herocraftonline.dthielke.herobounty.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Messaging {

    public static void send(CommandSender player, String msg, String... params) {
        player.sendMessage(parameterizeMessage(msg, params));
    }

    public static void broadcast(String msg, String... params) {
        Bukkit.getServer().broadcastMessage(parameterizeMessage(msg, params));
    }

    private static String parameterizeMessage(String msg, String... params) {
        msg = ChatColor.YELLOW + msg;
        for (int i = 0; i < params.length; i++) {
            msg = msg.replace("$" + (i + 1), ChatColor.WHITE + params[i] + ChatColor.YELLOW);
        }
        return msg;
    }

}
