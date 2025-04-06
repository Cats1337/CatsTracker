package io.github.cats1337.CatsTracker.utils;

import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaceholdersFish extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "fish";
    }

    @Override
    public @NotNull String getAuthor() {return "Cats1337";}

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    public @Nullable String onPlaceholderRequest(final Player player, @NotNull final String args) {
        if (args.startsWith("top_scores_")) {
            String[] parts = args.split("_");
            if (parts.length != 3 || !parts[0].equals("top") || !parts[1].equals("scores")) {
                return "&cNo data";
            }
            int num;
            try {
                num = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return "&cNo data";
            }

            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(PointsManager.getInstance().getAllSortedScores(getIdentifier()));
            if (num > 0 && num <= sortedEntries.size()) {
                String playerName = sortedEntries.get(num - 1).getKey();
                int score = sortedEntries.get(num - 1).getValue();

                return "&e" + num + ". &a" + playerName + " &8- &6" + score;
            }
            return "&e" + num + ". &cNo data";
        } else {
            if (args.equals("score")) {
                return String.valueOf(PointsManager.getInstance().getPoints(player, getIdentifier()));
            }
        }
        return null;
    }
}

