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

    private final PointLogger pointLogger;
    private PointsManager() {this.pointLogger = PointLogger.getInstance();}


    public void addPoints(Player p, int amount, String typeName, String category) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        int points = getPoints(serverPlayer, category);
        int newAmount = points + amount;
        setPoints(serverPlayer, category, newAmount);
        Bukkit.getScheduler().runTaskAsynchronously(CatsTracker.getInstance(), () -> {
            playerContainer.writeData(p.getUniqueId(), serverPlayer);
        });

        String player = p.getName();
        String entry = player + ": +" + amount + "pts - [" + typeName + "]";
        pointLogger.addEntry(entry);
    }

    public int getPoints(Player p, String category) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        return getPoints(serverPlayer, category);
    }

    private int getPoints(ServerPlayer serverPlayer, String category) {
        return switch (category.toLowerCase()) {
            case "adv" -> serverPlayer.getAdvPoints();
            case "fish" -> serverPlayer.getFishPoints();
            case "mob" -> serverPlayer.getMobPoints();
            case "purge" -> serverPlayer.getPurgePoints();
            default -> throw new IllegalArgumentException("Unknown category: " + category);
        };
    }

    public void setPoints(Player p, String category, int amount) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        setPoints(serverPlayer, category, amount);
        Bukkit.getScheduler().runTaskAsynchronously(CatsTracker.getInstance(), () -> {
            playerContainer.writeData(p.getUniqueId(), serverPlayer);
        });
    }

    private void setPoints(ServerPlayer serverPlayer, String category, int points) {
        switch (category.toLowerCase()) {
            case "adv":
                serverPlayer.setAdvPoints(points);
                break;
            case "fish":
                serverPlayer.setFishPoints(points);
                break;
            case "mob":
                serverPlayer.setMobPoints(points);
                break;
            case "purge":
                serverPlayer.setPurgePoints(points);
                break;
            default:
                serverPlayer.setAdvPoints(points);
                break;
        }
    }

    public List<Map.Entry<String, Integer>> getAllSortedScores(String category) {
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