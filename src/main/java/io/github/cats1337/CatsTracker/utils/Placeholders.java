package io.github.cats1337.CatsTracker.utils;

import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Placeholders extends PlaceholderExpansion {

    private final Set<String> validCategories;

    public Placeholders() {
        this.validCategories = new HashSet<>(CatsTracker.getInstance().getConfig().getStringList("placeholders"));
    }

    public Set<String> getValidCategories() {
        return validCategories;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Cats1337";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(final Player player, @NotNull String args) {
        args = args.replace("%cats_", "");

        String[] parts = args.split("_");
        String category = parts[0];

        if (parts[1].equals("top") && parts[2].equals("scores")) {
            if (parts.length != 4) {
                return "&cNo data";
            }
            int num;
            try {
                num = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                return "&cNo data";
            }
            if (!validCategories.contains(category)) {
                return "&cNo data";
            }

            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(PointsManager.getAllSortedScores(category));
            if (num > 0 && num <= sortedEntries.size()) {
                String playerName = sortedEntries.get(num - 1).getKey();
                int score = sortedEntries.get(num - 1).getValue();

                return "&e" + num + ". &a" + playerName + " &8- &6" + score;
            }
            return "&e" + num + ". &cNo data";
        } else {
            if (parts[1].equals("score")) {
                return String.valueOf(PointsManager.getPoints(player, category));
            }
        }
        return null;
    }
}

