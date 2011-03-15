package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class LocateCommand extends BaseCommand {

    public LocateCommand(HeroBounty plugin) {
        super(plugin);
        name = "Locate";
        description = "Shows approximate locations of tracked targets";
        usage = "/ch locate";
        minArgs = 0;
        maxArgs = 0;
        identifiers.add("bounty locate");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();

            List<Bounty> acceptedBounties = plugin.listBountiesAcceptedByPlayer(hunterName);
            if (acceptedBounties.isEmpty()) {
                hunter.sendMessage(plugin.getTag() + "§cYou currently have no accepted bounties.");
            } else {
                hunter.sendMessage("§cLast Known Target Locations: (x, z)");
                int locationRounding = plugin.getLocationRounding();
                for (int i = 0; i < acceptedBounties.size(); i++) {
                    Bounty b = acceptedBounties.get(i);
                    Player target = plugin.getServer().getPlayer(b.getTarget());
                    if (target == null) {
                        Messaging.send(hunter, "§f" + (i + 1) + ". §e" + b.getTarget() + ": offline");
                    } else {
                        Location loc = target.getLocation();
                        int x = loc.getBlockX();
                        int z = loc.getBlockZ();
                        x = (int) (Math.round(x / (float) locationRounding) * locationRounding);
                        z = (int) (Math.round(z / (float) locationRounding) * locationRounding);
                        Messaging.send(hunter, "§f" + (i + 1) + ". §e" + b.getTarget() + ": (" + x + ", " + z + ")");
                    }
                }
            }
        }
    }

}
