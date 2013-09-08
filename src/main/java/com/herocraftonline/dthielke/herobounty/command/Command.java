/*
 * Copyright (c) 2012 David "DThielke" Thielke <dave.thielke@gmail.com>.
 * All rights reserved.
 */

package com.herocraftonline.dthielke.herobounty.command;

import org.bukkit.command.CommandSender;

public interface Command {
    void cancelInteraction(CommandSender executor);

    boolean execute(CommandSender executor, String identifier, String[] args);

    String getDescription();

    String[] getIdentifiers();

    int getMaxArguments();

    int getMinArguments();

    String getName();

    String[] getNotes();

    String getPermission();

    String getUsage();

    boolean isIdentifier(CommandSender executor, String input);

    boolean isInProgress(CommandSender executor);

    boolean isInteractive();

    boolean isShownOnHelpMenu();
}
