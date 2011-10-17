package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class AbandonCommand extends BaseCommand {

    public AbandonCommand(HeroBounty plugin) {
        super(plugin);
        name = "Abandon";
        description = "Abandons a previously accepted bounty";
        usage = "§e/bounty abandon §9<target>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty abandon");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();

            List<Bounty> acceptedBounties = plugin.getBountyManager().getBountiesAcceptedBy(hunterName);
            Bounty bounty = null;

            for (Bounty b : acceptedBounties) {
                if (b.getTarget().equalsIgnoreCase(args[0])) {
                    bounty = b;
                    break;
                }
            }

            if (bounty == null) {
                Messaging.send(hunter, "Bounty not found.");
                return;
            }

            bounty.removeHunter(hunter);
            Messaging.send(hunter, "Bounty abandoned.");
            plugin.saveData();
        }
    }

}
