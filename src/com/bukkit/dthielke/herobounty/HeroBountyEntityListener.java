package com.bukkit.dthielke.herobounty;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class HeroBountyEntityListener extends EntityListener {
    private final HeroBountyPlugin plugin;
    
    private HashMap<String, String> deathRecords = new HashMap<String, String>();
 
    public HeroBountyEntityListener(HeroBountyPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;
        
        Player defender = (Player)entity;
        
        String defenderName = defender.getName();
        String attackerName = deathRecords.get(defenderName);
        
        System.out.println("Player died - defender: " + defenderName + "\t attacker: " + attackerName);
        
        List<Bounty> bounties = plugin.getBounties();
        
        for (int i = 0; i < bounties.size(); i++) {
            Bounty b = bounties.get(i);
            
            System.out.println("Bounty target: " + b.getTarget() + "\t hunter: " + b.getHunter());
            
            if (b.getTarget().equalsIgnoreCase(defenderName) && b.getHunter().equalsIgnoreCase(attackerName)) {
                
                System.out.println("Found bounty match");
                plugin.completeBounty(i);
                deathRecords.remove(defenderName);
                return;
            }
        }
    }
    
    public void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) {
        if (event.isCancelled())
            return;
        
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
            return;
        
        Player defender = (Player)event.getEntity();
        Player attacker = (Player)event.getDamager();
        
        int health = defender.getHealth() - event.getDamage();
        
        if (health > 0)
            return;
        
        for (Bounty b : plugin.getBounties()) {
            if (!b.getHunter().isEmpty() && b.getTarget().equalsIgnoreCase(defender.getName())) {
                System.out.println("Adding death record - defender: " + defender.getName() + "\t attacker: " + attacker.getName());
                
                deathRecords.put(defender.getName(), attacker.getName());
                break;
            }
        }
    }
    
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
            return;
        
        Player defender = (Player)event.getEntity();
        Player attacker = (Player)event.getDamager();
        
        int health = defender.getHealth() - event.getDamage();
        
        if (health > 0)
            return;
        
        for (Bounty b : plugin.getBounties()) {
            if (!b.getHunter().isEmpty() && b.getTarget().equalsIgnoreCase(defender.getName())) {
                System.out.println("Adding death record - defender: " + defender.getName() + "\t attacker: " + attacker.getName());
                
                deathRecords.put(defender.getName(), attacker.getName());
                break;
            }
        }
    }
}
