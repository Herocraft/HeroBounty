package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class ViewCommand extends BaseCommand {

    public ViewCommand(HeroBounty plugin) {
        super(plugin);
        name = "View";
        description = "Shows a list of bounties you have accepted";
        usage = "§e/bounty view";
        minArgs = 0;
        maxArgs = 0;
        identifiers.add("bounty view");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {

            Player hunter = (Player) sender;
            String hunterName = hunter.getName();

            List<Bounty> acceptedBounties = plugin.getBountyManager().getBountiesAcceptedBy(hunterName);
            if (acceptedBounties.isEmpty()) {
                Messaging.send(hunter, "You currently have no accepted bounties.");
            } else {
                hunter.sendMessage("§cAccepted Bounties:");
                for (int i = 0; i < acceptedBounties.size(); i++) {
                    Bounty bounty = acceptedBounties.get(i);
                    int bountyDuration = bounty.getMinutesLeft(hunterName);
                    String bountyExpiration = BountyManager.getBountyExpirationString(bountyDuration);
                    hunter.sendMessage("§f" + (i + 1) + ". §e" + bounty.getTarget() + " - " + HeroBounty.economy.format(bounty.getValue()) + " - " + bountyExpiration);
                }
            }
        }
    }

}
