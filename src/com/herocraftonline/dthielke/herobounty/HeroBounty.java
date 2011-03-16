/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.herocraftonline.dthielke.herobounty.command.CommandManager;
import com.herocraftonline.dthielke.herobounty.command.commands.AbandonCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.AcceptCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.CancelCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.HelpCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.ListCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.LocateCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.NewCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.ViewCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class HeroBounty extends JavaPlugin {
    private HeroBountyEntityListener entityListener = new HeroBountyEntityListener(this);
    private CommandManager commandManager;
    private List<Bounty> bounties;
    private String tag;
    private int bountyMin;
    private float bountyFeePercent;
    private float contractFeePercent;
    private float deathPenaltyPercent;
    private int bountyDuration; // in minutes
    private boolean payInconvenience;
    private boolean anonymousTargets;
    private boolean allowNegativeBalances;
    private int locationRounding;

    private Logger log;

    private Timer expirationTimer = new Timer();

    public static final int EXPIRATION_TIMER_DELAY = 10000;
    public static final int EXPIRATION_TIMER_PERIOD = 1 * 60 * 1000;
    public static final String[] Colors = new String[] { "&c", "&e", "&f", "&7" };

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

        Account hunterAccount = iConomy.getBank().getAccount(hunter.getName());
        Account targetAccount = iConomy.getBank().getAccount(target.getName());
        double targetBalance = targetAccount.getBalance();
        double deathPenalty = bounty.getDeathPenalty();
        if (!allowNegativeBalances && (targetBalance < deathPenalty))
            deathPenalty = targetBalance;

        hunterAccount.add(bounty.getValue());
        targetAccount.subtract(deathPenalty);

        Messaging.broadcast(this, tag + Colors[1] + hunter.getName() + Colors[0] + " has collected a bounty on " + Colors[1] + bounty.getTargetDisplayName() + Colors[0] + " for " + Colors[1] + iConomy.getBank().format(bounty.getValue()) + Colors[0] + "!");
        Messaging.send(hunter, tag + Colors[0] + "Well done! You have been awarded " + Colors[1] + iConomy.getBank().format(bounty.getValue()) + Colors[0] + ".");

        if (deathPenalty > 0)
            Messaging.send(target, tag + Colors[0] + "You have lost " + Colors[1] + iConomy.getBank().format(bounty.getDeathPenalty()) + Colors[0] + " for falling victim to a bounty issued by " + Colors[1] + bounty.getOwnerDisplayName() + Colors[0] + "!");
        else
            Messaging.send(target, tag + Colors[0] + "You have fallen victim to a bounty placed by " + Colors[1] + bounty.getOwnerDisplayName() + Colors[0] + "!");

        saveData();

        return true;
    }

    public boolean checkIConomy() {
        Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");
        boolean useIConomy = false;
        if (test != null) {
            useIConomy = true;
        }
        return useIConomy;

    }

    public List<Bounty> getBounties() {
        return bounties;
    }

    public String getTag() {
        return tag;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandManager.dispatch(sender, command, label, args);
    }

    @Override
    public void onDisable() {
        expirationTimer.cancel();
        expirationTimer.purge();

        PluginDescriptionFile pdfFile = this.getDescription();
        log(pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        log = Logger.getLogger("Minecraft");

        PluginDescriptionFile pdfFile = this.getDescription();
        log(pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");

        Configuration config = getConfiguration();

        bountyMin = config.getInt("bounty-min", 20);
        bountyFeePercent = config.getInt("bounty-fee-percent", 10) / 100f;
        contractFeePercent = config.getInt("contract-fee-percent", 5) / 100f;
        deathPenaltyPercent = config.getInt("death-penalty-percent", 5) / 100f;
        bountyDuration = config.getInt("bounty-duration", 24 * 60);
        anonymousTargets = config.getBoolean("anonymous-targets", false);
        tag = config.getString("bounty-tag", "&e[Bounty] ").replace('&', '§');
        payInconvenience = config.getBoolean("pay-inconvenience", true);
        allowNegativeBalances = config.getBoolean("allow-negative-balances", true);
        locationRounding = config.getInt("location-rounding", 100);

        File file = new File(getDataFolder(), "data.yml");
        bounties = BountyFileHandler.load(file);

        if (checkIConomy()) {
            TimerTask expirationChecker = new ExpirationChecker(this);
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, expirationChecker, EXPIRATION_TIMER_DELAY, EXPIRATION_TIMER_PERIOD);
        } else {
            log.log(Level.WARNING, "iConomy not found. Disabling.");
            this.getServer().getPluginManager().disablePlugin(this);
        }

        registerEvents();
        registerCommands();
    }

    public void log(String log) {
        this.log.log(Level.INFO, "[HeroBounty] " + log);
    }

    public void saveData() {
        File file = new File(getDataFolder(), "data.yml");
        BountyFileHandler.save(bounties, file);
    }

    public void setBounties(List<Bounty> bounties) {
        this.bounties = bounties;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isTarget(Player player) {
        String name = player.getName();
        for (Bounty bounty : bounties)
            if (bounty.getTarget().equals(name))
                return true;
        return false;
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pm.registerEvent(Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
    }

    private void registerCommands() {
        commandManager = new CommandManager();
        commandManager.addCommand(new HelpCommand(this));
        commandManager.addCommand(new ListCommand(this));
        commandManager.addCommand(new ViewCommand(this));
        commandManager.addCommand(new AcceptCommand(this));
        commandManager.addCommand(new AbandonCommand(this));
        commandManager.addCommand(new NewCommand(this));
        commandManager.addCommand(new CancelCommand(this));
        commandManager.addCommand(new LocateCommand(this));
    }

    public List<Bounty> listBountiesAcceptedByPlayer(String hunter) {
        List<Bounty> acceptedBounties = new ArrayList<Bounty>();

        for (Bounty b : bounties)
            if (b.isHunter(hunter))
                acceptedBounties.add(b);

        return acceptedBounties;
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

    public boolean isUsingAnonymousTargets() {
        return anonymousTargets;
    }

    public void setAnonymousTargets(boolean anonymousTargets) {
        this.anonymousTargets = anonymousTargets;
    }

    public int getLocationRounding() {
        return locationRounding;
    }

    public void setLocationRounding(int locationRounding) {
        this.locationRounding = locationRounding;
    }

    public int getBountyMin() {
        return bountyMin;
    }

    public void setBountyMin(int bountyMin) {
        this.bountyMin = bountyMin;
    }

    public float getBountyFeePercent() {
        return bountyFeePercent;
    }

    public void setBountyFeePercent(float bountyFeePercent) {
        this.bountyFeePercent = bountyFeePercent;
    }

    public float getContractFeePercent() {
        return contractFeePercent;
    }

    public void setContractFeePercent(float contractFeePercent) {
        this.contractFeePercent = contractFeePercent;
    }

    public float getDeathPenaltyPercent() {
        return deathPenaltyPercent;
    }

    public void setDeathPenaltyPercent(float deathPenaltyPercent) {
        this.deathPenaltyPercent = deathPenaltyPercent;
    }

    public int getBountyDuration() {
        return bountyDuration;
    }

    public void setBountyDuration(int bountyDuration) {
        this.bountyDuration = bountyDuration;
    }
    

    public boolean shouldPayInconvenience() {
        return payInconvenience;
    }
    

    public void setPayInconvenience(boolean payInconvenience) {
        this.payInconvenience = payInconvenience;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}

class ExpirationChecker extends TimerTask {

    private HeroBounty plugin;

    public ExpirationChecker(HeroBounty plugin) {
        this.plugin = plugin;
    }

    public HeroBounty getPlugin() {
        return plugin;
    }

    @Override
    public void run() {
        Bounty[] bounties = plugin.getBounties().toArray(new Bounty[0]);
        for (Bounty bounty : bounties) {
            String[] hunters = bounty.getExpirations().keySet().toArray(new String[0]);
            for (String name : hunters) {
                if (bounty.getMillisecondsLeft(name) <= 0) {
                    Player hunter = plugin.getServer().getPlayer(name);

                    bounty.removeHunter(name);

                    plugin.saveData();

                    if (hunter == null)
                        continue;

                    Messaging.send(hunter, plugin.getTag() + HeroBounty.Colors[0] + "Your bounty on " + HeroBounty.Colors[1] + bounty.getTargetDisplayName() + HeroBounty.Colors[0] + " has expired!");
                }
            }
        }
    }

    public void setPlugin(HeroBounty plugin) {
        this.plugin = plugin;
    }

}
