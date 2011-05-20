package com.herocraftonline.dthielke.herobounty;

import java.util.logging.Level;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;

public class HeroBountyServerListener extends ServerListener {

    private HeroBounty plugin;
    private Methods methods;
    
    public HeroBountyServerListener(HeroBounty plugin) {
        this.plugin = plugin;
        this.methods = new Methods();
    }
    
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        String name = plugin.getDescription().getName();
        
        if (name.equals("Permissions")) {
            this.plugin.loadPermissions();
            return;
        }
        
        if (!methods.hasMethod()) {
            if (methods.setMethod(plugin)) {
                Method method = methods.getMethod();
                this.plugin.setRegister(method);
                this.plugin.log(Level.INFO, "Payment method found (" + method.getName() + " version: " + method.getVersion() + ")");
            }
        }
    }

}
