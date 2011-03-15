package com.herocraftonline.dthielke.herobounty.command.commands;

import org.bukkit.command.CommandSender;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;

public class HelpCommand extends BaseCommand {

    public HelpCommand(HeroBounty plugin) {
        super(plugin);
        name = "Help";
        description = "Displays a list of commands";
        usage = "/bounty help";
        minArgs = 0;
        maxArgs = 0;
        identifiers.add("bounty help");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§c-----[ " + "§fHero Bounty Help " + "§c ]-----");
        sender.sendMessage("§e/bounty help - Show this information.");
        sender.sendMessage("§e/bounty new <player> <value> - Create a new bounty.");
        sender.sendMessage("§e/bounty list <page#> - List bounties available.");
        sender.sendMessage("§e/bounty accept <id#> - Accept bounty by id.");
        sender.sendMessage("§e/bounty abandon <id#> - Abandon bounty by id.");
        sender.sendMessage("§e/bounty cancel <id#> - Cancel bounty by id.");
        sender.sendMessage("§e/bounty view - View your accepted bounties.");
        sender.sendMessage("§e/bounty locate - Show last known target locations.");
    }

}
