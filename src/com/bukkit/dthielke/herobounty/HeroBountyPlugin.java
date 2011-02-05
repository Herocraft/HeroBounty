package com.bukkit.dthielke.herobounty;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.iConomy.iConomy;

public class HeroBountyPlugin extends JavaPlugin {
    private HeroBountyEntityListener entityListener = new HeroBountyEntityListener(this);

    private List<Bounty> bounties;
    private String bountyTag;
    private int bountyMin;
    private float bountyFeePercent;
    private float contractFeePercent;
    private float deathPenaltyPercent;
    private int bountyDuration; // in minutes
    private int locationUpdatePeriod; // in minutes
    private int locationUpdateDistance;
    private boolean anonymousTargets;

    private Timer expirationTimer = new Timer();
    private Timer locationTimer = new Timer();

    public static final int EXPIRATION_TIMER_DELAY = 10000;
    public static final int EXPIRATION_TIMER_PERIOD = 1 * 60 * 1000;
    public static final String[] Colors = new String[] { "&c",
                                                        "&e",
                                                        "&f",
                                                        "&7" };

    public HeroBountyPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGEDBY_PROJECTILE, entityListener, Priority.Normal, this);

        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");

        Configuration config = getConfiguration();

        bountyMin = config.getInt("bounty-min", 20);
        bountyFeePercent = config.getInt("bounty-fee-percent", 10) / 100f;
        contractFeePercent = config.getInt("contract-fee-percent", 5) / 100f;
        deathPenaltyPercent = config.getInt("death-penalty-percent", 5) / 100f;
        bountyDuration = config.getInt("bounty-duration", 24 * 60);
        anonymousTargets = config.getBoolean("anonymous-targets", false);
        bountyTag = config.getString("bounty-tag", "&e[BOUNTY] ");
        locationUpdatePeriod = config.getInt("location-update-period", 20) * 60 * 1000;
        locationUpdateDistance = config.getInt("location-update-distance", 100);

        File file = new File(getDataFolder(), "data.yml");
        bounties = BountyFileHandler.load(file);

        TimerTask expirationChecker = new ExpirationChecker(this);
        expirationTimer.scheduleAtFixedRate(expirationChecker, EXPIRATION_TIMER_DELAY, EXPIRATION_TIMER_PERIOD);
        
        TimerTask locationChecker = new LocationChecker(this);
        locationTimer.scheduleAtFixedRate(locationChecker, 0, locationUpdatePeriod);
    }

    public void saveData() {
        File file = new File(getDataFolder(), "data.yml");
        BountyFileHandler.save(bounties, file);
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length == 0) {
            performShowHelp(sender, args);

            return true;
        }

        String commandName = args[0].toLowerCase();
        String[] trimmedArgs = new String[args.length - 1];

        for (int i = 0; i < args.length - 1; i++)
            trimmedArgs[i] = args[i + 1];

        if (commandName.equals("help"))
            performShowHelp(sender, trimmedArgs);
        else if (commandName.equals("new"))
            performNewBounty(sender, trimmedArgs);
        else if (commandName.equals("cancel"))
            performCancelBounty(sender, trimmedArgs);
        else if (commandName.equals("list"))
            performListBounties(sender, trimmedArgs);
        else if (commandName.equals("accept"))
            performAcceptBounty(sender, trimmedArgs);
        else if (commandName.equals("abandon"))
            performAbandonBounty(sender, trimmedArgs);
        else if (commandName.equals("view"))
            performViewBounties(sender, trimmedArgs);
        else if (commandName.equals("locate"))
            performLocateTarget(sender, trimmedArgs);

        return true;
    }
    
    private boolean performLocateTarget(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        Player hunter = (Player)sender;
        String hunterName = hunter.getName();
        Messaging.save(hunter);
        
        List<Bounty> acceptedBounties = listBountiesAcceptedByPlayer(hunterName);
        
        if (acceptedBounties.isEmpty()) {
            Messaging.send(bountyTag + Colors[0] + "You currently have no accepted bounties.");
        } else {
            Messaging.send(Colors[0] + "Last Known Target Locations: (x,z)");
            
            for (int i = 0; i < acceptedBounties.size(); i++) {
                Bounty b = acceptedBounties.get(i);
                Point2D targetLocation = b.getTargetLocation();
                int x = (int)targetLocation.getX();
                int z = (int)targetLocation.getY();
                
                Messaging.send(Colors[2] + (i + 1) + ". " + Colors[1] + b.getTarget() + " - " + "(" + x + "," + z + ")");
            }
        }
        
        return true;
    }

    private boolean performShowHelp(CommandSender sender, String[] args) {
        Messaging.save(sender);
        Messaging.send(Colors[0] + "-----[ " + Colors[2] + " Hero Bounty Help " + Colors[0] + " ]-----");
        Messaging.send(Colors[1] + "/bounty help - Show this information.");
        Messaging.send(Colors[1] + "/bounty new <player> <value> - Create a new bounty.");
        Messaging.send(Colors[1] + "/bounty list <page#> - List bounties available.");
        Messaging.send(Colors[1] + "/bounty accept <id#> - Accept bounty by id.");
        Messaging.send(Colors[1] + "/bounty abandon <id#> - Abandon bounty by id.");
        Messaging.send(Colors[1] + "/bounty cancel <id#> - Cancel bounty by id.");
        Messaging.send(Colors[1] + "/bounty view - View your accepted bounties.");
        Messaging.send(Colors[1] + "/bounty locate - Show last known target locations.");

        return false;
    }

    private String formatCurrency(int Balance, String currency) {
        return formatNumberWithCommas(String.valueOf(Balance)) + " " + currency;
    }
    
    private String formatNumberWithCommas(String str) {
        if (str.length() < 4) {
            return str;
        }

        return formatNumberWithCommas(str.substring(0, str.length() - 3)) + "," + str.substring(str.length() - 3, str.length());
    }

    private boolean performNewBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Messaging.save(sender);

        if (args.length != 2) {
            Messaging.send(Colors[0] + "Usage: /bounty new <player> <value>");
            return false;
        }

        Player target = getServer().getPlayer(args[0]);
        if (target == null) {
            Messaging.send(bountyTag + Colors[0] + "Target player not found.");
            return false;
        }

        Player owner = (Player) sender;

        for (Bounty b : bounties) {
            if (b.getTarget().equalsIgnoreCase(args[0])) {
                Messaging.send(bountyTag + Colors[0] + "There is already a bounty on " + Colors[2] + target.getName() + Colors[0] + ".");
                return false;
            }
        }

        int value;

        try {
            value = Integer.parseInt(args[1]);

            if (value < bountyMin)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Messaging.send(bountyTag + Colors[0] + "Value must be a number greater than " + Colors[2] + bountyMin + Colors[0] + ".");
            return false;
        }

        int balance = iConomy.db.get_balance(owner.getName());
        if (balance < value) {
            Messaging.send(bountyTag + Colors[0] + "You don't have enough funds to do that!");
            return false;
        }

        int fee = (int) (bountyFeePercent * value);
        int award = value - fee;
        int contractFee = (int) (contractFeePercent * award);
        int deathPenalty = (int) (deathPenaltyPercent * award);

        Bounty bounty = new Bounty(owner.getName(), owner.getDisplayName(), target.getName(), target.getDisplayName(), award, contractFee, deathPenalty);
        Location loc = target.getLocation();
        bounty.setTargetLocation(new Point2D.Double(loc.getX(), loc.getZ()));
        
        bounties.add(bounty);
        Collections.sort(bounties);

        iConomy.db.set_balance(owner.getName(), balance - value);
        Messaging.send(bountyTag + Colors[0] + "Placed a bounty on " + Colors[1] + target.getName() + Colors[0] + "'s head for " + Colors[1] + award
                + iConomy.currency + Colors[0] + ".");
        Messaging.send(bountyTag + Colors[0] + "You have been charged a " + Colors[1] + fee + iConomy.currency + Colors[0] + " fee for posting a bounty.");
        Messaging.broadcast(bountyTag + Colors[1] + "A new bounty has been placed for " + Colors[2] + award + iConomy.currency + Colors[1] + ".");

        saveData();

        return true;
    }

    private boolean performCancelBounty(CommandSender sender, String[] args) {
        Messaging.save(sender);

        if (!(sender instanceof Player))
            return false;

        if (args.length != 1) {
            Messaging.send(bountyTag + Colors[0] + "Usage: /bounty cancel <id#>");
            return false;
        }

        Player owner = (Player) sender;
        Messaging.save(owner);

        int id = parseBountyId(args[0], bounties);

        if (id == -1) {
            Messaging.send(bountyTag + Colors[0] + "Bounty not found.");
            return false;
        }

        Bounty bounty = bounties.get(id);

        if (!bounty.getOwner().equalsIgnoreCase(owner.getName())) {
            Messaging.send(bountyTag + Colors[0] + "You can only cancel bounties you created.");
            return false;
        }

        bounties.remove(bounty);
        Collections.sort(bounties);

        iConomy.db.set_balance(owner.getName(), iConomy.db.get_balance(owner.getName()) + bounty.getValue());
        Messaging
                .send(bountyTag + Colors[0] + "You have been reimbursed " + Colors[1] + bounty.getValue() + iConomy.currency + Colors[0] + " for your bounty.");

        for (String hunterName : bounty.getHunters()) {
            Player hunter = getServer().getPlayer(hunterName);

            iConomy.db.set_balance(hunterName, iConomy.db.get_balance(hunterName) + bounty.getContractFee());

            if (hunter == null)
                continue;

            Messaging.send(hunter, bountyTag + Colors[0] + "The bounty you were pursuing targetting " + Colors[1] + bounty.getTarget() + Colors[0]
                    + " has been cancelled.");
            Messaging.send(hunter, bountyTag + Colors[0] + "You have been reimbursed the " + Colors[1]
                    + formatCurrency(bounty.getContractFee(), iConomy.currency) + Colors[0] + " you paid for the bounty.");
        }

        saveData();

        return true;
    }

    private boolean performListBounties(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Messaging.save(sender);
        String senderName = ((Player) sender).getName();

        int perPage = 7;
        int currentPage;

        if (args.length == 0)
            currentPage = 0;
        else
            currentPage = (args[0] == null) ? 0 : Integer.valueOf(args[0]);
        currentPage = (currentPage == 0) ? 1 : currentPage;
        int amountPages = (int) Math.ceil(bounties.size() / perPage) + 1;
        int pageStart = (currentPage - 1) * perPage;
        int pageEnd = (currentPage - 1) * perPage + perPage;
        pageEnd = (pageEnd >= bounties.size()) ? bounties.size() - 1 : pageEnd;

        if (bounties.isEmpty()) {
            Messaging.send(bountyTag + Colors[0] + "No bounties currently listed.");
        } else if (currentPage > amountPages) {
            Messaging.send(bountyTag + Colors[0] + "Invalid page number.");
        } else {
            Messaging.send(Colors[0] + "Available Bounties (Page &f#" + currentPage + Colors[0] + " of &f" + amountPages + Colors[0] + " ):");

            for (int i = pageStart; i <= pageEnd; i++) {
                Bounty b = bounties.get(i);

                String msg = Colors[3] + (i + 1) + ". " + Colors[1];

                if (!anonymousTargets)
                    msg += b.getTarget() + Colors[3] + " - " + Colors[1];

                msg += formatCurrency(b.getValue(), iConomy.currency) + Colors[3] + " - " + Colors[1] + "Fee: "
                        + formatCurrency(b.getContractFee(), iConomy.currency);

                if (senderName.equalsIgnoreCase(b.getOwner()))
                    msg += Colors[3] + " *YOURS*";

                Messaging.send(msg);
            }
        }

        return true;
    }

    private boolean performAcceptBounty(CommandSender sender, String[] args) {
        Messaging.save(sender);

        if (!(sender instanceof Player))
            return false;

        if (args.length != 1) {
            Messaging.send(Colors[0] + "Usage: /bounty accept <id#>");
            return false;
        }

        Player hunter = (Player) sender;
        String hunterName = hunter.getName();

        int id = parseBountyId(args[0], bounties);

        if (id < 0) {
            Messaging.send(bountyTag + Colors[0] + "Bounty not found.");
            return false;
        }

        Bounty bounty = bounties.get(id);

        if (bounty.getOwner().equalsIgnoreCase(hunter.getName())) {
            Messaging.send(bountyTag + Colors[0] + "You cannot accept your own bounty!");
            return false;
        }

        if (bounty.getTarget().equalsIgnoreCase(hunterName)) {
            Messaging.send(bountyTag + Colors[0] + "You cannot accept this bounty!");
            return false;
        }

        if (bounty.isHunter(hunterName)) {
            Messaging.send(bountyTag + Colors[0] + "You have already accepted this bounty!");
            return false;
        }

        int balance = iConomy.db.get_balance(hunterName);
        if (balance < bounty.getContractFee()) {
            Messaging.send(bountyTag + Colors[0] + "You don't have enough funds to do that!");
            return false;
        }

        bounty.addHunter(hunterName);

        GregorianCalendar expiration = new GregorianCalendar();
        expiration.add(Calendar.MINUTE, bountyDuration);
        bounty.getExpirations().put(hunterName, expiration.getTime());
        iConomy.db.set_balance(hunterName, balance - bounty.getContractFee());

        int bountyRelativeTime = (bountyDuration < 60) ? bountyDuration : (bountyDuration < (60 * 24)) ? bountyDuration / 60
                : (bountyDuration < (60 * 24 * 7)) ? bountyDuration / (60 * 24) : bountyDuration / (60 * 24 * 7);

        String bountyRelativeAmount = (bountyDuration < 60) ? " minutes" : (bountyDuration < (60 * 24)) ? " hours" : (bountyDuration < (60 * 24 * 7)) ? " days"
                : " weeks";

        Messaging.send(bountyTag + Colors[0] + "Bounty accepted. You have been charged a " + Colors[2]
                + formatCurrency(bounty.getContractFee(), iConomy.currency) + Colors[1] + " contract fee.");
        Messaging.send(bountyTag + Colors[0] + "Target is " + Colors[2] + bounty.getTargetDisplayName() + Colors[1] + "! Bounty will expire in " + Colors[2]
                + bountyRelativeTime + Colors[1] + bountyRelativeAmount);

        Player owner = getServer().getPlayer(bounty.getOwner());

        if (owner != null) {
            Messaging.send(owner, Colors[0] + "Your bounty on " + Colors[1] + bounty.getTarget() + Colors[0] + " has been accepted by " + Colors[1]
                    + hunterName + Colors[0] + ".");
        }

        saveData();

        return true;
    }

    private boolean performAbandonBounty(CommandSender sender, String[] args) {
        Messaging.save(sender);

        if (!(sender instanceof Player))
            return false;

        if (args.length != 1) {
            Messaging.send(Colors[0] + "Usage: /bounty abandon <id#>");
            return false;
        }

        Player hunter = (Player) sender;

        List<Bounty> acceptedBounties = listBountiesAcceptedByPlayer(hunter.getName());

        int id = parseBountyId(args[0], acceptedBounties);

        if (id == -1) {
            Messaging.send(bountyTag + Colors[0] + "Bounty not found.");
            return false;
        }

        acceptedBounties.get(id).removeHunter(hunter.getName());
        Messaging.send(bountyTag + Colors[0] + "Bounty abandoned.");

        saveData();

        return false;
    }

    private boolean performViewBounties(CommandSender sender, String[] args) {
        Messaging.save(sender);

        if (!(sender instanceof Player))
            return false;

        Player hunter = (Player) sender;
        String hunterName = hunter.getName();

        List<Bounty> acceptedBounties = listBountiesAcceptedByPlayer(hunterName);

        if (acceptedBounties.isEmpty()) {
            Messaging.send(bountyTag + Colors[0] + "You currently have no accepted bounties.");
        } else {
            Messaging.send(Colors[0] + "Accepted Bounties:");
            for (int i = 0; i < acceptedBounties.size(); i++) {
                Bounty b = acceptedBounties.get(i);
                int bountyDuration = b.getMinutesLeft(hunterName);

                int bountyRelativeTime = (bountyDuration < 60) ? bountyDuration : (bountyDuration < (60 * 24)) ? bountyDuration / 60
                        : (bountyDuration < (60 * 24 * 7)) ? bountyDuration / (60 * 24) : bountyDuration / (60 * 24 * 7);

                String bountyRelativeAmount = (bountyDuration < 60) ? " minutes" : (bountyDuration < (60 * 24)) ? " hours"
                        : (bountyDuration < (60 * 24 * 7)) ? " days" : " weeks";

                Messaging.send(Colors[2] + (i + 1) + ". " + Colors[1] + b.getTarget() + " - " + formatCurrency(b.getValue(), iConomy.currency) + " - "
                        + bountyRelativeTime + bountyRelativeAmount);
            }
        }

        return true;
    }

    public static int parseBountyId(String idStr, List<Bounty> bounties) {
        int id;

        try {
            id = Integer.parseInt(idStr) - 1;

            if (id < 0 || id >= bounties.size())
                throw new IndexOutOfBoundsException();
        } catch (Exception e) {
            return -1;
        }

        return id;
    }

    private List<Bounty> listBountiesAcceptedByPlayer(String hunter) {
        List<Bounty> acceptedBounties = new ArrayList<Bounty>();

        for (Bounty b : bounties)
            if (b.isHunter(hunter))
                acceptedBounties.add(b);

        return acceptedBounties;
    }

    public List<Bounty> getBounties() {
        return bounties;
    }

    public void setBounties(List<Bounty> bounties) {
        this.bounties = bounties;
    }

    public boolean completeBounty(int id, String hunterName) {
        if (id < 0 || id >= bounties.size())
            return false;

        Bounty bounty = bounties.get(id);

        Player hunter = getServer().getPlayer(hunterName);
        if (hunter == null)
            return false;

        Player target = getServer().getPlayer(bounty.getTarget());
        if (target == null)
            return false;

        bounties.remove(id);
        Collections.sort(bounties);

        int hunterBalance = iConomy.db.get_balance(hunter.getName());
        int targetBalance = iConomy.db.get_balance(target.getName());
        int deathPenalty = bounty.getDeathPenalty();
        if (targetBalance < deathPenalty)
            deathPenalty = targetBalance;

        iConomy.db.set_balance(hunter.getName(), hunterBalance + bounty.getValue());
        iConomy.db.set_balance(target.getName(), targetBalance - deathPenalty);

        Messaging.broadcast(bountyTag + Colors[1] + hunter.getName() + Colors[0] + " has collected a bounty on " + Colors[1] + bounty.getTargetDisplayName()
                + Colors[0] + " for " + Colors[1] + formatCurrency(bounty.getValue(), iConomy.currency) + Colors[0] + "!");
        Messaging.send(hunter, bountyTag + Colors[0] + "Well done! You have been awarded " + Colors[1] + formatCurrency(bounty.getValue(), iConomy.currency)
                + Colors[0] + ".");

        if (deathPenalty > 0)
            Messaging.send(target, bountyTag + Colors[0] + "You have lost " + Colors[1] + formatCurrency(bounty.getDeathPenalty(), iConomy.currency)
                    + Colors[0] + " for falling victim to a bounty issued by " + Colors[1] + bounty.getOwnerDisplayName() + Colors[0] + "!");
        else
            Messaging.send(target, bountyTag + Colors[0] + "You have fallen victim to a bounty placed by " + Colors[1] + bounty.getOwnerDisplayName()
                    + Colors[0] + "!");

        saveData();

        return true;
    }

    public String getBountyTag() {
        return bountyTag;
    }

    public void setBountyTag(String bountyTag) {
        this.bountyTag = bountyTag;
    }

    public void updateBountyTargetLocations() {
        for (Player p : getServer().getOnlinePlayers()) {
            for (Bounty b : bounties) {
                if (b.getTarget().equalsIgnoreCase(p.getName())) {
                    boolean nearAnyone = false;
                    
                    Location loc = p.getLocation();
                    Point2D point = new Point2D.Double(loc.getX(), loc.getZ());
                    for (Player p2 : getServer().getOnlinePlayers()) {
                        if (p2.getName().equals(p.getName()))
                            break;
                        
                        Location loc2 = p2.getLocation();
                        Point2D point2 = new Point2D.Double(loc2.getX(), loc2.getZ());
                        
                        if (point.distance(point2) <= locationUpdateDistance) {
                            nearAnyone = true;
                            break;
                        }
                    }
                    
                    if (nearAnyone) {
                        b.setTargetLocation(point);
                        break;
                    }
                }
            }
        }
    }
}

class ExpirationChecker extends TimerTask {

    private HeroBountyPlugin plugin;

    public ExpirationChecker(HeroBountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Bounty bounty : plugin.getBounties()) {
            for (String name : bounty.getExpirations().keySet()) {
                if (bounty.getMillisecondsLeft(name) <= 0) {
                    Player hunter = plugin.getServer().getPlayer(name);

                    bounty.removeHunter(name);

                    plugin.saveData();

                    if (hunter == null)
                        continue;

                    Messaging.send(hunter, plugin.getBountyTag() + HeroBountyPlugin.Colors[0] + "Your bounty on " + HeroBountyPlugin.Colors[1]
                            + bounty.getTargetDisplayName() + HeroBountyPlugin.Colors[0] + " has expired!");
                }
            }
        }
    }

    public HeroBountyPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(HeroBountyPlugin plugin) {
        this.plugin = plugin;
    }

}

class LocationChecker extends TimerTask {
    
    private HeroBountyPlugin plugin;

    public LocationChecker(HeroBountyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        plugin.updateBountyTargetLocations();
    }

    public void setPlugin(HeroBountyPlugin plugin) {
        this.plugin = plugin;
    }

    public HeroBountyPlugin getPlugin() {
        return plugin;
    }
    
}
