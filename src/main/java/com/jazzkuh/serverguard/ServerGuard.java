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

import java.util.ArrayList;

public class ServerGuard extends JavaPlugin {

    public static @Getter @Setter(AccessLevel.PRIVATE) ServerGuard instance;
    public @Getter ArrayList<PluginInformation> knownPlugins = new ArrayList<>();
    public @Getter ArrayList<PluginInformation> lookedUpPlugins = new ArrayList<>();

    @Override
    public void onEnable() {
        setInstance(this);

        new ServerGuardCMD().register(this);

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
                        this.getLogger().warning("Disabled plugin " + pluginInformation.getName() + " because it was flagged in the database as malicious.");
                        Bukkit.getPluginManager().disablePlugin(plugin);
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
