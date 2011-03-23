/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.CommandManager;
import com.herocraftonline.dthielke.herobounty.command.commands.AbandonCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.AcceptCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.CancelCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.HelpCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.ListCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.LocateCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.NewCommand;
import com.herocraftonline.dthielke.herobounty.command.commands.ViewCommand;
import com.herocraftonline.dthielke.herobounty.util.EconomyManager;
import com.herocraftonline.dthielke.herobounty.util.PermissionManager;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class HeroBounty extends JavaPlugin {
    private HeroBountyEntityListener entityListener;
    private HeroBountyServerListener serverListener;
    private CommandManager commandManager;
    private PermissionManager permissionManager;
    private EconomyManager economyManager;
    private BountyManager bountyManager;
    private String tag;
    private Logger log;

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public String getTag() {
        return tag;
    }

    public void loadConfig() {
        Configuration config = getConfiguration();
        bountyManager = new BountyManager(this);
        bountyManager.setBounties(BountyFileHandler.load(new File(getDataFolder(), "data.yml")));
        bountyManager.setMinimumValue(config.getInt("bounty-min", 20));
        bountyManager.setPlacementFee(config.getInt("bounty-fee-percent", 10) / 100f);
        bountyManager.setContractFee(config.getInt("contract-fee-percent", 5) / 100f);
        bountyManager.setDeathFee(config.getInt("death-penalty-percent", 5) / 100f);
        bountyManager.setDuration(config.getInt("bounty-duration", 24 * 60));
        bountyManager.setAnonymousTargets(config.getBoolean("anonymous-targets", false));
        bountyManager.setPayInconvenience(config.getBoolean("pay-inconvenience", true));
        bountyManager.setNegativeBalances(config.getBoolean("allow-negative-balances", true));
        bountyManager.setLocationRounding(config.getInt("location-rounding", 100));
        tag = config.getString("bounty-tag", "&e[Bounty] ").replace('&', '§');
    }

    public void loadIConomy() {
        economyManager = new EconomyManager();
        Plugin plugin = this.getServer().getPluginManager().getPlugin("iConomy");
        if (plugin != null) {
            if (plugin.isEnabled()) {
                iConomy iconomy = (iConomy) plugin;
                economyManager.setIconomy(iconomy);
                bountyManager.startExpirationTimer();
                registerCommands();
                log(Level.INFO, "iConomy " + iconomy.getDescription().getVersion() + " found.");
            }
        }
    }

    public void loadPermissions() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");
        if (plugin != null) {
            if (plugin.isEnabled()) {
                Permissions permissions = (Permissions) plugin;
                PermissionHandler security = permissions.getHandler();
                PermissionManager ph = new PermissionManager(security);
                this.permissionManager = ph;
                log(Level.INFO, "Permissions " + Permissions.version + " found.");
            }
        }
    }

    public void log(Level level, String log) {
        this.log.log(level, "[HeroBounty] " + log);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandManager.dispatch(sender, command, label, args);
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        log = Logger.getLogger("Minecraft");

        PluginDescriptionFile pdfFile = this.getDescription();
        log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");

        registerEvents();
        loadConfig();
        loadPermissions();
        loadIConomy();
        commandManager = new CommandManager();
    }

    public void saveData() {
        File file = new File(getDataFolder(), "data.yml");
        BountyFileHandler.save(bountyManager.getBounties(), file);
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    private void registerEvents() {
        entityListener = new HeroBountyEntityListener(this);
        serverListener = new HeroBountyServerListener(this);
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
        pluginManager.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
    }
}
