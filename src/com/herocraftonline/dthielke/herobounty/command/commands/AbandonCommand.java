package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class AbandonCommand extends BaseCommand {

    public AbandonCommand(HeroBounty plugin) {
        super(plugin);
        name = "Abandon";
        description = "Abandons a previously accepted bounty";
        usage = "§e/bounty abandon §9<id#>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty abandon");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();
            List<Bounty> acceptedBounties = plugin.getBountyManager().listBountiesAcceptedBy(hunterName);
            int id = BountyManager.parseBountyId(args[0], acceptedBounties);
            if (id != -1) {
                acceptedBounties.get(id).removeHunter(hunterName);
                Messaging.send(plugin, hunter, "Bounty abandoned.");
                plugin.saveData();
            } else {
                Messaging.send(plugin, hunter, "Bounty not found.");
            }
        }
    }

}
