package io.github.cats1337.CatsTracker.utils;

import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class Placeholders extends PlaceholderExpansion {

    private final Set<String> validCategories;

    public Placeholders() {
        this.validCategories = new HashSet<>(CatsTracker.getInstance().getConfig().getStringList("placeholders"));
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
        return "1.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(final Player player, @NotNull String identifier) {

        if (player == null) {
            return "";
        }

        try {
            // Handle the case where identifier is just a category name
            if (validCategories.contains(identifier)) {
                int points = PointsManager.getPoints(player, identifier);
                return String.valueOf(points);
            }
            
            // Split the identifier into parts
            String[] parts = identifier.split("_");
            
            // Handle category_score format (e.g., adv_score)
            if (parts.length == 2 && parts[1].equals("score")) {
                String category = parts[0];
                int points = PointsManager.getPoints(player, category);
                return String.valueOf(points);
            }
            
            // Handle category_top_scores_N format (e.g., adv_top_scores_1)
            if (parts.length >= 4 && parts[1].equals("top") && parts[2].equals("scores")) {
                String category = parts[0];
                int position;
                try {
                    position = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    return "&cInvalid position number";
                }
                
                List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(PointsManager.getAllSortedScores(category));
                if (position > 0 && position <= sortedEntries.size()) {
                    String playerName = sortedEntries.get(position - 1).getKey();
                    int score = sortedEntries.get(position - 1).getValue();
                    return "&e" + position + ". &a" + playerName + " &8- &6" + score;
                }
                return "&e" + position + ". &cNo data";
            }
            
            return null;
            
        } catch (Exception e) {
            return "&cError";
        }
    }
}

