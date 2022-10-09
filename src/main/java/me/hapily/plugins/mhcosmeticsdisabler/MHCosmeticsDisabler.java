package me.hapily.plugins.mhcosmeticsdisabler;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.accessibility.Accessible;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MHCosmeticsDisabler extends JavaPlugin {

    @Override
    public void onEnable() {
        Server server = getServer();
        PluginManager manager = server.getPluginManager();
        Plugin cosmeticsPlugin = manager.getPlugin("MinehutCosmetics");
        Logger logger = getLogger();
        if (cosmeticsPlugin != null) {
            try {
                SimpleCommandMap simpleCommandMap = (SimpleCommandMap) getCommandMap();
                String[] disabledCommands = disableCosmeticCommands(simpleCommandMap);
                logger.info("Successfully disabled commands: " + prettyDisabledCommands(disabledCommands));
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.warning("Couldn't get the command map for some reason, unable to disable MinehutCosmetics commands.");
            }
            manager.disablePlugin(cosmeticsPlugin);
            logger.info("MinehutCosmetics plugin has been found, disabled.");
        }
        else {
            logger.severe("MinehutCosmetics plugin has not been found.");
        }
        logger.info("Disabling plugin...");
        manager.disablePlugin(this);
    }

    private CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

        bukkitCommandMap.setAccessible(true);
        return (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
    }

    private String prettyDisabledCommands(String[] commands) {
        return String.join(", ", commands);
    }

    private String[] disableCosmeticCommands(SimpleCommandMap commandMap) {
        List<String> commands = new ArrayList<>();
        HashMap<String, Command> knownCommands = (HashMap<String, Command>) getKnownCommands(commandMap);
        assert knownCommands != null;
        String[] mhCommandLabels = getMHCommandLabels(knownCommands);
        for (String label :mhCommandLabels) {
            knownCommands.remove(label);
            commands.add(label);
        }
        return commands.toArray(new String[0]);
    }

    public String[] getMHCommandLabels(Map<String, Command> knownCommands) {
        List<String> labels = new ArrayList<>();
        for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
            String label = entry.getKey();
            if (label.startsWith("minehutcosmetics")) {
                labels.add(label);
            }
        }
        List<String> labels2 = new ArrayList<>(); // bypass concurrentmodification
        // Going through the entry set again to get the commands that don't have the minehutcosmetics prefix
        for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
            for (String alreadyLabel : labels) {
                String label = entry.getKey();
                if (alreadyLabel.replaceAll("minehutcosmetics:", "").equals(label)) {
                    labels2.add(label);
                }
            }
        }
        labels.addAll(labels2);
        return labels.toArray(new String[0]);
    }

    private Map<String, Command> getKnownCommands(SimpleCommandMap simpleCommandMap) {
        try {
            final Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");

            knownCommands.setAccessible(true);
            return (Map<String, Command>) knownCommands.get(simpleCommandMap);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
