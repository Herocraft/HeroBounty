/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty.bounties;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

public class Bounty implements Comparable<Bounty> {
    private String owner = "";
    private String target = "";
    private String ownerDisplayName = "";
    private String targetDisplayName = "";
    private List<String> hunters = new ArrayList<String>();
    private HashMap<String, Date> expirations = new HashMap<String, Date>();
    private Point2D targetLocation = new Point2D.Double();
    private int value = 0;
    private int postingFee = 0;
    private int deathPenalty = 0;
    private int contractFee = 0;
    private Date creationDate;

    public Bounty() {}

    public Bounty(String owner, String ownerDisplayName, String target, String targetDisplayName, int value, int postingFee, int contractFee, int deathPenalty) {
        this.owner = owner;
        this.ownerDisplayName = ownerDisplayName;
        this.target = target;
        this.targetDisplayName = targetDisplayName;
        this.value = value;
        this.setPostingFee(postingFee);
        this.contractFee = contractFee;
        this.deathPenalty = deathPenalty;
        this.creationDate = new Date();
    }
    
    public void addHunter(Player player) {
        addHunter(player.getName());
    }

    public void addHunter(String name) {
        hunters.add(name);
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

    public int getContractFee() {
        return contractFee;
    }

    public int getDeathPenalty() {
        return deathPenalty;
    }
    
    public Date getExpiration(Player hunter) {
        return getExpiration(hunter.getName());
    }

    public Date getExpiration(String hunter) {
        return expirations.get(hunter);
    }

    public HashMap<String, Date> getExpirations() {
        return expirations;
    }

    public List<String> getHunters() {
        return hunters;
    }

    public long getMillisecondsLeft(String hunter) {
        Date now = new Date();

        long diff = expirations.get(hunter).getTime() - now.getTime();

        return diff;
    }

    public int getMinutesLeft(String hunter) {
        return (int) Math.ceil(getMillisecondsLeft(hunter) / (1000 * 60));
    }

    public String getOwner() {
        return owner;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public int getPostingFee() {
        return postingFee;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetDisplayName() {
        return targetDisplayName;
    }

    public int getValue() {
        return value;
    }

    public boolean isHunter(String name) {
        for (String hunter : hunters)
            if (hunter.equalsIgnoreCase(name))
                return true;
        return false;
    }
    
    public boolean isHunter(Player player) {
        return isHunter(player.getName());
    }

    public void removeHunter(String name) {
        hunters.remove(name);
        expirations.remove(name);
    }
    
    public void removeHunter(Player player) {
        removeHunter(player.getName());
    }

    public void setContractFee(int contractFee) {
        this.contractFee = contractFee;
    }

    public void setDeathPenalty(int deathPenalty) {
        this.deathPenalty = deathPenalty;
    }
    
    public void setExpiration(Player player, int duration) {
        setExpiration(player.getName(), duration);
    }
    
    public void setExpiration(String name, int duration) {
        GregorianCalendar expiration = new GregorianCalendar();
        expiration.add(Calendar.MINUTE, duration);
        expirations.put(name, expiration.getTime());
    }

    public void setExpirations(HashMap<String, Date> expirations) {
        this.expirations = expirations;
    }

    public void setHunters(List<String> hunters) {
        this.hunters = hunters;
    }
    
    public boolean isOwner(String owner) {
        return this.owner.equals(owner);
    }
    
    public boolean isOwner(Player player) {
        return isOwner(player.getName());
    }
    
    public void setOwner(Player player) {
        setOwner(player.getName());
        setOwnerDisplayName(player.getDisplayName());
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }

    public void setPostingFee(int postingFee) {
        this.postingFee = postingFee;
    }
    
    public void setTarget(Player player) {
        setTarget(player.getName());
        setTargetDisplayName(player.getDisplayName());
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetDisplayName(String targetDisplayName) {
        this.targetDisplayName = targetDisplayName;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setTargetLocation(Point2D targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Point2D getTargetLocation() {
        return targetLocation;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }
}
