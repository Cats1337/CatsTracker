package io.github.cats1337.CatsTracker;

import io.github.cats1337.CatsTracker.Events.AdvancementListener;
import io.github.cats1337.CatsTracker.Events.FishListener;
import io.github.cats1337.CatsTracker.Events.MobListener;
import io.github.cats1337.CatsTracker.Events.PlayerListener;
import io.github.cats1337.CatsTracker.commands.PointsCommand;
import io.github.cats1337.CatsTracker.commands.SizeCommand;
import io.github.cats1337.CatsTracker.commands.UtilCommands;
import io.github.cats1337.CatsTracker.playerdata.PlayerContainer;
import io.github.cats1337.CatsTracker.utils.*;
import io.github.cats1337.CatsTracker.playerdata.PlayerHandler;
import com.marcusslover.plus.lib.command.CommandManager;
import com.marcusslover.plus.lib.container.ContainerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class CatsTracker extends JavaPlugin {
    public static Logger log;
    private CommandManager cmdManager;
    private ContainerManager containerManager;
    private PlaceholdersFish fishPlaceholder;
    private PlaceholdersAdv advPlaceholder;
    private PlaceholdersMob mobPlaceholder;
    private PlaceholdersPurge purgePlaceholder;
    private PointLogger pointLogger;

    public static CatsTracker getInstance() {
        return CatsTracker.getPlugin(CatsTracker.class);
    }

    @Override
    public void onEnable() {
        checkAndResetConfig();

        executeSettings();

        log = getInstance().getLogger();

        this.pointLogger = PointLogger.getInstance(); // initialize pointLogger
        this.pointLogger.fileLogger(); // initialize fileLogger
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aPointLogger initialized");

        // Register commands
        cmdManager = CommandManager.get(this);
        cmdManager.register(new PointsCommand());
        cmdManager.register(new UtilCommands());
        cmdManager.register(new SizeCommand());
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aCommands registered");

        // Register containers for player data
        containerManager = new ContainerManager();
        containerManager.register("players", new PlayerContainer());
        containerManager.init(this);
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aContainers registered");

        // Register events for player and fishing actions
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerHandler(), this);
        pm.registerEvents(new AdvancementListener(), this);
        pm.registerEvents(new FishListener(), this);
        pm.registerEvents(new MobListener(),this);
        pm.registerEvents(new PlayerListener(), this);
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aEvents registered");

        fishPlaceholder = new PlaceholdersFish();
        advPlaceholder = new PlaceholdersAdv();
        mobPlaceholder = new PlaceholdersMob();
        purgePlaceholder = new PlaceholdersPurge();
        // Register placeholders for PlaceholderAPI
        if (pm.isPluginEnabled("PlaceholderAPI")) {
            if (!fishPlaceholder.isRegistered()) {
                fishPlaceholder.register();
            }
            if (!advPlaceholder.isRegistered()) {
                advPlaceholder.register();
            }
            if (!mobPlaceholder.isRegistered()) {
                mobPlaceholder.register();
            }
            if (!purgePlaceholder.isRegistered()) {
                purgePlaceholder.register();
            }
            Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aPlaceholders registered");
        }
        else {
            Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cPlaceholderAPI not found, placeholders not registered");
        }

        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aCatsTracker enabled");
    }

//    Adding a new thing:
//    Add placeholder to config
//    Add switch case to getPoints/setPoints in PointsManager

//    Register placeholder & event
//    pm.registerEvents(new BLANKListener(),this);
//    placeholderBLANK = new PlaceholderBLANK();
//    placeholderBLANK.register();

    @Override
    public void onDisable() {
        cmdManager.clearCommands();
        advPlaceholder.unregister();
        fishPlaceholder.unregister();
        mobPlaceholder.unregister();
        purgePlaceholder.unregister();
        this.pointLogger.writeEntries();

        // set fileLoggerRunning to false, so it can be restarted
        PointLogger.fileLoggerRunning = false;
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cCatsTracker disabled");
    }

    public ContainerManager getContainerManager() {return containerManager;}

    private void executeSettings() {
        if (!getConfig().getBoolean("settingsRan")) { // if settings_run == false
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule announceAdvancements false");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function blazeandcave:config/msg_set_off");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function blazeandcave:config/msg_super_challenge_on");
            Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aSettings executed");
            getConfig().set("settingsRan", true);
            saveConfig();
        }
    }

    public void reload() {
        reloadConfig();
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aCatsTracker reloaded");
    }

    private void checkAndResetConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();  // Create plugin's folder if it doesn't exist
        }

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            // If config doesn't exist, reset it
            saveDefaultConfig();
            getConfig().options().copyDefaults();
            saveConfig();
            Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aConfig file not found, created with defaults.");
        } else {
            // Check if config contains only the "settingsRan" flag
            if (getConfig().getKeys(false).size() == 1 && getConfig().contains("settingsRan")) {
                // Reset config if only "settingsRan" exists
                saveDefaultConfig();
                getConfig().options().copyDefaults();
                saveConfig();
//                empty config found, reseting
                Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cEmpty Config file detected. §aReset to defaults.");

            }
        }
    }
}
