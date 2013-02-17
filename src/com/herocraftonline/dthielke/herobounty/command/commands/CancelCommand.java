package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BasicInteractiveCommand;
import com.herocraftonline.dthielke.herobounty.command.BasicInteractiveCommandState;
import com.herocraftonline.dthielke.herobounty.command.InteractiveCommandState;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Hashtable;
import java.util.List;

public class CancelCommand extends BasicInteractiveCommand {
    private Hashtable<CommandSender, Bounty> pendingCancellations = new Hashtable<CommandSender, Bounty>();
    private final HeroBounty plugin;

    public CancelCommand(HeroBounty plugin) {
        super("Cancel");
        setDescription("Cancels a previously posted bounty");
        setUsage("§e/bounty cancel §9<target>");
        this.plugin = plugin;
        this.setStates(new InteractiveCommandState[] { new StateA(), new StateB() });
    }

    @Override
    public String getCancelIdentifier() {
        return "bounty cancel abort";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) {
        if (executor instanceof Player) {
            pendingCancellations.remove(executor);
        }
    }

    class StateA extends BasicInteractiveCommandState {
        
        public StateA() {
            super("bounty cancel");
            setArgumentRange(1, 1);
        }

        @Override
        public boolean execute(CommandSender executor, String identifier, String[] args) {
            if (executor instanceof Player) {
                Player owner = (Player) executor;

                BountyManager bountyMngr = plugin.getBountyManager();

                if (!bountyMngr.isTarget(args[0])) {
                    Messaging.send(owner, "Bounty not found.");
                    return false;
                }

                Bounty bounty = bountyMngr.getBountyOn(args[0]);
                if (!bounty.isOwner(owner)) {
                    Messaging.send(owner, "You don't own this bounty.");
                    return false;
                }

                int cancellationFee = (int) ((bounty.getValue() + bounty.getPostingFee()) * bountyMngr.getCancellationFee());

                if (cancellationFee > 0) {
                    Messaging.send(owner, "You will be charged with cancellation fee of $1.", HeroBounty.economy.format(cancellationFee));
                }
                pendingCancellations.put(owner, bounty);
                Messaging.send(owner, "Please §8/bounty cancel confirm §7or §8/bounty cancel abort §7this cancellation.");
                return true;
            }
            return false;
        }
    }

    class StateB extends BasicInteractiveCommandState {
        
        public StateB() {
            super("bounty cancel confirm");
            setArgumentRange(0, 0);
        }

        public boolean execute(CommandSender sender, String identifier, String[] args) {
            if (sender instanceof Player) {
                Player owner = (Player) sender;
                String ownerName = owner.getName();
                Bounty bounty = pendingCancellations.get(owner);

                BountyManager bountyMngr = plugin.getBountyManager();
                bountyMngr.removeBounty(bounty);
                bountyMngr.sortBounties();

                double returnAmount = bounty.getValue() - (bounty.getValue() + bounty.getPostingFee()) * bountyMngr.getCancellationFee();
                HeroBounty.economy.depositPlayer(ownerName, returnAmount);
                Messaging.send(owner, "You have been reimbursed $1 for your bounty.", HeroBounty.economy.format(returnAmount));

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
                return true;
            }
            return false;
        }
    }
}
