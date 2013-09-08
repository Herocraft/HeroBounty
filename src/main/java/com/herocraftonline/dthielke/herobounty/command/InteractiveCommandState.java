/*
 * Copyright (c) 2012 David "DThielke" Thielke <dave.thielke@gmail.com>.
 * All rights reserved.
 */

package com.herocraftonline.dthielke.herobounty.command;

import org.bukkit.command.CommandSender;

public interface InteractiveCommandState {
    boolean execute(CommandSender executor, String identifier, String[] args);

    int getMaxArguments();

    int getMinArguments();

    boolean isIdentifier(String input);
}
