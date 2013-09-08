package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.command.BasicCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AbandonCommand extends BasicCommand {
    private final HeroBounty plugin;

    public AbandonCommand(HeroBounty plugin) {
        super("Abandon");
        setDescription("Abandons a previously accepted bounty");
        setUsage("§e/bounty abandon §9<target>");
        setArgumentRange(1, 1);
        setIdentifiers("bounty abandon");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
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
                return true;
            }

            bounty.removeHunter(hunter);
            Messaging.send(hunter, "Bounty abandoned.");
            plugin.saveData();
        }

        return true;
    }

}
