package com.bukkit.dthielke.herobounty;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.util.Vector;

public class Bounty implements Comparable<Bounty> {
    private String owner = "";
    private String target = "";
    private String ownerDisplayName = "";
    private String targetDisplayName = "";
    private Vector targetLocation = new Vector();
    private List<String> hunters = new ArrayList<String>();
    private HashMap<String, Date> expirations = new HashMap<String, Date>();
    private int value = 0;
    private int deathPenalty = 0;
    private int contractFee = 0;

    public Bounty() {}
    
    public Bounty(String owner, String ownerDisplayName, String target, String targetDisplayName, int value, int contractFee, int deathPenalty) {
        this.owner = owner;
        this.ownerDisplayName = ownerDisplayName;
        this.target = target;
        this.targetDisplayName = targetDisplayName;
        this.value = value;
        this.contractFee = contractFee;
        this.deathPenalty = deathPenalty;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getDeathPenalty() {
        return deathPenalty;
    }

    public void setDeathPenalty(int deathPenalty) {
        this.deathPenalty = deathPenalty;
    }

    public int getContractFee() {
        return contractFee;
    }

    public void setContractFee(int contractFee) {
        this.contractFee = contractFee;
    }

    @Override
    public int compareTo(Bounty o) {        
        int oValue = o.getValue();
        
        if (value < oValue)
            return 1;
        else if (value > oValue)
            return -1;
        else
            return 0;
    }

    public List<String> getHunters() {
        return hunters;
    }

    public void setHunters(List<String> hunters) {
        this.hunters = hunters;
    }
    
    public boolean isHunter(String name) {
        for (String hunter : hunters)
            if (hunter.equalsIgnoreCase(name))
                return true;
        return false;
    }
    
    public void addHunter(String name) {
        hunters.add(name);
    }
    
    public void removeHunter(String name) {
        hunters.remove(name);
        expirations.remove(name);
    }
    
    public long getMillisecondsLeft(String hunter) {
        Date now = new Date();

        long diff = expirations.get(hunter).getTime() - now.getTime();

        return diff;
    }
    
    public int getMinutesLeft(String hunter) {
        return (int)Math.ceil(getMillisecondsLeft(hunter) / (1000 * 60));
    }
    
    public Date getExpiration(String hunter) {
        return expirations.get(hunter);
    }

    public HashMap<String, Date> getExpirations() {
        return expirations;
    }

    public void setExpirations(HashMap<String, Date> expirations) {
        this.expirations = expirations;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }

    public String getTargetDisplayName() {
        return targetDisplayName;
    }

    public void setTargetDisplayName(String targetDisplayName) {
        this.targetDisplayName = targetDisplayName;
    }

    public void setTargetLocation(Vector targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Vector getTargetLocation() {
        return targetLocation;
    }
}

