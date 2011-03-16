package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.nijiko.coelho.iConomy.iConomy;

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
            String senderName = ((Player) sender).getName();
            List<Bounty> bounties = plugin.getBounties();

            int perPage = 7;
            int currentPage;
            if (args.length == 0) {
                currentPage = 0;
            } else {
                try {
                    currentPage = (args[0] == null) ? 0 : Integer.valueOf(args[0]);
                } catch(NumberFormatException e) {
                    currentPage = 0;
                }
            }
            currentPage = (currentPage == 0) ? 1 : currentPage;
            int numPages = (int) Math.ceil(bounties.size() / perPage) + 1;
            int pageStart = (currentPage - 1) * perPage;
            int pageEnd = pageStart + perPage - 1;
            pageEnd = (pageEnd >= bounties.size()) ? bounties.size() - 1 : pageEnd;

            if (bounties.isEmpty()) {
                sender.sendMessage(plugin.getTag() + "§cNo bounties currently listed.");
            } else if (currentPage > numPages) {
                sender.sendMessage(plugin.getTag() + "§cInvalid page number.");
            } else {
                sender.sendMessage("§cAvailable Bounties (Page §f#" + currentPage + "§c of §f" + numPages + "§c):");
                for (int i = pageStart; i <= pageEnd; i++) {
                    Bounty b = bounties.get(i);
                    String msg = "§f" + (i + 1) + ". §e";
                    if (!plugin.isUsingAnonymousTargets()) {
                        msg += b.getTarget() + "§f - §e";
                    }
                    msg += iConomy.getBank().format(b.getValue()) + "§f - §eFee: " + iConomy.getBank().format(b.getContractFee());
                    if (senderName.equalsIgnoreCase(b.getOwner())) {
                        msg += "§7 (posted by you)";
                    }
                    sender.sendMessage(msg);
                }
            }
        }
    }

}
