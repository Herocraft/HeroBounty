package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;

public class AcceptCommand extends BaseCommand {

    public AcceptCommand(HeroBounty plugin) {
        super(plugin);
        name = "Accept";
        description = "Accepts a posted bounty for a small contract fee";
        usage = "§e/bounty accept §9<id#>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty accept");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();
            List<Bounty> bounties = plugin.getBountyManager().getBounties();
            int id = BountyManager.parseBountyId(args[0], bounties);
            if (id != -1) {
                Bounty bounty = bounties.get(id);
                if (!bounty.getOwner().equals(hunterName)) {
                    if (!bounty.getTarget().equals(hunterName)) {
                        if (!bounty.isHunter(hunterName)) {
                            if (plugin.getPermissions().canAcceptBounty(hunter)) {
                                Method register = plugin.getRegister();
                                MethodAccount hunterAccount = register.getAccount(hunterName);

                                int contractFee = bounty.getContractFee();
                                if (hunterAccount.hasEnough(contractFee)) {
                                    bounty.addHunter(hunterName);
                                    hunterAccount.subtract(contractFee);

                                    GregorianCalendar expiration = new GregorianCalendar();
                                    int bountyDuration = plugin.getBountyManager().getDuration();
                                    expiration.add(Calendar.MINUTE, bountyDuration);
                                    bounty.getExpirations().put(hunterName, expiration.getTime());

                                    int bountyRelativeTime = (bountyDuration < 60) ? bountyDuration : (bountyDuration < (60 * 24)) ? bountyDuration / 60 : (bountyDuration < (60 * 24 * 7)) ? bountyDuration / (60 * 24) : bountyDuration / (60 * 24 * 7);
                                    String bountyRelativeAmount = (bountyDuration < 60) ? " minutes" : (bountyDuration < (60 * 24)) ? " hours" : (bountyDuration < (60 * 24 * 7)) ? " days" : " weeks";

                                    Messaging.send(plugin, hunter, "Bounty accepted. You have been charged $1.", register.format(contractFee));
                                    Messaging.send(plugin, hunter, "Your target is $1. This bounty expires in $2.", bounty.getTargetDisplayName(), bountyRelativeTime + bountyRelativeAmount);

                                    Player owner = plugin.getServer().getPlayer(bounty.getOwner());
                                    if (owner != null) {
                                        Messaging.send(plugin, owner, "Your bounty on $1 has been accepted by $2.", bounty.getTargetDisplayName(), hunter.getDisplayName());
                                    }

                                    plugin.saveData();
                                } else {
                                    Messaging.send(plugin, hunter, "You don't have enough funds.");
                                }
                            } else {
                                Messaging.send(plugin, hunter, "You don't have permission to accept bounties.");
                            }
                        } else {
                            Messaging.send(plugin, hunter, "You have already accepted this bounty.");
                        }
                    } else {
                        Messaging.send(plugin, hunter, "You can't accept this bounty.");
                    }
                } else {
                    Messaging.send(plugin, hunter, "You can't accept your own bounty.");
                }
            } else {
                Messaging.send(plugin, hunter, "Bounty not found.");
            }
        }
    }

}
