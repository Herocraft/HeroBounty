package com.herocraftonline.dthielke.herobounty.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;

import com.herocraftonline.dthielke.herobounty.BountyFileHandler;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;

public class ConfigManager {

    private HeroBounty plugin;
    private File primaryConfigFile;
    private File bountyConfigFile;
    private boolean respectUntargettables;

    public ConfigManager(HeroBounty plugin) {
        this.plugin = plugin;
        this.primaryConfigFile = new File(plugin.getDataFolder(), "config.yml");
        this.bountyConfigFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        checkConfig(primaryConfigFile);
        checkConfig(bountyConfigFile);
        
        Configuration config = new Configuration(primaryConfigFile);
        config.load();
        
        BountyManager bountyManager = plugin.getBountyManager();
        bountyManager.setBounties(BountyFileHandler.load(new File(plugin.getDataFolder(), "data.yml")));
        bountyManager.setMinimumValue(config.getInt("bounty-min", 20));
        bountyManager.setPlacementFee(config.getInt("bounty-fee-percent", 10) / 100f);
        bountyManager.setContractFee(config.getInt("contract-fee-percent", 5) / 100f);
        bountyManager.setDeathFee(config.getInt("death-penalty-percent", 5) / 100f);
        bountyManager.setDuration(config.getInt("bounty-duration", 24 * 60));
        bountyManager.setAnonymousTargets(config.getBoolean("anonymous-targets", false));
        bountyManager.setPayInconvenience(config.getBoolean("pay-inconvenience", true));
        bountyManager.setNegativeBalances(config.getBoolean("allow-negative-balances", true));
        bountyManager.setLocationRounding(config.getInt("location-rounding", 100));
        plugin.setTag(config.getString("bounty-tag", "&e[Bounty] ").replace('&', '§'));
        respectUntargettables = config.getBoolean("respect-untargettables", true);
    }

    private void checkConfig(File config) {
        if (!config.exists()) {
            try {
                plugin.log(Level.WARNING, "File " + config.getName() + " not found - generating defaults.");
                config.getParentFile().mkdir();
                config.createNewFile();
                OutputStream output = new FileOutputStream(config, false);
                InputStream input = ConfigManager.class.getResourceAsStream("/defaults/" + config.getName());
                byte[] buf = new byte[8192];
                while (true) {
                    int length = input.read(buf);
                    if (length < 0) {
                        break;
                    }
                    output.write(buf, 0, length);
                }
                input.close();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean shouldRespectUntargettables() {
        return respectUntargettables;
    }

}
