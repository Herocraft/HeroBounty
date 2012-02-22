package com.herocraftonline.dthielke.herobounty.command.commands;

import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BasicCommand;
import com.herocraftonline.dthielke.herobounty.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand extends BasicCommand {
    private static final int CMDS_PER_PAGE = 8;
    private final HeroBounty plugin;

    public HelpCommand(HeroBounty plugin) {
        super("Help");
        setDescription("Displays a list of commands");
        setUsage("§e/bounty help");
        setArgumentRange(0, 0);
        setIdentifiers("bounty help", "bounty");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        int page = 0;
        if (args.length != 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException e) {
            }
        }

        List<Command> commands = plugin.getCommandHandler().getCommands();

        int numPages = commands.size() / CMDS_PER_PAGE;
        if (commands.size() % CMDS_PER_PAGE != 0) {
            numPages++;
        }

        if (page >= numPages || page < 0) {
            page = 0;
        }
        sender.sendMessage("§c-----[ " + "§fHeroBounty Help <" + (page + 1) + "/" + numPages + ">§c ]-----");
        int start = page * CMDS_PER_PAGE;
        int end = start + CMDS_PER_PAGE;
        if (end > commands.size()) {
            end = commands.size();
        }
        for (int c = start; c < end; c++) {
            Command cmd = commands.get(c);
            sender.sendMessage("  §a" + cmd.getUsage());
        }

        sender.sendMessage("§cFor more info on a particular command, type §f/<command> ?§c.");
        return true;
    }

}
