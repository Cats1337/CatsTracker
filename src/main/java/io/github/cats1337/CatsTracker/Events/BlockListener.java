package io.github.cats1337.CatsTracker.Events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class BlockListener implements Listener {
    public BlockListener() {
        addIgnoreBlock();
        startPointsAlertTask();
    }

    // Cache for block lookups to avoid excessive API calls
    private final Map<Location, Boolean> lookupCache = new HashMap<>();
    private final Map<Location, Long> cacheTimestamps = new HashMap<>();

    // Recent point timestamps for players
    private final Map<UUID, Map<String, List<Long>>> recentPointTimestamps = new HashMap<>();


    // Set of flora/crops/leaves to ignore for breaking
    private static final Set<Material> IGNORED_BREAK_TYPES = EnumSet.of(
            Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
            Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
            Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY,
            Material.SUNFLOWER, Material.ROSE_BUSH, Material.PEONY, Material.LILAC,
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
            Material.SUGAR_CANE, Material.MELON, Material.PUMPKIN, Material.CACTUS,
            Material.BAMBOO, Material.SWEET_BERRY_BUSH, Material.GLOW_BERRIES,
            Material.OAK_LEAVES, Material.BIRCH_LEAVES, Material.SPRUCE_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES
    );

    /**
     * Adds blocks from the config's ignoredBlocks list to the IGNORED_BREAK_TYPES set
     */
    private void addIgnoreBlock() {
        // Get the list of ignored blocks from config
        List<String> ignoredBlocksList = CatsTracker.getInstance().getConfig().getStringList("ignoredBlocks");
        
        // If the list is empty, return
        if (ignoredBlocksList.isEmpty()) {
            return;
        }
        
        // Add each valid material to the IGNORED_BREAK_TYPES set
        for (String ignoredBlock : ignoredBlocksList) {
            try {
                // Try to convert the string to a Material enum
                Material material = Material.valueOf(ignoredBlock.toUpperCase());
                IGNORED_BREAK_TYPES.add(material);
            } catch (IllegalArgumentException e) {
                // If the string isn't a valid Material, log a warning
                CatsTracker.log.warning("Invalid material in ignoredBlocks config: " + ignoredBlock);
            }
        }
    }
    
    private CoreProtectAPI getCoreProtect() {
        CoreProtectAPI api = CatsTracker.getInstance().getCoreProtectAPI();
        if (api != null && api.isEnabled()) {
            return api;
        }
        
        // Fallback to direct lookup if the API isn't available from the main class
        Plugin plugin = CatsTracker.getInstance().getServer().getPluginManager().getPlugin("CoreProtect");
        
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }
        
        CoreProtectAPI coreProtect = ((CoreProtect) plugin).getAPI();
        
        if (!coreProtect.isEnabled()) {
            return null;
        }
        
        if (coreProtect.APIVersion() < 6) {
            CatsTracker.log.warning("CoreProtect API version is too old! Need version 6 or higher.");
            return null;
        }
        
        return coreProtect;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.place");
        if (!trackPoints) return;
        Player p = e.getPlayer();
        Block block = e.getBlock();

        if (IGNORED_BREAK_TYPES.contains(block.getType())) return;

        PointsManager.addPoints(p, 1, "block_place", "place");
        recordReward(p, "placing");
    }

    // Utility to check if block was placed by player (with caching)
    private boolean isUnnaturalBlock(CoreProtectAPI api, Block block) {
        Location loc = block.getLocation();

        // Cached
        if (lookupCache.containsKey(loc)) {
            long last = cacheTimestamps.getOrDefault(loc, 0L);
            // 1 min
            long cacheExpiryMillis = 60_000;
            if (System.currentTimeMillis() - last < cacheExpiryMillis) {
                return lookupCache.get(loc);
            }
        }

        boolean isUnnatural = false;
        List<String[]> lookup = api.blockLookup(block, 259200); // 3 days

        if (lookup == null || lookup.isEmpty()) {
            isUnnatural = true;
        }


        if (lookup != null) {
            for (String[] entry : lookup) {
                if (entry.length >= 6 && entry[5].equals("1")) {
                    isUnnatural = true;
                    break;
                }
            }
        }

        lookupCache.put(loc, isUnnatural);
        cacheTimestamps.put(loc, System.currentTimeMillis());
        return isUnnatural;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.break");
        if (!trackPoints) return;

        Player p = e.getPlayer();
        Block block = e.getBlock();

        if (IGNORED_BREAK_TYPES.contains(block.getType())) return;

        CoreProtectAPI api = getCoreProtect();
        if (api == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(CatsTracker.getInstance(), () -> {
            if (!isUnnaturalBlock(api, block)) return;

            int points = 1;

            Bukkit.getScheduler().runTask(CatsTracker.getInstance(), () -> {
                PointsManager.addPoints(p, points, "block_break", "break");
                recordReward(p, "breaking");
            });
        });
    }


    public void recordReward(Player p, String category) {
        UUID uuid = p.getUniqueId();
        recentPointTimestamps
                .computeIfAbsent(uuid, k -> new HashMap<>())
                .computeIfAbsent(category, k -> new ArrayList<>())
                .add(System.currentTimeMillis());
    }

    public void onPointsAlert(Player p) {
        long cutoff = System.currentTimeMillis() - (5 * 60 * 1000);
        UUID uuid = p.getUniqueId();
        Map<String, List<Long>> categoryMap = recentPointTimestamps.getOrDefault(uuid, Collections.emptyMap());

        for (Map.Entry<String, List<Long>> entry : categoryMap.entrySet()) {
            String category = entry.getKey();
            List<Long> timestamps = entry.getValue();

            long count = timestamps.stream().filter(t -> t >= cutoff).count();

            if (count > 0) {
                int points = (int) count; // Assuming 1 point per event here, adjust if needed

                // Send message on main thread
                Bukkit.getScheduler().runTask(CatsTracker.getInstance(), () -> {
                    Text.of("&fYou've received [&a" + points + "&f] points for " + category + " blocks in the last 5 minutes.").send(p);
                });
            }
        }
    }


    private void startPointsAlertTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(CatsTracker.getInstance(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                onPointsAlert(p);
            }
        }, 0L, 20L * 60 * 5); // every 5 minutes async
    }

}