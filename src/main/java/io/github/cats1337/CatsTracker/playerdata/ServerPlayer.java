package io.github.cats1337.CatsTracker.playerdata;
// Credit https://github.com/bumpyJake
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class ServerPlayer {
    private final UUID uuid;

    @Setter(AccessLevel.PUBLIC)
    private String name;
    
    // Single map to store all point categories
    private Map<String, Integer> points = new HashMap<>();
    
    // For backward compatibility - these will be migrated to the points map
    private int advPoints;
    private int fishPoints;
    private int mobPoints;
    private int purgePoints;
    private int breakPoints;
    private int placePoints;

    public ServerPlayer(UUID uuid) {
        this.uuid = uuid;
        // Initialize the points map
        if (this.points == null) {
            this.points = new HashMap<>();
        }
        
        // Migrate legacy fields to the map if they have values
        migrateOldFields();
    }
    
    /**
     * Migrate old field values to the points map
     */
    private void migrateOldFields() {
        if (advPoints > 0) points.put("adv", advPoints);
        if (fishPoints > 0) points.put("fish", fishPoints);
        if (mobPoints > 0) points.put("mob", mobPoints);
        if (purgePoints > 0) points.put("purge", purgePoints);
        if (breakPoints > 0) points.put("break", breakPoints);
        if (placePoints > 0) points.put("place", placePoints);
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Get points for a specific category
     * @param category The category to get points for
     * @return The number of points
     */
    public int getPoints(String category) {
        // Ensure the map is initialized
        if (points == null) {
            points = new HashMap<>();
            migrateOldFields();
        }
        
        // For backward compatibility, check old fields if the map doesn't have the value
        if (!points.containsKey(category.toLowerCase())) {
            switch (category.toLowerCase()) {
                case "adv" -> {
                    if (advPoints > 0) {
                        points.put("adv", advPoints);
                        return advPoints;
                    }
                }
                case "fish" -> {
                    if (fishPoints > 0) {
                        points.put("fish", fishPoints);
                        return fishPoints;
                    }
                }
                case "mob" -> {
                    if (mobPoints > 0) {
                        points.put("mob", mobPoints);
                        return mobPoints;
                    }
                }
                case "purge" -> {
                    if (purgePoints > 0) {
                        points.put("purge", purgePoints);
                        return purgePoints;
                    }
                }
                case "break" -> {
                    if (breakPoints > 0) {
                        points.put("break", breakPoints);
                        return breakPoints;
                    }
                }
                case "place" -> {
                    if (placePoints > 0) {
                        points.put("place", placePoints);
                        return placePoints;
                    }
                }
            }
        }
        
        return points.getOrDefault(category.toLowerCase(), 0);
    }

    /**
     * Set points for a specific category
     * @param category The category to set points for
     * @param value The number of points
     * @return This ServerPlayer instance for chaining
     */
    public ServerPlayer setPoints(String category, int value) {
        // Ensure the map is initialized
        if (points == null) {
            points = new HashMap<>();
        }
        
        // Store in the map
        points.put(category.toLowerCase(), value);
        
        // For backward compatibility, also update the old fieldss
        switch (category.toLowerCase()) {
            case "adv" -> advPoints = value;
            case "fish" -> fishPoints = value;
            case "mob" -> mobPoints = value;
            case "purge" -> purgePoints = value;
            case "break" -> breakPoints = value;
            case "place" -> placePoints = value;
        }
        
        return this;
    }

    /**
     * Add points to a specific category
     * @param category The category to add points to
     * @param amount The amount of points to add
     */
    public void addPoints(String category, int amount) {
        setPoints(category, getPoints(category) + amount);
    }
}
