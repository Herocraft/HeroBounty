/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 *
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty;

import com.herocraftonline.dthielke.herobounty.bounties.BountyFileHandler;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.CommandHandler;
import com.herocraftonline.dthielke.herobounty.command.commands.*;
import com.herocraftonline.dthielke.herobounty.util.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeroBounty extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private HeroBountyEntityListener entityListener;
    private CommandHandler commandHandler;
    private BountyManager bountyManager;
    private ConfigManager configManager;

    public static Permission permission;
    public static Economy economy;

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void log(Level level, String log) {
        HeroBounty.log.log(level, "[HeroBounty] " + log);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.dispatch(sender, label, args);
    }

    @Override
    public void onDisable() {
        log(Level.INFO, "version " + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        bountyManager = new BountyManager(this);
        configManager = new ConfigManager(this);

        registerEvents();
        registerCommands();
        configManager.load();

        if (!setupPermission()) {
            log(Level.SEVERE, "Permission plugin not found. Disabling plugin.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            log(Level.SEVERE, "Economy plugin not found. Disabling plugin.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        log(Level.INFO, "version " + getDescription().getVersion() + " enabled.");
    }

    private boolean setupPermission() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        return (permission != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public void saveData() {
        File file = new File(getDataFolder(), "data.yml");
        BountyFileHandler.save(bountyManager.getBounties(), file);
    }

    private void registerCommands() {
        commandHandler = new CommandHandler();
        commandHandler.addCommand(new HelpCommand(this));
        commandHandler.addCommand(new ListCommand(this));
        commandHandler.addCommand(new ViewCommand(this));
        commandHandler.addCommand(new AcceptCommand(this));
        commandHandler.addCommand(new AbandonCommand(this));
        commandHandler.addCommand(new PlaceCommand(this));
        commandHandler.addCommand(new CancelCommand(this));
        commandHandler.addCommand(new LocateCommand(this));
    }

    private void registerEvents() {
        entityListener = new HeroBountyEntityListener(this);
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(entityListener, this);
    }

}