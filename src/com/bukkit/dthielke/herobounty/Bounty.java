package com.bukkit.dthielke.herobounty;

public class Bounty implements Comparable<Bounty> {
    private String owner = "";
    private String target = "";
    private String hunter = "";
    private int value = 0;
    private int deathPenalty = 0;
    private int contractFee = 0;

    public Bounty() {}
    
    public Bounty(String owner, String target, int value, int contractFee, int deathPenalty) {
        this.owner = owner;
        this.target = target;
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

    public String getHunter() {
        return hunter;
    }

    public void setHunter(String hunter) {
        this.hunter = hunter;
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
}

