package org.twistermc.contraband;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.twistermc.contraband.crate.command.ContrabandCommand;
import org.twistermc.contraband.crate.listener.ContrabandListener;
import org.twistermc.contraband.utils.Config;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class Main extends JavaPlugin {
    private static Main INSTANCE;
    private final File configuration = new File(getDataFolder(), "configuration.yml");
    private final FileConfiguration configConfiguration = new YamlConfiguration();
    private final File data = new File(getDataFolder(), "data.yml");
    private final FileConfiguration dataConfiguration = new YamlConfiguration();
    private final File location = new File(getDataFolder(), "locations.yml");
    private final FileConfiguration locationConfiguration = new YamlConfiguration();
    public static boolean running = false;
    public static UUID uuid = null;
    public static String tier = null;
    public static int amount = 0;
    public static int allowedAmount;

    public static void reset() {
        Main.running = false;
        Main.uuid = null;
        Main.tier = null;
        Main.amount = 0;
        Main.allowedAmount = Main.getINSTANCE().getConfigConfiguration().getInt("CHEST-LIMIT");
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.configuration();
        this.command();
        this.listener(Bukkit.getPluginManager());
        allowedAmount = this.getConfigConfiguration().getInt("CHEST-LIMIT");
    }

    public static Main getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        INSTANCE = null;
    }

    private void configuration(){
        new Config(configuration, configConfiguration, "configuration.yml");
        new Config(data, dataConfiguration, "data.yml");
        new Config(location, locationConfiguration, "locations.yml");
    }

    private void command(){
        Objects.requireNonNull(getCommand("contraband")).setExecutor(new ContrabandCommand(this));
    }

    private void listener(PluginManager manager){
        manager.registerEvents(new ContrabandListener(this),this);
    }
}
