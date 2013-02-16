package com.herocraftonline.dthielke.herobounty.util;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyFileHandler;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;

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

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(primaryConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        config.options().copyDefaults(true);

        BountyManager bountyManager = plugin.getBountyManager();
        bountyManager.setBounties(BountyFileHandler.load(new File(plugin.getDataFolder(), "data.yml")));
        bountyManager.setMinimumValue(config.getInt("bounty-min", 20));
        bountyManager.setPlacementFee(config.getInt("bounty-fee-percent", 10) / 100f);
        bountyManager.setContractFee(config.getInt("contract-fee-percent", 5) / 100f);
        bountyManager.setDeathFee(config.getInt("death-penalty-percent", 5) / 100f);
        bountyManager.setCancellationFee(config.getDouble("cancellation-fee-percent", 5) / 100f);
        bountyManager.setDuration(config.getInt("bounty-duration", 24 * 60));
        bountyManager.setAnonymousTargets(config.getBoolean("anonymous-targets", false));
        bountyManager.setPayInconvenience(config.getBoolean("pay-inconvenience", true));
        bountyManager.setLocationRounding(config.getInt("location-rounding", 100));
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
