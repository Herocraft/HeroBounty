package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class AcceptCommand extends BaseCommand {

    public AcceptCommand(HeroBounty plugin) {
        super(plugin);
        name = "Accept";
        description = "Accepts a posted bounty for a small contract fee";
        usage = "/bounty accept <id#>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty accept");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();
            List<Bounty> bounties = plugin.getBounties();
            int id = HeroBounty.parseBountyId(args[0], bounties);
            Bounty bounty = bounties.get(id);
            if (id != -1) {
                if (!bounty.getOwner().equals(hunterName)) {
                    if (!bounty.getTarget().equals(hunterName)) {
                        if (!bounty.isHunter(hunterName)) {
                            Account hunterAccount = iConomy.getBank().getAccount(hunterName);
                            double balance = hunterAccount.getBalance();
                            int contractFee = bounty.getContractFee();
                            if (balance >= contractFee) {
                                bounty.addHunter(hunterName);
                                
                                GregorianCalendar expiration = new GregorianCalendar();
                                int bountyDuration = plugin.getBountyDuration();
                                expiration.add(Calendar.MINUTE, bountyDuration);
                                bounty.getExpirations().put(hunterName, expiration.getTime());
                                hunterAccount.subtract(contractFee);
                                
                                int bountyRelativeTime = (bountyDuration < 60) ? bountyDuration : (bountyDuration < (60 * 24)) ? bountyDuration / 60 : (bountyDuration < (60 * 24 * 7)) ? bountyDuration / (60 * 24) : bountyDuration / (60 * 24 * 7);
                                String bountyRelativeAmount = (bountyDuration < 60) ? " minutes" : (bountyDuration < (60 * 24)) ? " hours" : (bountyDuration < (60 * 24 * 7)) ? " days" : " weeks";
                                
                                hunter.sendMessage(plugin.getTag() + "§cBounty accepted. You have been charged §f" + iConomy.getBank().format(contractFee) + "§c.");
                                hunter.sendMessage(plugin.getTag() + "§cTarget is §f" + bounty.getTargetDisplayName() + "§c. Bounty expires in §f" + bountyRelativeTime + bountyRelativeAmount + "§c.");
                                
                                Player owner = plugin.getServer().getPlayer(bounty.getOwner());
                                if (owner != null) {
                                    owner.sendMessage(plugin.getTag() + "§cYour bounty on §e" + bounty.getTargetDisplayName() + "§c has been accepted by §e" + hunter.getDisplayName() + "§c.");
                                }
                                
                                plugin.saveData();
                            } else {
                                hunter.sendMessage(plugin.getTag() + "§cYou don't have enough funds.");
                            }
                        } else {
                            hunter.sendMessage(plugin.getTag() + "§cYou have already accepted this bounty.");
                        }
                    } else {
                        hunter.sendMessage(plugin.getTag() + "§cYou cannot accept this bounty.");
                    }
                } else {
                    hunter.sendMessage(plugin.getTag() + "§cYou cannot accept your own bounty.");
                }
            } else {
                hunter.sendMessage(plugin.getTag() + "§cBounty not found.");
            }
        }
    }

}
