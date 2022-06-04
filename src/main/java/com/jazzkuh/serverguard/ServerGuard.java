package com.jazzkuh.serverguard;

import com.jazzkuh.serverguard.commands.ServerGuardCMD;
import com.jazzkuh.serverguard.framework.PluginInformation;
import com.jazzkuh.serverguard.framework.PluginStatus;
import com.jazzkuh.serverguard.utils.PluginUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;

public class ServerGuard extends JavaPlugin {

    public static @Getter @Setter(AccessLevel.PRIVATE) ServerGuard instance;
    public @Getter ArrayList<PluginInformation> knownPlugins = new ArrayList<>();
    public @Getter ArrayList<PluginInformation> lookedUpPlugins = new ArrayList<>();
    public @Getter ArrayList<String> infectedFiles = new ArrayList<>();

    @Override
    public void onEnable() {
        setInstance(this);

        new ServerGuardCMD().register(this);

        try {
            Files.walk(FileSystems.getDefault().getPath("plugins"), new java.nio.file.FileVisitOption[0]).forEach(path -> {
                if (PluginUtils.scanFile(path.toFile())) {
                    this.getLogger().severe("Plugin " + path.toFile() + " contains traces of hostflow malware. We have disabled the server to prevent further damage.");
                    System.exit(0);
                }
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin == this) {
                    lookedUpPlugins.add(new PluginInformation(plugin.getName(), plugin.getDescription().getAuthors(), "ServerGuard is a verified plugin.", PluginStatus.SAFE));
                    continue;
                }

                PluginInformation pluginInformation = PluginUtils.getDatabasePluginInfo(plugin);
                if (pluginInformation == null) {
                    lookedUpPlugins.add(new PluginInformation(plugin.getName(), plugin.getDescription().getAuthors(), "Plugin is not in the database.", PluginStatus.UNKNOWN));
                    continue;
                }

                switch (pluginInformation.getStatus()) {
                    case MALICIOUS: {
                        lookedUpPlugins.add(pluginInformation);
                        this.getLogger().severe("Disabled plugin " + pluginInformation.getName() + " because it was flagged in the database as malicious.");
                        Bukkit.getPluginManager().disablePlugin(plugin);
                        break;
                    }
                    case WARNING: {
                        lookedUpPlugins.add(pluginInformation);
                        this.getLogger().warning("The plugin " + pluginInformation.getName() + " was flagged in the database as " + pluginInformation.getStatus().name().toLowerCase() + ". Details: " + pluginInformation.getReason());
                        break;
                    }
                    case SAFE:
                    default: {
                        lookedUpPlugins.add(pluginInformation);
                        break;
                    }
                }
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
    }
}
