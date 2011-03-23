package com.herocraftonline.dthielke.herobounty.util;

import org.bukkit.command.CommandSender;

import com.herocraftonline.dthielke.herobounty.HeroBounty;

public class Messaging {

    public static void send(HeroBounty plugin, CommandSender player, String msg, String ... params) {
        player.sendMessage(parameterizeMessage(plugin, msg, params));
    }
    
    public static void broadcast(HeroBounty plugin, String msg, String ... params) {
        plugin.getServer().broadcastMessage(parameterizeMessage(plugin, msg, params));
    }
    
    private static String parameterizeMessage(HeroBounty plugin, String msg, String ... params) {
        msg = plugin.getTag() + "§c" + msg;
        for (int i = 0; i < params.length; i++) {
            msg = msg.replace("$" + (i+1), "§f" + params[i] + "§c");
        }
        return msg;
    }
    
}
