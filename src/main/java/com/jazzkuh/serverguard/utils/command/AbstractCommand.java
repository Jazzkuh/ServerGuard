package com.jazzkuh.serverguard.utils.command;

import com.jazzkuh.serverguard.ServerGuard;
import com.jazzkuh.serverguard.utils.PluginUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class AbstractCommand implements TabExecutor {
    public CommandSender sender;
    public Command command;
    public String commandName;
    public Argument[] arguments;

    public AbstractCommand(String commandName) {
        this.commandName = commandName;
        this.arguments = new Argument[]{};
    }

    public AbstractCommand(String commandName, Argument... arguments) {
        this.commandName = commandName;
        this.arguments = arguments;
    }

    public void register(ServerGuard plugin) {
        PluginCommand cmd = plugin.getCommand(commandName);
        if (cmd != null) {
            cmd.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.command = command;
        this.sender = sender;
        this.execute(sender, command, label, args);
        return true;
    }

    public abstract void execute(CommandSender sender, Command command, String label, String[] args);

    protected boolean senderIsPlayer() {
        return sender instanceof Player;
    }

    protected boolean hasPermission(String permission, boolean silent) {
        if (sender.hasPermission(permission)) {
            return true;
        }

        if (!silent) {
            sender.sendMessage(PluginUtils.color("&cTo use this command, you need permission " + permission + "."));
        }
        return false;
    }

    protected void addIfPermission(Collection<String> options, String permission, String option) {
        if (sender.hasPermission(permission)) {
            options.add(option);
        }
    }

    protected String getBasePermission() {
        return "serverguard.command." + commandName;
    }

    protected void sendNotEnoughArguments(AbstractCommand command) {
        for (Argument argument : this.arguments) {
            if (argument.getPermission() == null || sender.hasPermission(argument.getPermission())) {
                sender.sendMessage(PluginUtils.color("&6/" + command.command.getName() + " " + argument.getArguments() + "&8 - &7" + argument.getDescription()));
            }
        }
    }

    protected List<String> getApplicableTabCompleters(String arg, Collection<String> completions) {
        return StringUtil.copyPartialMatches(arg, completions, new ArrayList<>(completions.size()));
    }
}