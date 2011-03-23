package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class LocateCommand extends BaseCommand {

    public LocateCommand(HeroBounty plugin) {
        super(plugin);
        name = "Locate";
        description = "Shows approximate locations of tracked targets";
        usage = "§e/bounty locate §8[id#]";
        minArgs = 0;
        maxArgs = 1;
        identifiers.add("bounty locate");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();

            if (plugin.getPermissionManager().canLocateTargets(hunter)) {
                List<Bounty> acceptedBounties = plugin.getBountyManager().listBountiesAcceptedBy(hunterName);
                int locationRounding = plugin.getBountyManager().getLocationRounding();
                if (acceptedBounties.isEmpty()) {
                    Messaging.send(plugin, hunter, "You currently have no accepted bounties.");
                } else if (args.length == 1) {
                    int id = BountyManager.parseBountyId(args[0], acceptedBounties);
                    if (id != -1) {
                        Bounty b = acceptedBounties.get(id);
                        Player target = plugin.getServer().getPlayer(b.getTarget());
                        if (target != null) {
                            Location loc = roundLocation(target.getLocation(), locationRounding);
                            hunter.setCompassTarget(loc);
                            Messaging.send(plugin, hunter, "Compass now points near $1.", target.getDisplayName());
                        } else {
                            Messaging.send(plugin, hunter, "Target is offline.");
                        }
                    } else {
                        Messaging.send(plugin, hunter, "Invalid bounty id#.");
                    }
                } else {
                    hunter.sendMessage("§cLast Known Target Locations: (x, z)");
                    for (int i = 0; i < acceptedBounties.size(); i++) {
                        Bounty b = acceptedBounties.get(i);
                        Player target = plugin.getServer().getPlayer(b.getTarget());
                        if (target == null) {
                            hunter.sendMessage("§f" + (i + 1) + ". §e" + b.getTarget() + ": offline");
                        } else {
                            Location loc = roundLocation(target.getLocation(), locationRounding);
                            hunter.sendMessage("§f" + (i + 1) + ". §e" + b.getTarget() + ": (" + loc.getBlockX() + ", " + loc.getBlockZ() + ")");
                        }
                    }
                }
            } else {
                Messaging.send(plugin, hunter, "You don't have permission to use this command.");
            }
        }
    }

    private Location roundLocation(Location loc, int roundTo) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        x = (int) (Math.round(x / (float) roundTo) * roundTo);
        z = (int) (Math.round(z / (float) roundTo) * roundTo);
        return new Location(loc.getWorld(), x, 0, z);
    }

}
