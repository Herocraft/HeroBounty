package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class LocateCommand extends BaseCommand {

    public LocateCommand(HeroBounty plugin) {
        super(plugin);
        name = "Locate";
        description = "Shows approximate locations of tracked targets";
        usage = "§e/bounty locate §8[target]";
        minArgs = 0;
        maxArgs = 1;
        identifiers.add("bounty locate");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();

            BountyManager bountyMngr = plugin.getBountyManager();

            if (!HeroBounty.permission.playerHas(hunter, "herobounty.locate")) {
                Messaging.send(hunter, "You don't have permission to locate targets.");
                return;
            }

            List<Bounty> acceptedBounties = bountyMngr.getBountiesAcceptedBy(hunterName);
            int locationRounding = bountyMngr.getLocationRounding();
            if (acceptedBounties.isEmpty()) {
                Messaging.send(hunter, "You currently have no accepted bounties.");
            } else if (args.length == 1) {
                if (!bountyMngr.isTarget(args[0])) {
                    Messaging.send(hunter, "Bounty not found.");
                    return;
                }

                Player target = plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    Messaging.send(hunter, "Target is offline.");
                    return;
                }

                Location loc = roundLocation(target.getLocation(), locationRounding);
                hunter.setCompassTarget(loc);
                Messaging.send(hunter, "Compass now points near $1.", target.getDisplayName());
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
