package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.command.BasicInteractiveCommand;
import com.herocraftonline.dthielke.herobounty.command.BasicInteractiveCommandState;
import com.herocraftonline.dthielke.herobounty.command.InteractiveCommandState;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class PlaceCommand extends BasicInteractiveCommand {
    private final Hashtable<CommandSender, Bounty> pendingBounties = new Hashtable<CommandSender, Bounty>();
    private final HeroBounty plugin;

    public PlaceCommand(HeroBounty plugin) {
        super("Place");
        setDescription("Places a bounty for a fee");
        setUsage("§e/bounty place §9<target> <value>");
        this.plugin = plugin;
        this.setStates(new InteractiveCommandState[] { new StateA(), new StateB() });
    }

    @Override
    public String getCancelIdentifier() {
        return "bounty place abort";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) {
        if (executor instanceof Player) {
            pendingBounties.remove(executor);
        }
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("bounty place");
            setArgumentRange(2, 2);
        }

        @Override
        public boolean execute(CommandSender executor, String identifier, String[] args) {
            if (executor instanceof Player) {
                Player owner = (Player) executor;
                String ownerName = owner.getName();
                Player target = plugin.getServer().getPlayer(args[0]);
                if (target != null) {
                    String targetName = target.getName();
                    if (target != owner) {
                        if (HeroBounty.permission.playerHas(owner, "herobounty.new") || HeroBounty.permission.playerHas(owner, "herobounty.place")) {
                            if (!HeroBounty.permission.playerHas(target, "herobounty.untargettable")) {
                                List<Bounty> bounties = plugin.getBountyManager().getBounties();
                                for (Bounty b : bounties) {
                                    if (b.getTarget().equalsIgnoreCase(targetName)) {
                                        Messaging.send(owner, "There is already a bounty on $1.", targetName);
                                        return false;
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
                                    return false;
                                }
                                if (HeroBounty.economy.getBalance(ownerName) >= value) {
                                    int postingFee = (int) (plugin.getBountyManager().getPlacementFee() * value);
                                    int award = value - postingFee;
                                    int contractFee = (int) (plugin.getBountyManager().getContractFee() * award);
                                    int deathPenalty = (int) (plugin.getBountyManager().getDeathFee() * award);

                                    Bounty bounty = new Bounty(ownerName, owner.getDisplayName(), targetName, target.getDisplayName(), award, postingFee, contractFee, deathPenalty);
                                    pendingBounties.put(executor, bounty);
                                    int cancellationFee = (int) (plugin.getBountyManager().getCancellationFee() * value);
                                    if (cancellationFee > 0) {
                                        Messaging.send(executor, "This bounty has a cancellation fee of $1", HeroBounty.economy.format(cancellationFee));
                                    }
                                    Messaging.send(executor, "Please §8/bounty place confirm §7or §8/bounty place abort §7this placement.");
                                    return true;
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
            return false;
        }
    }

    class StateB extends BasicInteractiveCommandState {

        public StateB() {
            super("bounty place confirm");
            this.setArgumentRange(0, 0);
        }

        @Override
        public boolean execute(CommandSender executor, String identifier, String [] args) {
            if (executor instanceof Player) {
                List<Bounty> bounties = plugin.getBountyManager().getBounties();
                Bounty bounty = pendingBounties.remove(executor);
                int totalValue = bounty.getValue() + bounty.getPostingFee(); 

                if (HeroBounty.economy.getBalance(bounty.getOwner()) >= totalValue) {
                    bounties.add(bounty);
                    Collections.sort(bounties);
    
                    HeroBounty.economy.withdrawPlayer(bounty.getOwner(), totalValue);
                    Messaging.send(executor, "Placed a bounty on $1's head for $2.", bounty.getTarget(), HeroBounty.economy.format(bounty.getValue()));
                    Messaging.send(executor, "You have been charged $1 for posting this bounty.", HeroBounty.economy.format(bounty.getPostingFee()));
                    Messaging.broadcast("A new bounty has been placed for $1.", HeroBounty.economy.format(bounty.getValue()));
    
                    plugin.saveData();
                    return true;
                }
                else {
                    Messaging.send(executor, "You don't have enough funds to do that.");
                    cancelInteraction(executor);
                }
            }
            return false;
        }
    }
}
