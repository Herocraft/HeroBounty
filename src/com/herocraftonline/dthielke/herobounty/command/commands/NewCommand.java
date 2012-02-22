package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.command.BasicCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class NewCommand extends BasicCommand {
    private final HeroBounty plugin;

    public NewCommand(HeroBounty plugin) {
        super("New");
        setDescription("Creates a new bounty for a fee");
        setUsage("§e/bounty new §9<target> <value>");
        setArgumentRange(2, 2);
        setIdentifiers("bounty new");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (sender instanceof Player) {
            Player owner = (Player) sender;
            String ownerName = owner.getName();
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target != null) {
                String targetName = target.getName();
                if (target != owner) {
                    if (HeroBounty.permission.playerHas(owner, "herobounty.new")) {
                        if (!HeroBounty.permission.playerHas(target, "herobounty.untargettable")) {
                            List<Bounty> bounties = plugin.getBountyManager().getBounties();
                            for (Bounty b : bounties) {
                                if (b.getTarget().equalsIgnoreCase(targetName)) {
                                    Messaging.send(owner, "There is already a bounty on $1.", targetName);
                                    return true;
                                }
                            }

                            int value;
                            try {
                                value = Integer.parseInt(args[1]);
                                if (value < plugin.getBountyManager().getMinimumValue()) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException e) {
                                Messaging.send(owner, "Value must be greater than $1.", String.valueOf(plugin.getBountyManager().getMinimumValue()));
                                return true;
                            }
                            if (HeroBounty.economy.getBalance(ownerName) >= value) {
                                int postingFee = (int) (plugin.getBountyManager().getPlacementFee() * value);
                                int award = value - postingFee;
                                int contractFee = (int) (plugin.getBountyManager().getContractFee() * award);
                                int deathPenalty = (int) (plugin.getBountyManager().getDeathFee() * award);

                                Bounty bounty = new Bounty(ownerName, owner.getDisplayName(), targetName, target.getDisplayName(), award, postingFee, contractFee, deathPenalty);
                                bounties.add(bounty);
                                Collections.sort(bounties);

                                HeroBounty.economy.withdrawPlayer(ownerName, value);
                                Messaging.send(owner, "Placed a bounty on $1's head for $2.", targetName, HeroBounty.economy.format(award));
                                Messaging.send(owner, "You have been charged $1 for posting this bounty.", HeroBounty.economy.format(postingFee));
                                Messaging.broadcast("A new bounty has been placed for $1.", HeroBounty.economy.format(award));

                                plugin.saveData();
                            } else {
                                Messaging.send(owner, "You don't have enough funds to do that.");
                            }
                        } else {
                            Messaging.send(owner, "This player can't be targetted.");
                        }
                    } else {
                        Messaging.send(owner, "You don't have permission to create bounties.");
                    }
                } else {
                    Messaging.send(owner, "You can't place a bounty on yourself.");
                }
            } else {
                Messaging.send(owner, "Target player not found.");
            }
        }
        return true;
    }

}
