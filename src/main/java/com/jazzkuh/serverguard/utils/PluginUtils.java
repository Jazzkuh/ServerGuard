package com.jazzkuh.serverguard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jazzkuh.serverguard.ServerGuard;
import com.jazzkuh.serverguard.framework.PluginInformation;
import com.jazzkuh.serverguard.framework.PluginStatus;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PluginUtils {
    static {
        JsonObject jsonObject = getJSON("https://dash.mtwapens.nl/api/serverguard", "GET");
        if (jsonObject != null && jsonObject.get("success").getAsBoolean()) {
            jsonObject.getAsJsonArray("plugins").forEach(plugin -> {
                JsonObject pluginObject = plugin.getAsJsonObject();
                String pluginName = pluginObject.get("pluginName").getAsString();
                JsonArray jsonArray = pluginObject.getAsJsonArray("pluginAuthors");
                List<String> pluginAuthors = new ArrayList<>();
                for (JsonElement jsonElement : jsonArray) {
                    pluginAuthors.add(jsonElement.getAsString());
                }

                PluginStatus pluginState = PluginStatus.valueOf(pluginObject.get("state").getAsString());
                String reason = pluginObject.get("reason").getAsString();

                ServerGuard.getInstance().getKnownPlugins().add(new PluginInformation(pluginName, pluginAuthors, reason, pluginState));
            });
        }
    }

    public static JsonObject getJSON(String url, String method) {
        try {
            HttpURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", "ServerGuard");
            connection.connect();

            return new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent())).getAsJsonObject();
        } catch (IOException ignored) {
        }

        return null;
    }

    public static void sendMessage(CommandSender sender, String input) {
        sender.sendMessage(color(input));
    }

    public static String color(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }

    public static @Nullable PluginInformation getDatabasePluginInfo(Plugin plugin) {
        for (PluginInformation pluginInformation : ServerGuard.getInstance().getKnownPlugins()) {
            if (plugin.getName().equals(pluginInformation.getName()) && plugin.getDescription().getAuthors().equals(pluginInformation.getAuthors())) {
                return pluginInformation;
            }
        }

        return null;
    }

    public static @Nullable PluginInformation getKnownPluginInfo(Plugin plugin) {
        for (PluginInformation pluginInformation : ServerGuard.getInstance().getLookedUpPlugins()) {
            if (plugin.getName().equals(pluginInformation.getName())) {
                if (pluginInformation.getAuthors().isEmpty()) return pluginInformation;
                if (!pluginInformation.getAuthors().equals(plugin.getDescription().getAuthors())) continue;
                return pluginInformation;
            }
        }

        return null;
    }

    public static String getPluginStatus(PluginStatus pluginStatus) {
        switch (pluginStatus) {
            case SAFE:
                return "&aSafe (Verified)";
            case MALICIOUS:
                return "&cMalicious";
            default:
                return "&eUnknown";
        }
    }
}
