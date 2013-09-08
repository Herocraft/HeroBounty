package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.Bounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BasicCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LocateCommand extends BasicCommand {
    private final HeroBounty plugin;

    public LocateCommand(HeroBounty plugin) {
        super("Locate");
        setDescription("Shows approximate locations of tracked targets");
        setUsage("§e/bounty locate §8[target]");
        setArgumentRange(0, 1);
        setIdentifiers("bounty locate");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();

            BountyManager bountyMngr = plugin.getBountyManager();

            if (!HeroBounty.permission.playerHas(hunter, "herobounty.locate")) {
                Messaging.send(hunter, "You don't have permission to locate targets.");
                return true;
            }

            List<Bounty> acceptedBounties = bountyMngr.getBountiesAcceptedBy(hunterName);
            int locationRounding = bountyMngr.getLocationRounding();
            if (acceptedBounties.isEmpty()) {
                Messaging.send(hunter, "You currently have no accepted bounties.");
            } else if (args.length == 1) {
                if (!bountyMngr.isTarget(args[0])) {
                    Messaging.send(hunter, "Bounty not found.");
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    Messaging.send(hunter, "Target is offline.");
                    return true;
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
        return true;
    }

    private Location roundLocation(Location loc, int roundTo) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        x = (int) (Math.round(x / (float) roundTo) * roundTo);
        z = (int) (Math.round(z / (float) roundTo) * roundTo);
        return new Location(loc.getWorld(), x, 0, z);
    }

}
