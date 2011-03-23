package com.herocraftonline.dthielke.herobounty.util;

import org.bukkit.entity.Player;

import com.nijiko.permissions.PermissionHandler;

public class PermissionManager {

    private PermissionHandler security;

    public PermissionManager(PermissionHandler security) {
        this.security = security;
    }

    public boolean canCreateBounty(Player p) {
        if (security != null) {
            return security.has(p, "herobounty.new");
        } else {
            return true;
        }
    }
    
    public boolean canAcceptBounty(Player p) {
        if (security != null) {
            return security.has(p, "herobounty.accept");
        } else {
            return true;
        }
    }
    
    public boolean canBeTargetted(Player p) {
        if (security != null) {
            return !security.has(p, "herobounty.untargettable");
        } else {
            return true;
        }
    }
    
    public boolean canLocateTargets(Player p) {
        if (security != null) {
            return security.has(p, "herobounty.locate");
        } else {
            return true;
        }
    }
    
}
