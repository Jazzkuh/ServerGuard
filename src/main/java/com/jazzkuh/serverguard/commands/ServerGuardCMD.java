package com.jazzkuh.serverguard.commands;

import com.jazzkuh.serverguard.framework.PluginInformation;
import com.jazzkuh.serverguard.utils.PluginUtils;
import com.jazzkuh.serverguard.utils.command.AbstractCommand;
import com.jazzkuh.serverguard.utils.command.Argument;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class ServerGuardCMD extends AbstractCommand {

    public ServerGuardCMD() {
        super("serverguard",
                new Argument("report <plugin>", "Get a detailed report of a plugin."),
                new Argument("plugins", "Get a list of plugins with their status."));
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!senderIsPlayer()) return;
        if (!hasPermission(getBasePermission(), false)) return;

        Player player = (Player) sender;

        if (args.length < 1) {
            sendNotEnoughArguments(this);
            return;
        }

        switch (args[0]) {
            case "report": {
                if (args.length < 2) {
                    sendNotEnoughArguments(this);
                    return;
                }

                String pluginName = args[1];
                if (pluginName == null) {
                    sendNotEnoughArguments(this);
                    return;
                }

                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (plugin == null) {
                    PluginUtils.sendMessage(player, "&cThe plugin &4" + pluginName + "&c is not known to the ServerGuard plugin.");
                    return;
                }

                PluginInformation pluginInformation = PluginUtils.getKnownPluginInfo(plugin);
                if (pluginInformation == null) {
                    PluginUtils.sendMessage(player, "&cThe plugin &4" + pluginName + "&c is not known to the ServerGuard plugin.");
                    return;
                }

                String builder =
                        "&8-------------------------------------------------\n" +
                        "&6Plugin report for " + pluginName + ":\n\n" +
                        "&6Authors: &7" + StringUtils.join(pluginInformation.getAuthors(), ", ") + "\n" +
                        "&6Main Class: &7" + plugin.getClass().getName() + "\n" +
                        "&6Version: &7" + plugin.getDescription().getVersion() + "\n" +
                        "&6Status: &7" + PluginUtils.getPluginStatus(pluginInformation.getStatus()) + "\n" +
                        "&6Details: " + "\n" +
                        " &7" + pluginInformation.getReason() + "\n" +
                        "&8-------------------------------------------------\n";
                PluginUtils.sendMessage(player, builder);
                break;
            }
            case "plugins": {
                ArrayList<String> plugins = new ArrayList<>();
                for(Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                    PluginInformation pluginInformation = PluginUtils.getKnownPluginInfo(plugin);
                    plugins.add(PluginUtils.color(PluginUtils.getStatusColor(pluginInformation.getStatus()) + plugin.getName()));
                }
                String pluginsText = plugins.toString().replace("[", "").replace("]", "");
                player.sendMessage(PluginUtils.color("&6Plugins &7(" + plugins.size() + "&7): &7" + pluginsText));
                break;
            }
            default: {
                sendNotEnoughArguments(this);
                break;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(getBasePermission())) return Collections.emptyList();

        if (args.length == 1) {
            return getApplicableTabCompleters(args[0],
                    Collections.singletonList("report"));
        }

        if (args.length == 2) {
            return getApplicableTabCompleters(args[1], Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList()));
        }

        return Collections.emptyList();
    }
}