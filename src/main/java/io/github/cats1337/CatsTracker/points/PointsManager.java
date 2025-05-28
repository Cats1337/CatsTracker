package io.github.cats1337.CatsTracker.points;

import io.github.cats1337.CatsTracker.playerdata.PlayerContainer;
import io.github.cats1337.CatsTracker.playerdata.PlayerHandler;
import io.github.cats1337.CatsTracker.playerdata.ServerPlayer;
import io.github.cats1337.CatsTracker.utils.PointLogger;
import io.github.cats1337.CatsTracker.CatsTracker;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PointsManager {
    @Getter
    private static final PointsManager instance = new PointsManager();

    private static PointLogger pointLogger = new PointLogger();
    private PointsManager() {pointLogger = PointLogger.getInstance();}

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
        Bukkit.getScheduler().runTaskAsynchronously(CatsTracker.getInstance(), () -> playerContainer.writeData(p.getUniqueId(), serverPlayer));
    }

    public static void setPoints(Player p, String category, int amount) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        setPoints(serverPlayer, category, amount);
        Bukkit.getScheduler().runTaskAsynchronously(CatsTracker.getInstance(), () -> playerContainer.writeData(p.getUniqueId(), serverPlayer));
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

}