package com.herocraftonline.dthielke.herobounty.bounties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.util.Economy;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

@SuppressWarnings("unused")
public class BountyManager {
    
    private static final long EXPIRATION_DELAY = 10 * 1000;
    private static final long EXPIRATION_PERIOD = 5 * 60 * 1000;

    private HeroBounty plugin;
    private List<Bounty> bounties;
    private double minimumValue;
    private double placementFee;
    private double contractFee;
    private double deathFee;
    private boolean payInconvenience;
    private boolean anonymousTargets;
    private boolean negativeBalances;
    private int duration;
    private int locationRounding;

    public BountyManager(HeroBounty plugin) {
        this.plugin = plugin;
    }

    public void startExpirationTimer() {
        TimerTask expirationChecker = new ExpirationChecker(plugin);
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, expirationChecker, EXPIRATION_DELAY, EXPIRATION_PERIOD);
    }
    
    public void stopExpirationTimer() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }
    
    public boolean completeBounty(int id, String hunterName) {
        Bounty bounty = bounties.get(id);
        Player hunter = plugin.getServer().getPlayer(hunterName);
        Player target = plugin.getServer().getPlayer(bounty.getTarget());
        if (hunter == null || target == null) {
            return false;
        }
        
        bounties.remove(id);
        Collections.sort(bounties);
        plugin.saveData();

        Economy econ = plugin.getEconomy();
        double penalty = econ.subtract(target.getName(), bounty.getDeathPenalty(), negativeBalances);
        double award = econ.add(hunter.getName(), bounty.getValue());
        if (penalty != Double.NaN && award != Double.NaN) {
            String awardStr = econ.format(award);
            String penaltyStr = econ.format(penalty);
            Messaging.broadcast(plugin, "$1 has collected a bounty on $2 for $3!", hunter.getName(), bounty.getTargetDisplayName(), awardStr);
        } else {
            Messaging.broadcast(plugin, "$1 has collected a bounty on $2's head!", hunter.getName(), bounty.getTargetDisplayName());
        }
        return true;
    }

    public boolean isTarget(Player player) {
        String name = player.getName();
        for (Bounty bounty : bounties) {
            if (bounty.getTarget().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public List<Bounty> listBountiesAcceptedBy(String hunter) {
        List<Bounty> acceptedBounties = new ArrayList<Bounty>();
        for (Bounty bounty : bounties) {
            if (bounty.isHunter(hunter)) {
                acceptedBounties.add(bounty);
            }
        }
        return acceptedBounties;
    }

    public static int parseBountyId(String idStr, List<Bounty> bounties) {
        int id;
        try {
            id = Integer.parseInt(idStr) - 1;
            if (id < 0 || id >= bounties.size()) {
                throw new IndexOutOfBoundsException();
            }
        } catch (Exception e) {
            id = -1;
        }
        return id;
    }

    public List<Bounty> getBounties() {
        return bounties;
    }

    public void setBounties(List<Bounty> bounties) {
        this.bounties = bounties;
    }

    public double getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(double minimumValue) {
        this.minimumValue = minimumValue;
    }

    public double getPlacementFee() {
        return placementFee;
    }

    public void setPlacementFee(double placementFee) {
        this.placementFee = placementFee;
    }

    public double getContractFee() {
        return contractFee;
    }

    public void setContractFee(double contractFee) {
        this.contractFee = contractFee;
    }

    public double getDeathFee() {
        return deathFee;
    }

    public void setDeathFee(double deathFee) {
        this.deathFee = deathFee;
    }

    public boolean shouldPayInconvenience() {
        return payInconvenience;
    }

    public void setPayInconvenience(boolean payInconvenience) {
        this.payInconvenience = payInconvenience;
    }

    public boolean usesAnonymousTargets() {
        return anonymousTargets;
    }

    public void setAnonymousTargets(boolean anonymousTargets) {
        this.anonymousTargets = anonymousTargets;
    }

    public boolean isNegativeBalances() {
        return negativeBalances;
    }

    public void setNegativeBalances(boolean negativeBalances) {
        this.negativeBalances = negativeBalances;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getLocationRounding() {
        return locationRounding;
    }

    public void setLocationRounding(int locationRounding) {
        this.locationRounding = locationRounding;
    }

    public class ExpirationChecker extends TimerTask {

        private HeroBounty plugin;

        public ExpirationChecker(HeroBounty plugin) {
            this.plugin = plugin;
        }

        public HeroBounty getPlugin() {
            return plugin;
        }

        @Override
        public void run() {
            Bounty[] bounties = plugin.getBountyManager().getBounties().toArray(new Bounty[0]);
            for (Bounty bounty : bounties) {
                String[] hunters = bounty.getExpirations().keySet().toArray(new String[0]);
                for (String hunterName : hunters) {
                    if (bounty.getMillisecondsLeft(hunterName) <= 0) {
                        Player hunter = plugin.getServer().getPlayer(hunterName);
                        bounty.removeHunter(hunterName);
                        plugin.saveData();
                        if (hunter != null) {
                            Messaging.send(plugin, hunter, "Your bounty on $1 has expired.", bounty.getTargetDisplayName());
                        }
                    }
                }
            }
        }

        public void setPlugin(HeroBounty plugin) {
            this.plugin = plugin;
        }
    }
}
