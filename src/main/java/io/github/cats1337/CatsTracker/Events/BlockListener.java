package io.github.cats1337.CatsTracker.Events;

import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class BlockListener implements Listener {
    public BlockListener() { addIgnoreBlock(); }
    
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
    public void onBlockPlace(BlockPlaceEvent event) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.place");
        if (!trackPoints) return;
        Player player = event.getPlayer();
        
        // Award 1 point for block placement (category: "block")
        PointsManager.addPoints(player, 1, "block_place", "block");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.break");
        if (!trackPoints) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Ignore flora, crops, and leaves
        if (IGNORED_BREAK_TYPES.contains(block.getType())) {
            return;
        }
        
        // Check if the block was placed by a player using CoreProtect
        CoreProtectAPI coreProtect = getCoreProtect();
        if (coreProtect != null) {
            // Look up data from the last 3 days
            List<String[]> blockData = coreProtect.blockLookup(block, 259200);
            
            if (blockData != null) {
                for (String[] data : blockData) {
                    // Check if this block was placed (action #1 is placement)
                    if (data[4].equals("1")) {
                        // Block was placed by a player, don't award points
                        return;
                    }
                }
            }
        }

        // if block is an ore, add 2 points instead of 1
        if (block.getType().name().contains("ORE")) {
            PointsManager.addPoints(player, 2, "block_break", "block");
            return;
        }

        // Award 1 point for breaking valid block (category: "block")
        PointsManager.addPoints(player, 1, "block_break", "block");
    }

}