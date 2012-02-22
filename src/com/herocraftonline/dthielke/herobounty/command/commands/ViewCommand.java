package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BasicCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ViewCommand extends BasicCommand {
    private final HeroBounty plugin;

    public ViewCommand(HeroBounty plugin) {
        super("View");
        setDescription("Shows a list of bounties you have accepted");
        setUsage("§e/bounty view");
        setArgumentRange(0, 0);
        setIdentifiers("bounty view");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
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
        return true;
    }

}
