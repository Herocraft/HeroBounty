package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class CancelCommand extends BaseCommand {

    public CancelCommand(HeroBounty plugin) {
        super(plugin);
        name = "Cancel";
        description = "Cancels a previously posted bounty";
        usage = "§e/bounty cancel §9<target>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty cancel");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player owner = (Player) sender;
            String ownerName = owner.getName();
            
            BountyManager bountyMngr = plugin.getBountyManager();
            
            if (!bountyMngr.isTarget(args[0])) {
                Messaging.send(owner, "Bounty not found.");
                return;
            }
            
            Bounty bounty = bountyMngr.getBountyOn(args[0]);
            if (!bounty.isOwner(owner)) {
                Messaging.send(owner, "You don't own this bounty.");
                return;
            }
            
            int value = bounty.getValue();
            bountyMngr.removeBounty(bounty);
            bountyMngr.sortBounties();
            
            HeroBounty.economy.depositPlayer(ownerName, value);
            Messaging.send(owner, "You have been reimbursed $1 for your bounty.", HeroBounty.economy.format(value));

            List<String> hunters = bounty.getHunters();
            if (!hunters.isEmpty()) {
                int inconvenience = (int) Math.floor((double) bounty.getPostingFee() / hunters.size());
                for (String hunterName : bounty.getHunters()) {
                    if (plugin.getBountyManager().shouldPayInconvenience()) {
                        HeroBounty.economy.depositPlayer(hunterName, inconvenience);
                    }

                    Player hunter = plugin.getServer().getPlayer(hunterName);
                    if (hunter != null) {
                        Messaging.send(hunter, "The bounty on $1 has been cancelled.", bounty.getTargetDisplayName());
                        Messaging.send(hunter, "Your contract fee has been refunded.");
                        if (plugin.getBountyManager().shouldPayInconvenience() && inconvenience > 0) {
                            Messaging.send(hunter, "You have received $1 for the inconvenience.", HeroBounty.economy.format(inconvenience));
                        }
                    }

                }
            }
            
            plugin.saveData();
        }
    }

}
