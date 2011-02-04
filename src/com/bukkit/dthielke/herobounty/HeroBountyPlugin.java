package com.bukkit.dthielke.herobounty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import com.nijikokun.bukkit.iConomy.iConomy;

public class HeroBountyPlugin extends JavaPlugin {    
    private HeroBountyEntityListener entityListener = new HeroBountyEntityListener(this);
    
    private List<Bounty> bounties;
    private int bountyMin;
    private float bountyFeePercent;
    private float contractFeePercent;

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

        bounties = new ArrayList<Bounty>();
        
        bountyMin = 20;
        bountyFeePercent = 0.10f;
        contractFeePercent = 0.05f;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (args.length == 0) {
            return performShowHelp(sender, args);
        }

        String commandName = args[0].toLowerCase();
        String[] trimmedArgs = new String[args.length - 1];

        for (int i = 0; i < args.length - 1; i++)
            trimmedArgs[i] = args[i + 1];

        if (commandName.equals("help"))
            return performShowHelp(sender, trimmedArgs);
        else if (commandName.equals("new"))
            return performNewBounty(sender, trimmedArgs);
        else if (commandName.equals("cancel"))
            return performCancelBounty(sender, trimmedArgs);
        else if (commandName.equals("list"))
            return performListBounties(sender, trimmedArgs);
        else if (commandName.equals("accept"))
            return performAcceptBounty(sender, trimmedArgs);
        else if (commandName.equals("abandon"))
            return performAbandonBounty(sender, trimmedArgs);
        else if (commandName.equals("view"))
            return performViewBounties(sender, trimmedArgs);

