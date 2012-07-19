package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BasicCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand extends BasicCommand {
    private final HeroBounty plugin;

    public AcceptCommand(HeroBounty plugin) {
        super("Accept");
        setDescription("Accepts a posted bounty for a small contract fee");
        setUsage("§e/bounty accept §9<target>");
        setArgumentRange(1, 1);
        setIdentifiers("bounty accept");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player hunter = (Player) sender;
        String hunterName = hunter.getName();

        BountyManager bountyMngr = plugin.getBountyManager();
        Player target;
        if (bountyMngr.usesAnonymousTargets()) {
            try {
                int num = Integer.parseInt(args[0]) - 1;
                if (num < 0 || num >= bountyMngr.getBounties().size()) {
                    num = 0;
                }
                target = Bukkit.getPlayer(bountyMngr.getBounties().get(num).getTarget());
            } catch (NumberFormatException e) {
                Messaging.send(sender, "Bounty not found.");
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
        }
        if (target == null) {
            Messaging.send(hunter, "Player not found.");
            return true;
        }

        if (!bountyMngr.isTarget(target)) {
            Messaging.send(hunter, "There is no bounty on $1.", target.getName());
            return true;
        }

        if (target.equals(hunter)) {
            Messaging.send(hunter, "You cannot accept a bounty on yourself.");
            return true;
        }

        Bounty bounty = bountyMngr.getBountyOn(target);
        if (bounty.isHunter(hunter)) {
            Messaging.send(hunter, "You are already pursuing this bounty.");
            return true;
        }

        if (bounty.isOwner(hunter)) {
            Messaging.send(hunter, "You cannot accept a bounty you have issued.");
            return true;
        }

        if (!HeroBounty.permission.playerHas(hunter, "herobounty.accept")) {
            Messaging.send(hunter, "You don't have permission to accept bounties.");
            return true;
        }

        int contractFee = bounty.getContractFee();
        if (HeroBounty.economy.getBalance(hunterName) < contractFee) {
            Messaging.send(hunter, "You don't have enough funds.");
            return true;
        }

        bounty.addHunter(hunter);
        HeroBounty.economy.withdrawPlayer(hunterName, contractFee);
        bounty.setExpiration(hunter, bountyMngr.getDuration());
        String expiration = bountyMngr.getBountyExpirationString();

        Messaging.send(hunter, "Bounty accepted. You have been charged $1.", HeroBounty.economy.format(contractFee));
        Messaging.send(hunter, "Your target is $1. This bounty expires in $2.", bounty.getTargetDisplayName(), expiration);

        Player owner = plugin.getServer().getPlayer(bounty.getOwner());
        if (owner != null) {
            Messaging.send(owner, "Your bounty on $1 has been accepted by $2.", bounty.getTargetDisplayName(), hunter.getDisplayName());
        }

        plugin.saveData();
        return true;
    }

}
