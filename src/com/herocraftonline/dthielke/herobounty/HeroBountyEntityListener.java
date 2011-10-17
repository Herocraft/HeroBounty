/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import com.herocraftonline.dthielke.herobounty.bounties.Bounty;

public class HeroBountyEntityListener extends EntityListener {
    public static HeroBounty plugin;

    private HashMap<String, String> deathRecords = new HashMap<String, String>();

    public HeroBountyEntityListener(HeroBounty plugin) {
        HeroBountyEntityListener.plugin = plugin;
    }

    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled())
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        Player defender = (Player) event.getEntity();
        int health = defender.getHealth();
        int damage = event.getDamage();
        String defenderName = defender.getName();
        String attackerName = "NOT_A_PLAYER";

        if (event instanceof EntityDamageByProjectileEvent) {
            EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
            Entity attacker = subEvent.getDamager();
            if (attacker instanceof Player)
                attackerName = ((Player) attacker).getName();
        } else if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
            Entity attacker = subEvent.getDamager();
            if (attacker instanceof Player)
                attackerName = ((Player) attacker).getName();
        }

        tryAddDeathRecord(defenderName, attackerName, health, damage);
    }

    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player defender = (Player) entity;

        String defenderName = defender.getName();
        String attackerName = deathRecords.get(defenderName);

        List<Bounty> bounties = plugin.getBountyManager().getBounties();

        for (int i = 0; i < bounties.size(); i++) {
            Bounty b = bounties.get(i);

            if (b.getTarget().equalsIgnoreCase(defenderName) && b.isHunter(attackerName)) {
                plugin.getBountyManager().completeBounty(i, attackerName);
                deathRecords.remove(defenderName);
                return;
            }
        }
    }

    private void tryAddDeathRecord(String defenderName, String attackerName, int health, int damage) {
        health -= damage;

        if (health > 0)
            return;

        for (Bounty b : plugin.getBountyManager().getBounties()) {
            if (b.getTarget().equalsIgnoreCase(defenderName)) {
                deathRecords.put(defenderName, attackerName);
                break;
            }
        }
    }
}
