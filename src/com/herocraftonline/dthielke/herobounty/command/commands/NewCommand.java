package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class NewCommand extends BaseCommand {

    public NewCommand(HeroBounty plugin) {
        super(plugin);
        name = "New";
        description = "Creates a new bounty for a fee";
        usage = "/ch locate";
        minArgs = 0;
        maxArgs = 0;
        identifiers.add("bounty new");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player owner = (Player) sender;
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target != null) {
                String targetName = target.getName();
                if (target != owner) {
                    List<Bounty> bounties = plugin.getBounties();
                    for (Bounty b : bounties) {
                        if (b.getTarget().equalsIgnoreCase(targetName)) {
                            owner.sendMessage(plugin.getTag() + "§cThere is already a bounty on §f" + targetName + "§c.");
                            return;
                        }
                    }

                    int value;
                    try {
                        value = Integer.parseInt(args[1]);
                        if (value < plugin.getBountyMin()) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        owner.sendMessage(plugin.getTag() + "§cValue must be a number greater than §f" + plugin.getBountyMin() + "§c.");
                        return;
                    }
                    Account ownerAccount = iConomy.getBank().getAccount(owner.getName());
                    double balance = ownerAccount.getBalance();
                    if (balance >= value) {
                        int postingFee = (int) (plugin.getBountyFeePercent() * value);
                        int award = value - postingFee;
                        int contractFee = (int) (plugin.getContractFeePercent() * award);
                        int deathPenalty = (int) (plugin.getDeathPenaltyPercent() * award);

                        Bounty bounty = new Bounty(owner.getName(), owner.getDisplayName(), targetName, target.getDisplayName(), award, postingFee, contractFee, deathPenalty);
                        bounties.add(bounty);
                        Collections.sort(bounties);

                        ownerAccount.subtract(value);
                        owner.sendMessage(plugin.getTag() + "§cPlaced a bounty on §e" + targetName + "§c's head for §e" + iConomy.getBank().format(award) + "§c.");
                        owner.sendMessage(plugin.getTag() + "§cYou have been charged a §e" + iConomy.getBank().format(postingFee) + "§c fee for posting this bounty.");
                        plugin.getServer().broadcastMessage(plugin.getTag() + "§eA new bounty has been placed for §f" + iConomy.getBank().format(award) + "§e.");
                        
                        plugin.saveData();
                    } else {
                        owner.sendMessage(plugin.getTag() + "§cYou don't have enough funds to do that.");
                    }
                } else {
                    owner.sendMessage(plugin.getTag() + "§cYou cannot place a bounty on yourself.");
                }
            } else {
                owner.sendMessage(plugin.getTag() + "§cTarget player not found.");
            }
        }
    }

}
