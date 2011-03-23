package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.EconomyManager;
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

            List<Bounty> acceptedBounties = plugin.getBountyManager().listBountiesAcceptedBy(hunterName);
            if (acceptedBounties.isEmpty()) {
                Messaging.send(plugin, hunter, "You currently have no accepted bounties.");
            } else {
                EconomyManager econ = plugin.getEconomyManager();
                hunter.sendMessage("§cAccepted Bounties:");
                for (int i = 0; i < acceptedBounties.size(); i++) {
                    Bounty bounty = acceptedBounties.get(i);
                    int bountyDuration = bounty.getMinutesLeft(hunterName);
                    int bountyRelativeTime = (bountyDuration < 60) ? bountyDuration : (bountyDuration < (60 * 24)) ? bountyDuration / 60 : (bountyDuration < (60 * 24 * 7)) ? bountyDuration / (60 * 24) : bountyDuration / (60 * 24 * 7);
                    String bountyRelativeAmount = (bountyDuration < 60) ? " minutes" : (bountyDuration < (60 * 24)) ? " hours" : (bountyDuration < (60 * 24 * 7)) ? " days" : " weeks";
                    hunter.sendMessage("§f" + (i + 1) + ". §e" + bounty.getTarget() + " - " + econ.format(bounty.getValue()) + " - " + bountyRelativeTime + bountyRelativeAmount);
                }
            }
        }
    }

}
