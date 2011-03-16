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

public class CancelCommand extends BaseCommand {

    public CancelCommand(HeroBounty plugin) {
        super(plugin);
        name = "Cancel";
        description = "Cancels a previously posted bounty";
        usage = "§e/bounty cancel §9<id#>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty cancel");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player owner = (Player) sender;
            String ownerName = owner.getName();
            List<Bounty> bounties = plugin.getBounties();
            int id = HeroBounty.parseBountyId(args[0], bounties);
            if (id != -1) {
                Bounty bounty = bounties.get(id);
                int value = bounty.getValue();
                if (bounty.getOwner().equals(ownerName)) {
                    bounties.remove(bounty);
                    Collections.sort(bounties);

                    Account ownerAccount = iConomy.getBank().getAccount(ownerName);
                    ownerAccount.add(value);
                    owner.sendMessage(plugin.getTag() + "§cYou have been reimbursed §e" + iConomy.getBank().format(value) + " §cfor your bounty.");

                    List<String> hunters = bounty.getHunters();
                    if (!hunters.isEmpty()) {
                        int inconvenience = (int) Math.floor((double) bounty.getPostingFee() / hunters.size());
                        for (String hunterName : bounty.getHunters()) {
                            Account hunterAccount = iConomy.getBank().getAccount(hunterName);
                            hunterAccount.add(bounty.getContractFee());

                            if (plugin.shouldPayInconvenience()) {
                                hunterAccount.add(inconvenience);
                            }

                            Player hunter = plugin.getServer().getPlayer(hunterName);
                            if (hunter != null) {
                                hunter.sendMessage(plugin.getTag() + "§cThe bounty you were pursuing targetting §e" + bounty.getTarget() + " §chas been cancelled.");
                                hunter.sendMessage(plugin.getTag() + "§cYou have been reimbursed your contract fee.");
                                if (plugin.shouldPayInconvenience() && inconvenience > 0) {
                                    hunter.sendMessage(plugin.getTag() + "§cYou have received §e" + iConomy.getBank().format(inconvenience) + " §cfor the inconvenience.");
                                }
                            }

                        }
                    }
                    plugin.saveData();
                } else {
                    owner.sendMessage(plugin.getTag() + "§cYou can only cancel bounties you created.");
                }
            } else {
                owner.sendMessage(plugin.getTag() + "§cBounty not found.");
            }
        }
    }

}