        return false;
    }

    private boolean performShowHelp(CommandSender sender, String[] args) {
        return false;
    }

    private boolean performNewBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 2) {
            sender.sendMessage("Usage: /bounty new <player> <value>");
            return false;
        }
        
        Player target = getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Target player not found.");
            return false;
        }
        
        Player owner = (Player)sender;
        
        for (Bounty b : bounties) {
            if (b.getTarget().equalsIgnoreCase(args[0])) {
                owner.sendMessage("There is already a bounty on " + target.getName() + ".");
                return false;
            }
        }
        
        int value;
        
        try {
            value = Integer.parseInt(args[1]);
            
            if (value < bountyMin)
                throw new NumberFormatException();
        } catch(NumberFormatException e) {
            owner.sendMessage("Value must be a number greater than " + bountyMin + ".");
            return false;
        }
        
        int balance = iConomy.db.get_balance(owner.getName());
        if (balance < value) {
            owner.sendMessage("You don't have enough funds to do that!");
            return false;
        }
        
        int fee = (int)(bountyFeePercent * value);
        int award = value - fee;
        
        bounties.add(new Bounty(owner.getName(), target.getName(), award, (int)(contractFeePercent * award), (int)(contractFeePercent * award)));
        Collections.sort(bounties);
        
        iConomy.db.set_balance(owner.getName(), balance - value);
        owner.sendMessage("Placed a bounty on " + target.getName() + "'s head for " + award + iConomy.currency + ".");
        owner.sendMessage("You have been charged a " + fee + iConomy.currency + " fee for posting a bounty.");
        
        return true;
    }

    private boolean performCancelBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 1) {
            sender.sendMessage("Usage: /bounty cancel <id#>");
            return false;
        }
        
        Player owner = (Player)sender;
        
        List<Bounty> ownedBounties = listBountiesOwnedByPlayer(((Player)sender).getName());
        
        int id = parseBountyId(args[0], ownedBounties);
        
        if (id == -1) {
            owner.sendMessage("Bounty not found.");
            return false;
        }
        
        Bounty removedBounty = ownedBounties.get(id);
        bounties.remove(removedBounty);
        Collections.sort(bounties);
        
        iConomy.db.set_balance(owner.getName(), iConomy.db.get_balance(owner.getName()) + removedBounty.getValue());
        owner.sendMessage("You have been reimbursed " + removedBounty.getValue() + iConomy.currency + " for your bounty.");
        
        return true;
    }

    private boolean performListBounties(CommandSender sender, String[] args) {
        if (bounties.size() == 0) {
            sender.sendMessage("There are no open bounties.");
            sender.sendMessage("To create a bounty type '/bounty new <player> <value>'.");
        } else {
            sender.sendMessage("Available Bounties: id#. value");
            for (int i = 0; i < bounties.size(); i++) {
                Bounty b = bounties.get(i);
                sender.sendMessage((i + 1) + ". " + b.getValue() + "c");
            }
        }
        return true;
    }

    private boolean performAcceptBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 1) {
            sender.sendMessage("Usage: /bounty accept <id#>");
            return false;
        }
        
        Player hunter = (Player)sender;
        
        List<Bounty> openBounties = listOpenBounties();
        
        int id = parseBountyId(args[0], openBounties);
        
        if (id < 0) {
            hunter.sendMessage("Bounty not found.");
            return false;
        }
        
        Bounty acceptedBounty = openBounties.get(id);
        
        int balance = iConomy.db.get_balance(hunter.getName());
        if (balance < acceptedBounty.getContractFee()) {
            hunter.sendMessage("You don't have enough funds to do that!");
            return false;
        }
        
        acceptedBounty.setHunter(hunter.getName());
        
        iConomy.db.set_balance(hunter.getName(), balance - acceptedBounty.getContractFee());
        
        hunter.sendMessage("Bounty accepted. You have been charged a " + acceptedBounty.getContractFee() + iConomy.currency + " contract fee.");
        hunter.sendMessage("Your target is " + acceptedBounty.getTarget() + "! Go go go!");
        
        Player owner = getServer().getPlayer(acceptedBounty.getOwner());
        if (owner != null)
            owner.sendMessage("Your bounty on " + acceptedBounty.getTarget() + " has been accepted by " + acceptedBounty.getHunter() + ".");
        
        return true;
    }

    private boolean performAbandonBounty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        if (args.length != 1) {
            sender.sendMessage("Usage: /bounty abandon <id#>");
            return false;
        }
        
        List<Bounty> acceptedBounties = listBountiesAcceptedByPlayer(((Player)sender).getName());
        
        int id = parseBountyId(args[0], acceptedBounties);
        
        if (id == -1) {
            sender.sendMessage("Bounty not found.");
            return false;
        }
        
        acceptedBounties.get(id).setHunter("");
        sender.sendMessage("Bounty abandoned.");
        
        return false;
    }

    private boolean performViewBounties(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return false;
        
        List<Bounty> acceptedBounties = listBountiesAcceptedByPlayer(((Player)sender).getName());
        
        if (acceptedBounties.size() == 0) {
            sender.sendMessage("You currently have no accepted bounties.");
            sender.sendMessage("To view available bounties, type '/bounty list'.");
        } else {
            sender.sendMessage("Accepted Bounties: id#. Target - Value");
            for (int i = 0; i < acceptedBounties.size(); i++) {
                Bounty b = acceptedBounties.get(i);
                sender.sendMessage((i + 1) + ". " + b.getTarget() + " - " + b.getValue() + "c");
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
    
    private List<Bounty> listOpenBounties() {
        return listBountiesAcceptedByPlayer("");
    }
    
    private List<Bounty> listBountiesAcceptedByPlayer(String hunter) {
        List<Bounty> acceptedBounties = new ArrayList<Bounty>();
        
        for (Bounty b : bounties)
            if (b.getHunter().equalsIgnoreCase(hunter))
                acceptedBounties.add(b);
        
        return acceptedBounties;
    }
    
    public List<Bounty> getBounties() {
        return bounties;
    }
    
    public void setBounties(List<Bounty> bounties) {
        this.bounties = bounties;
    }
    
    public boolean completeBounty(int id) {
        if (id < 0 || id >= bounties.size())
            return false;
        
        Bounty bounty = bounties.get(id);
        
        Player hunter = getServer().getPlayer(bounty.getHunter());
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
        
        getServer().broadcastMessage(hunter.getName() + " has collected a bounty on " + bounty.getTarget() + " for " + bounty.getValue() + iConomy.currency + "!");
        hunter.sendMessage("Well done! You have been awarded " + bounty.getValue() + iConomy.currency + ".");
        target.sendMessage("You have lost " + bounty.getDeathPenalty() + iConomy.currency + " for falling victim to a bounty issued by " + bounty.getOwner() + "!");
        
        return true;
    }
}
