package com.bukkit.dthielke.herobounty;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
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
    private int bountyMin;
    private float bountyFeePercent;
    private float contractFeePercent;
    private int bountyDuration; // in minutes
    
    private Timer expirationTimer = new Timer();
    
    public static final int EXPIRATION_TIMER_DELAY = 10000;
    public static final int EXPIRATION_TIMER_PERIOD = 1 * 60 * 1000;
    public static final String COLOR1 = ChatColor.RED.toString();
    public static final String COLOR2 = ChatColor.YELLOW.toString();

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
        bountyDuration = config.getInt("bounty-duration", 24 * 60);
        
        File file = new File(getDataFolder(), "data.yml");
        bounties = BountyFileHandler.load(file);
        
        TimerTask expirationChecker = new ExpirationChecker(this);
        expirationTimer.scheduleAtFixedRate(expirationChecker, EXPIRATION_TIMER_DELAY, EXPIRATION_TIMER_PERIOD);
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

        return true;
    }

    private boolean performShowHelp(CommandSender sender, String[] args) {
        sender.sendMessage(COLOR1 + "Hero Bounty Help:");
        sender.sendMessage(COLOR2 + "/bounty help - show this list");
        sender.sendMessage(COLOR2 + "/bounty new <player> <value> - posts a new bounty");
        sender.sendMessage(COLOR2 + "/bounty cancel <id#> - cancels one of your bounties");
        sender.sendMessage(COLOR2 + "/bounty list - lists available bounties");
        sender.sendMessage(COLOR2 + "/bounty accept <id#> - accepts a bounty");
        sender.sendMessage(COLOR2 + "/bounty abandon <id#> - abandons an accepted bounty");
        sender.sendMessage(COLOR2 + "/bounty view - lists bounties you have accepted");
        
        return false;
    }

    private boolean performNewBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 2) {
            sender.sendMessage(COLOR1 + "Usage: /bounty new <player> <value>");
            return false;
        }
        
        Player target = getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(COLOR1 + "Target player not found.");
            return false;
        }
        
        Player owner = (Player)sender;
        
        for (Bounty b : bounties) {
            if (b.getTarget().equalsIgnoreCase(args[0])) {
                owner.sendMessage(COLOR1 + "There is already a bounty on " + COLOR2 + target.getName() + COLOR1 + ".");
                return false;
            }
        }
        
        int value;
        
        try {
            value = Integer.parseInt(args[1]);
            
            if (value < bountyMin)
                throw new NumberFormatException();
        } catch(NumberFormatException e) {
            owner.sendMessage(COLOR1 + "Value must be a number greater than " + COLOR2 + bountyMin + COLOR1 + ".");
            return false;
        }
        
        int balance = iConomy.db.get_balance(owner.getName());
        if (balance < value) {
            owner.sendMessage(COLOR1 + "You don't have enough funds to do that!");
            return false;
        }
        
        int fee = (int)(bountyFeePercent * value);
        int award = value - fee;
        int contractFee = (int)(contractFeePercent * award);        
        
        bounties.add(new Bounty(owner.getName(), target.getName(), award, contractFee, contractFee));
        Collections.sort(bounties);
        
        iConomy.db.set_balance(owner.getName(), balance - value);
        owner.sendMessage(COLOR1 + "Placed a bounty on " + COLOR2 + target.getName() + COLOR1 + "'s head for " + COLOR2 + award + iConomy.currency + COLOR1 + ".");
        owner.sendMessage(COLOR1 + "You have been charged a " + COLOR2 + fee + iConomy.currency + COLOR1 + " fee for posting a bounty.");
        
        saveData();
        
        return true;
    }

    private boolean performCancelBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 1) {
            sender.sendMessage(COLOR1 + "Usage: /bounty cancel <id#>");
            return false;
        }
        
        Player owner = (Player)sender;
        
        List<Bounty> ownedBounties = listBountiesOwnedByPlayer(((Player)sender).getName());
        
        int id = parseBountyId(args[0], ownedBounties);
        
        if (id == -1) {
            owner.sendMessage(COLOR1 + "Bounty not found.");
            return false;
        }
        
        Bounty bounty = ownedBounties.get(id);
        
        if (!bounty.getOwner().equalsIgnoreCase(owner.getName())) {
            owner.sendMessage(COLOR1 + "You can only cancel bounties you created.");
            return false;
        }
        
        bounties.remove(bounty);
        Collections.sort(bounties);
        
        iConomy.db.set_balance(owner.getName(), iConomy.db.get_balance(owner.getName()) + bounty.getValue());
        owner.sendMessage(COLOR1 + "You have been reimbursed " + COLOR2 + bounty.getValue() + iConomy.currency + COLOR1 + " for your bounty.");
        
        saveData();
        
        return true;
    }

    private boolean performListBounties(CommandSender sender, String[] args) {
        if (bounties.size() == 0) {
            sender.sendMessage(COLOR1 + "There are no available bounties.");
        } else {
            sender.sendMessage(COLOR1 + "Available Bounties:");
            for (int i = 0; i < bounties.size(); i++) {
                Bounty b = bounties.get(i);
                sender.sendMessage(COLOR2 + (i + 1) + ". " + b.getValue() + "c");
            }
        }
        return true;
    }

    private boolean performAcceptBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 1) {
            sender.sendMessage(COLOR1 + "Usage: /bounty accept <id#>");
            return false;
        }
        
        Player hunter = (Player)sender;
        String hunterName = hunter.getName();
        
        int id = parseBountyId(args[0], bounties);
        
        if (id < 0) {
            hunter.sendMessage(COLOR1 + "Bounty not found.");
            return false;
        }
        
        Bounty bounty = bounties.get(id);
        
        if (bounty.isHunter(hunterName)) {
            hunter.sendMessage(COLOR1 + "You have already accepted this bounty!");
            return false;
        }
        
        int balance = iConomy.db.get_balance(hunterName);
        if (balance < bounty.getContractFee()) {
            hunter.sendMessage(COLOR1 + "You don't have enough funds to do that!");
            return false;
        }
        
        bounty.addHunter(hunterName);
        
        GregorianCalendar expiration = new GregorianCalendar();
        expiration.add(Calendar.MINUTE, bountyDuration);
        bounty.getExpirations().put(hunterName, expiration.getTime());
        
        iConomy.db.set_balance(hunterName, balance - bounty.getContractFee());
        
        hunter.sendMessage(COLOR1 + "Bounty accepted. You have been charged a " + COLOR2 + bounty.getContractFee() + iConomy.currency + COLOR1 + " contract fee.");
        
        if (bountyDuration < 60)
            hunter.sendMessage(COLOR1 + "Your target is " + COLOR2 + bounty.getTarget() + COLOR1 + "! This bounty will expire in " + COLOR2 + bountyDuration + COLOR1 + " minutes.");
        else
            hunter.sendMessage(COLOR1 + "Your target is " + COLOR2 + bounty.getTarget() + COLOR1 + "! This bounty will expire in " + COLOR2 + bountyDuration / 60 + COLOR1 + " hours.");
        
        Player owner = getServer().getPlayer(bounty.getOwner());
        if (owner != null)
            owner.sendMessage(COLOR1 + "Your bounty on " + COLOR2 + bounty.getTarget() + COLOR1 + " has been accepted by " + COLOR2 + hunterName + COLOR1 + ".");
        
        saveData();
        
        return true;
    }

    private boolean performAbandonBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 1) {
            sender.sendMessage(COLOR1 + "Usage: /bounty abandon <id#>");
            return false;
        }
        
        Player hunter = (Player)sender;
        
        List<Bounty> acceptedBounties = listBountiesAcceptedByPlayer(hunter.getName());
        
        int id = parseBountyId(args[0], acceptedBounties);
        
        if (id == -1) {
            hunter.sendMessage(COLOR1 + "Bounty not found.");
            return false;
        }
        
        acceptedBounties.get(id).removeHunter(hunter.getName());
        hunter.sendMessage(COLOR1 + "Bounty abandoned.");
        
        saveData();
        
        return false;
    }

    private boolean performViewBounties(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        Player hunter = (Player)sender;
        String hunterName = hunter.getName();
        
        List<Bounty> acceptedBounties = listBountiesAcceptedByPlayer(hunterName);
        
        if (acceptedBounties.size() == 0) {
            hunter.sendMessage(COLOR1 + "You currently have no accepted bounties.");
        } else {
            hunter.sendMessage(COLOR1 + "Accepted Bounties:");
            for (int i = 0; i < acceptedBounties.size(); i++) {
                Bounty b = acceptedBounties.get(i);
                int minLeft = b.getMinutesLeft(hunterName);
                if (minLeft < 60)
                    hunter.sendMessage(COLOR2 + (i + 1) + ". " + b.getTarget() + " - " + b.getValue() + iConomy.currency + " - " + minLeft + "min");
                else
                    hunter.sendMessage(COLOR2 + (i + 1) + ". " + b.getTarget() + " - " + b.getValue() + iConomy.currency + " - " + minLeft / 60 + "hrs");
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
        } catch(Exception e) {
            return -1;
        }
        
        return id;
    }
    
    private List<Bounty> listBountiesOwnedByPlayer(String owner) {
        List<Bounty> ownedBounties = new ArrayList<Bounty>();
        
        for (Bounty b : bounties)
            if (b.getOwner().equalsIgnoreCase(owner))
                ownedBounties.add(b);
        
        return ownedBounties;
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
        
        getServer().broadcastMessage(COLOR2 + hunter.getName() + COLOR1 + " has collected a bounty on " + COLOR2 + bounty.getTarget() + COLOR1 + " for " + COLOR2 + bounty.getValue() + iConomy.currency + COLOR1 + "!");
        hunter.sendMessage(COLOR1 + "Well done! You have been awarded " + COLOR2 + bounty.getValue() + iConomy.currency + COLOR1 + ".");
        target.sendMessage(COLOR1 + "You have lost " + COLOR2 + bounty.getDeathPenalty() + iConomy.currency + COLOR1 + " for falling victim to a bounty issued by " + COLOR2 + bounty.getOwner() + COLOR1 + "!");
        
        saveData();
        
        return true;
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
                    
                    hunter.sendMessage(HeroBountyPlugin.COLOR1 + "Your bounty on " + HeroBountyPlugin.COLOR2 + bounty.getTarget() + HeroBountyPlugin.COLOR1 + " has expired!");
                }
            }
        }
    }
    
}
