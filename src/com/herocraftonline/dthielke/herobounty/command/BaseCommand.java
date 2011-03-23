/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 * 
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.herocraftonline.dthielke.herobounty.HeroBounty;

public abstract class BaseCommand {

    protected HeroBounty plugin;
    protected String name;
    protected String description;
    protected String usage;
    protected int minArgs;
    protected int maxArgs;
    protected List<String> identifiers;
    protected List<String> notes;

    public BaseCommand(HeroBounty plugin) {
        this.plugin = plugin;
        this.identifiers = new ArrayList<String>();
        this.notes = new ArrayList<String>();
    }

    public abstract void execute(CommandSender sender, String[] args);

    public String[] validate(String input, StringBuilder identifier) {
        String match = matchIdentifier(input);

        if (match != null) {
            identifier = identifier.append(match);
            int i = identifier.length();
            String[] args = input.substring(i).trim().split(" ");
            if (args[0].isEmpty()) {
                args = new String[0];
            }
            int l = args.length;
            if (l >= minArgs && l <= maxArgs) {
                return args;
            }
        }
        return null;
    }

    public String matchIdentifier(String input) {
        String lower = input.toLowerCase();

        int index = -1;
        int n = identifiers.size();
        for (int i = 0; i < n; i++) {
            String identifier = identifiers.get(i).toLowerCase();
            if (lower.matches(identifier + "(\\s+.*|\\s*)")) {
                if (index == -1 || identifier.length() > identifiers.get(index).length()) {
                    index = i;
                }
            }
        }

        if (index != -1) {
            return identifiers.get(index);
        } else {
            return null;
        }
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getNotes() {
        return notes;
    }

}
