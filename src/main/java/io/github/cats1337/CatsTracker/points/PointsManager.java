package io.github.cats1337.CatsTracker.points;

import io.github.cats1337.CatsTracker.playerdata.PlayerContainer;
import io.github.cats1337.CatsTracker.playerdata.PlayerHandler;
import io.github.cats1337.CatsTracker.playerdata.ServerPlayer;
import io.github.cats1337.CatsTracker.utils.PointLogger;
import io.github.cats1337.CatsTracker.CatsTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public final class PointsManager {
    
    private static final PointLogger pointLogger = PointLogger.getInstance();

    // Private constructor to prevent instantiation
    private PointsManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static int getPoints(Player p, String category) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        return getPoints(serverPlayer, category);
    }

    private static int getPoints(ServerPlayer serverPlayer, String category) {
        try {
            if (serverPlayer == null) {
                CatsTracker.log.warning("ServerPlayer is null");
                return 0;
            }
            
            return serverPlayer.getPoints(category.toLowerCase());
        } catch (Exception e) {
            CatsTracker.log.warning("Error getting points for ServerPlayer in category " + category + ": " + e.getMessage());
            return 0;
        }
    }

    public static void addPoints(Player p, int amount, String typeName, String category) {
        modifyPoints(p, amount, category);

        String player = p.getName();
        String entry = player + ": +" + amount + "pts - [" + typeName + "]";
        pointLogger.addEntry(entry);
    }

    public static void removePoints(Player p, int amount, String typeName, String category) {
        modifyPoints(p, -amount, category);

        String player = p.getName();
        String entry = player + ": -" + amount + "pts - [" + typeName + "]";
        pointLogger.addEntry(entry);
    }

    public static void modifyPoints(Player p, int amount, String category) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        int points = getPoints(serverPlayer, category);
        int newAmount = (points + amount);
        setPoints(serverPlayer, category, newAmount);
        
        // Save the data after modifying
        savePlayerData(p.getUniqueId(), serverPlayer);
    }

    public static void setPoints(Player p, String category, int amount) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        setPoints(serverPlayer, category, amount);
        savePlayerData(p.getUniqueId(), serverPlayer);
    }

    private static void setPoints(ServerPlayer serverPlayer, String category, int points) {
        serverPlayer.setPoints(category.toLowerCase(), points);
    }

    public static List<Map.Entry<String, Integer>> getAllSortedScores(String category) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();

        Map<String, Integer> playerScores = new HashMap<>();

        for (ServerPlayer serverPlayer : playerContainer.getValues()) {
            String name = serverPlayer.getName();
            int score = getPoints(serverPlayer, category);
            playerScores.put(name, score);
        }

        List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(playerScores.entrySet());
        sortedScores.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        return sortedScores;
    }

    private static void savePlayerData(UUID uuid, ServerPlayer serverPlayer) {
        CatsTracker plugin = CatsTracker.getInstance();
        
        // Check if plugin is enabled
        if (plugin.isEnabled()) {
            // Plugin is enabled, save data asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, 
                () -> PlayerHandler.getInstance().getContainer().writeData(uuid, serverPlayer));
        } else {
            try {
                // Plugin is disabled, save data synchronously
                PlayerHandler.getInstance().getContainer().writeData(uuid, serverPlayer);
                
                // Log that we're handling a disabled plugin situation
                Bukkit.getLogger().warning("[CatsTracker] Plugin was disabled during data save operation. Data saved synchronously.");
            } catch (Exception e) {
                Bukkit.getLogger().severe("[CatsTracker] Error saving player data while plugin is disabled: " + e.getMessage());
            }
        }
    }
}