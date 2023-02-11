package me.hapily.plugins.mhcosmeticsdisabler;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class MHCosmeticsDisabler extends JavaPlugin {


    @SuppressWarnings("unchecked")
    @Override
    public void onLoad() {
        Logger logger = getLogger();
        logger.info("Initializing config...");
        FileConfiguration config = getConfig();
        config.options().copyDefaults();
        saveDefaultConfig();
        try {
            List<String> targetPluginNames = config.getStringList("disable");
            logger.info("Config target plugins: " + formattedList(targetPluginNames));
            PluginManager pluginManager = Bukkit.getPluginManager();
            Class<?> pluginManagerClass = pluginManager.getClass();
            Field pluginsField = pluginManagerClass.getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            List<Plugin> plugins = (List<Plugin>) pluginsField.get(pluginManager);

            logger.info(getPluginList(plugins));
            for (String pluginName : targetPluginNames) {
                pluginName = pluginName.replace(' ', '_');
                Plugin targetPlugin = pluginManager.getPlugin(pluginName);
                if (targetPlugin == null) {
                    logger.severe("'" + pluginName + "'" + " plugin has not been found.");
                }
                else {
                    deletePlugin(plugins, targetPlugin, logger);
                }
            }
            deletePlugin(plugins, this, logger);
            logger.info(getPluginList(plugins));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void deletePlugin(List<Plugin> plugins, Plugin plugin, Logger logger) {
        String name = plugin.getDescription().getName();
        logger.info("Deleting plugin '" + name + "'...");
        plugins.remove(plugin);
    }

    private String getPluginList(List<Plugin> pls) {
        List<String> plugins = new ArrayList<>();
        for (Plugin plugin : pls) {
            plugins.add(plugin.getDescription().getName());
        }
        return "(" + plugins.size() + "): " + formattedList(plugins);
    }

    private String formattedList(List<String> strings) {
        StringBuilder formattedList = new StringBuilder();
        for (String s : strings) {
            if (formattedList.length() > 0) {
                formattedList.append(", ");
            }
            formattedList.append(s);
        }
        return formattedList.toString();
    }

}
