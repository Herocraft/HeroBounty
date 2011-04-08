package com.herocraftonline.dthielke.herobounty.util;

import org.bukkit.entity.Player;

import com.nijiko.permissions.PermissionHandler;

public class PermissionWrapper {

    private PermissionHandler security;
    private boolean respectUntargettables;

    public PermissionWrapper(PermissionHandler security) {
        this.security = security;
        this.respectUntargettables = true;
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
        if (respectUntargettables && security != null) {
            return !security.has(p, "herobounty.untargettable");
        } else {
            return true;
        }
    }
    
    public boolean canViewBountyList(Player p) {
        if (security != null) {
            return security.has(p, "herobounty.list");
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

    public void setRespectUntargettables(boolean respectUntargettables) {
        this.respectUntargettables = respectUntargettables;
    }
    
}
