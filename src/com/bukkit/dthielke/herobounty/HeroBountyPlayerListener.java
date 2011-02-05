package com.bukkit.dthielke.herobounty;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HeroBountyPlayerListener extends PlayerListener {
    public static HeroBountyPlugin plugin;
    
    public HeroBountyPlayerListener(HeroBountyPlugin plugin) {
        HeroBountyPlayerListener.plugin = plugin;
    }
    
    public void onPlayerTeleport(PlayerMoveEvent event) {
        
    }
}
