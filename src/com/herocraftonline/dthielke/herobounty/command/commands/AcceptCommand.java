package com.herocraftonline.dthielke.herobounty.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class AcceptCommand extends BaseCommand {

    public AcceptCommand(HeroBounty plugin) {
        super(plugin);
        name = "Accept";
        description = "Accepts a posted bounty for a small contract fee";
        usage = "§e/bounty accept §9<target>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty accept");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return;

        Player hunter = (Player) sender;
        String hunterName = hunter.getName();

        BountyManager bountyMngr = plugin.getBountyManager();
        Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            Messaging.send(hunter, "Player not found.");
            return;
        }

        if (!bountyMngr.isTarget(target)) {
            Messaging.send(hunter, "There is no bounty on $1.", target.getName());
            return;
        }

        if (target.equals(hunter)) {
            Messaging.send(hunter, "You cannot accept a bounty on yourself.");
            return;
        }

        Bounty bounty = bountyMngr.getBountyOn(target);
        if (bounty.isHunter(hunter)) {
            Messaging.send(hunter, "You are already pursuing this bounty.");
            return;
        }

        if (bounty.isOwner(hunter)) {
            Messaging.send(hunter, "You cannot accept a bounty you have issued.");
            return;
        }

        if (!HeroBounty.permission.playerHas(hunter, "herobounty.accept")) {
            Messaging.send(hunter, "You don't have permission to accept bounties.");
            return;
        }

        int contractFee = bounty.getContractFee();
        if (HeroBounty.economy.getBalance(hunterName) < contractFee) {
            Messaging.send(hunter, "You don't have enough funds.");
            return;
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
    }

}
