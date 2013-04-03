package com.herocraftonline.dthielke.herobounty.bounties;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class BountyManager {

    private static final long EXPIRATION_DELAY = 10 * 1000;
    private static final long EXPIRATION_PERIOD = 5 * 60 * 1000;

    private HeroBounty plugin;
    private List<Bounty> bounties;
    private double minimumValue;
    private double placementFee;
    private double contractFee;
    private double deathFee;
    private double cancellationFee;
    private boolean payInconvenience;
    private boolean anonymousTargets;
    private int duration;
    private int locationRounding;
    private int acceptDelay;

    public BountyManager(HeroBounty plugin) {
        this.plugin = plugin;
    }
    
    public static String getBountyExpirationString(int duration) {
        int d = duration;
        return (d < 60) ? d + " minutes" : (d < (60 * 24)) ? d / 60 + " hours" : (d < (60 * 24 * 7)) ? d / (60 * 24) + " days" : d / (60 * 24 * 7) + " weeks";
    }
    
    public boolean addBounty(Bounty bounty) {
        if (isTarget(bounty.getTarget()))
            return false;
        else
            bounties.add(bounty);
        return true;
    }
    
    public void sortBounties() {
        Collections.sort(bounties);
    }
    
    public boolean removeBounty(Bounty bounty) {
        return bounties.remove(bounty);
    }
    
    public String getBountyExpirationString() {
        return getBountyExpirationString(duration);
    }

    public void startExpirationTimer() {
        TimerTask expirationChecker = new ExpirationChecker(plugin);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, expirationChecker, EXPIRATION_DELAY, EXPIRATION_PERIOD);
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

        HeroBounty.economy.withdrawPlayer(target.getName(), bounty.getDeathPenalty());
        HeroBounty.economy.depositPlayer(hunter.getName(), bounty.getValue());

        Messaging.broadcast("§7[§eBounty§7] §e$1 has collected a bounty on $2 for $3!", hunter.getDisplayName(), bounty.getTargetDisplayName(), HeroBounty.economy.format(bounty.getValue()));
        return true;
    }
    
    public Bounty getBountyOn(Player player) {
        return getBountyOn(player.getName());
    }
    
    public Bounty getBountyOn(String name) {
        for (Bounty bounty : bounties) {
            if (bounty.getTarget().equalsIgnoreCase(name)) {
                return bounty;
            }
        }
        return null;
    }

    public boolean isTarget(Player player) {
        return isTarget(player.getName());
    }
    
    public boolean isTarget(String name) {
        for (Bounty bounty : bounties) {
            if (bounty.getTarget().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public List<Bounty> getBountiesAcceptedBy(String hunter) {
        List<Bounty> acceptedBounties = new ArrayList<Bounty>();
        for (Bounty bounty : bounties) {
            if (bounty.isHunter(hunter)) {
                acceptedBounties.add(bounty);
            }
        }
        return acceptedBounties;
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

    public double getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(double cancellationFee) {
        this.cancellationFee = cancellationFee;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAcceptDelay() {
        return acceptDelay;
    }

    public void setAcceptDelay(int acceptDelay) {
        this.acceptDelay = acceptDelay;
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
                            Messaging.send(hunter, "§7[§eBounty§7] §eYour bounty on $1 has expired.", bounty.getTargetDisplayName());
                        }
                    }
                }
            }
        }

        public void setPlugin(HeroBounty plugin) {
            this.plugin = plugin;
        }
    }

    public boolean isAcceptDelayDone(Bounty bounty) {
        return Calendar.getInstance().getTime().after(getAcceptDelayDate(bounty));
    }

    public Date getAcceptDelayDate(Bounty bounty) {
        if (bounty.getCreationDate() != null) {
            return DateUtils.addMinutes(bounty.getCreationDate(), acceptDelay);
        }

        return DateUtils.addMinutes(new Date(), -acceptDelay);
    }
}