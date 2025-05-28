package io.github.cats1337.CatsTracker;

import io.github.cats1337.CatsTracker.Events.*;
import io.github.cats1337.CatsTracker.commands.PointsCommand;
import io.github.cats1337.CatsTracker.commands.SizeCommand;
import io.github.cats1337.CatsTracker.commands.TimeWarpCommand;
import io.github.cats1337.CatsTracker.commands.UtilCommands;
import io.github.cats1337.CatsTracker.playerdata.PlayerContainer;
import io.github.cats1337.CatsTracker.utils.*;
import io.github.cats1337.CatsTracker.playerdata.PlayerHandler;
import com.marcusslover.plus.lib.command.CommandManager;
import com.marcusslover.plus.lib.container.ContainerManager;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class CatsTracker extends JavaPlugin {
    public static Logger log;
    private CommandManager cmdManager;
    private ContainerManager containerManager;
    private Placeholders placeholders;
    private CoreProtectAPI coreProtectAPI;
    private PointLogger pointLogger;
    private static CatsTracker instance;

    @Override
    public void onEnable() {
        instance = this;
        checkAndResetConfig();

        executeSettings();

        log = getInstance().getLogger();

        this.pointLogger = PointLogger.getInstance(); // initialize pointLogger
        this.pointLogger.fileLogger(); // initialize fileLogger
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aPointLogger initialized");

        // Register commands
        cmdManager = new CommandManager(this);
        cmdManager.register(new PointsCommand());
        cmdManager.register(new UtilCommands());
        cmdManager.register(new SizeCommand());
        cmdManager.register(new TimeWarpCommand());
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aCommands registered");

        // Register containers for player data
        containerManager = new ContainerManager();
        containerManager.register("players", new PlayerContainer());
        containerManager.init(this);
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aContainers registered");

        // Register events
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerHandler(), this);
        pm.registerEvents(new AdvancementListener(), this);
        pm.registerEvents(new FishListener(), this);
        pm.registerEvents(new MobListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new BlockListener(), this); // Register the BlockListener
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aEvents registered");

        // Initialize and register placeholders
        placeholders = new Placeholders();
        registerPlaceholders(pm);
        initializeCoreProtect(pm);

        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aCatsTracker enabled");
    }

    public void registerPlaceholders(PluginManager pm) {
        if (placeholders == null || !placeholders.isRegistered()) {
            placeholders = new Placeholders();
        }
        if (pm.isPluginEnabled("PlaceholderAPI")) {
            if (placeholders.register()) {
                Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aRegistering Placeholders:");
                for (String key : placeholders.getValidCategories()) {
                    Bukkit.getConsoleSender().sendMessage(" §8- §7" + key);
                }
                Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aPlaceholders registered");
            } else {
                Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cFailed to register placeholders with PlaceholderAPI");
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cPlaceholderAPI not found, placeholders will not work");
        }
    }

    public boolean initializeCoreProtect(PluginManager pm) {
        if (pm.isPluginEnabled("CoreProtect")) {
            Plugin coreProtect = pm.getPlugin("CoreProtect");
            if (coreProtect instanceof CoreProtect) {
                CoreProtectAPI api = ((CoreProtect) coreProtect).getAPI();
                if (api.isEnabled() && api.APIVersion() >= 10) {
                    this.coreProtectAPI = api;
                    Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aConnected to CoreProtect API v" + api.APIVersion());
                    return true;
                } else {
                    Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cCoreProtect API not available or version too old");
                }
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cCoreProtect not found, block tracking will be limited");
        }
        return false;
    }


    @Override
    public void onDisable() {
        cmdManager.clearCommands();
        if (placeholders.isRegistered()) {
            placeholders.unregister();
        }
        this.pointLogger.writeEntries();

        // set fileLoggerRunning to false, so it can be restarted
        PointLogger.fileLoggerRunning = false;
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cCatsTracker disabled");
    }

    public ContainerManager getContainerManager() { return containerManager; }

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
        
        // Re-register placeholders if needed
        PluginManager pm = Bukkit.getPluginManager();
        if (placeholders == null || !placeholders.isRegistered()) {
            placeholders = new Placeholders();
            registerPlaceholders(pm);
        }
        
        // Initialize CoreProtect if needed
        if (coreProtectAPI == null || !coreProtectAPI.isEnabled()) {
            initializeCoreProtect(pm);
        }
        
        Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aCatsTracker reloaded");
    }

    private void checkAndResetConfig() {
        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdirs()) {  // Create plugin's folder if it doesn't exist
                Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §aPlugin folder created.");
            } else {
                Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cFailed to create plugin folder.");
            }
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
                Bukkit.getConsoleSender().sendMessage("[§bCatsTracker§r] §cEmpty Config file detected. §aReset to defaults."); // Config is empty, reset it
            }
        }
    }


    public CoreProtectAPI getCoreProtectAPI() { return coreProtectAPI; }

    public static CatsTracker getInstance() { return instance; }
}
