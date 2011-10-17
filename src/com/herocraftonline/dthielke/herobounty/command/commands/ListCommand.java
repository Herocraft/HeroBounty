package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class ListCommand extends BaseCommand {

    public ListCommand(HeroBounty plugin) {
        super(plugin);
        name = "List";
        description = "Lists available bounties";
        usage = "§e/bounty list §8[page#]";
        minArgs = 0;
        maxArgs = 1;
        identifiers.add("bounty list");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (HeroBounty.permission.playerHas(player, "herobounty.list")) {
                String senderName = player.getName();
                List<Bounty> bounties = new ArrayList<Bounty>(plugin.getBountyManager().getBounties());
                for (Iterator<Bounty> iter = bounties.iterator(); iter.hasNext();) {
                    if (plugin.getServer().getPlayer(iter.next().getTarget()) == null)
                        iter.remove();
                }

                int perPage = 7;
                int currentPage;
                if (args.length == 0) {
                    currentPage = 0;
                } else {
                    try {
                        currentPage = (args[0] == null) ? 0 : Integer.valueOf(args[0]);
                    } catch (NumberFormatException e) {
                        currentPage = 0;
                    }
                }
                currentPage = (currentPage == 0) ? 1 : currentPage;
                int numPages = (int) Math.ceil(bounties.size() / perPage) + 1;
                int pageStart = (currentPage - 1) * perPage;
                int pageEnd = pageStart + perPage - 1;
                pageEnd = (pageEnd >= bounties.size()) ? bounties.size() - 1 : pageEnd;

                if (bounties.isEmpty()) {
                    Messaging.send(sender, "No bounties currently listed.");
                } else if (currentPage > numPages) {
                    Messaging.send(sender, "Invalid page number.");
                } else {
                    sender.sendMessage("§cAvailable Bounties (Page §f#" + currentPage + "§c of §f" + numPages + "§c):");
                    for (int i = pageStart; i <= pageEnd; i++) {
                        Bounty b = bounties.get(i);
                        String msg = "§f" + (i + 1) + ". §e";
                        if (!plugin.getBountyManager().usesAnonymousTargets()) {
                            msg += b.getTarget() + "§f - §e";
                        }
                        msg += HeroBounty.economy.format(b.getValue()) + "§f - §eFee: " + HeroBounty.economy.format(b.getContractFee());
                        if (senderName.equalsIgnoreCase(b.getOwner())) {
                            msg += "§7 (posted by you)";
                        }
                        sender.sendMessage(msg);
                    }
                }
            } else {
                Messaging.send(player, "You don't have permission to use this command.");
            }
        }
    }

}