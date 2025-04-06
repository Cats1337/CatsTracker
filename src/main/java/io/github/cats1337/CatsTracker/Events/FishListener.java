package io.github.cats1337.CatsTracker.Events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

//    Give points depending on the fish caught
//    Cod, Salmon, Pufferfish, Tropical Fish
//    Treasure, Junk, and Fish
//    Cod, Salmon - 1 point
//    Junk - 2 points
//    Pufferfish, Tropical Fish - 3 points
//    Treasure - 5 points
public class FishListener implements Listener {
    private static final Set<Material> JUNK_MATERIALS = new HashSet<>();

    // Add all junk materials
    static {
        JUNK_MATERIALS.add(Material.LILY_PAD);
        JUNK_MATERIALS.add(Material.BAMBOO);
        JUNK_MATERIALS.add(Material.BONE);
        JUNK_MATERIALS.add(Material.BOWL);
        JUNK_MATERIALS.add(Material.LEATHER);
        JUNK_MATERIALS.add(Material.LEATHER_BOOTS);
        JUNK_MATERIALS.add(Material.ROTTEN_FLESH);
        JUNK_MATERIALS.add(Material.GLASS_BOTTLE);
        JUNK_MATERIALS.add(Material.TRIPWIRE_HOOK);
        JUNK_MATERIALS.add(Material.STICK);
        JUNK_MATERIALS.add(Material.STRING);
        JUNK_MATERIALS.add(Material.INK_SAC);
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.fish");
        if (!trackPoints) return;

        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Entity caughtEntity = e.getCaught();
        if (!(caughtEntity instanceof Item caughtItem)) return;

        ItemStack itemStack = caughtItem.getItemStack();
        Material type = itemStack.getType();
        int points = getPoints(type, itemStack);

        if (points > 0) {
            Player p = e.getPlayer();
            PointsManager.getInstance().addPoints(p, points, type.name(), "fish");
            Text.of("You caught something! &7[&a+" + points + "&7]").send(p);
        }
    }


    private static int getPoints(Material type, ItemStack itemStack) {
        int points = switch (type) {
            case COD, SALMON -> 1;
            case PUFFERFISH, TROPICAL_FISH -> 3;
            case BOW, ENCHANTED_BOOK, NAME_TAG, NAUTILUS_SHELL, SADDLE -> 5;
            default -> JUNK_MATERIALS.contains(type) ? 2 : 0;
        };

        // Check if it's a fishing rod and has enchantments
        if (type == Material.FISHING_ROD) {
            points = itemStack.getEnchantments().isEmpty() ? 2 : 5;
        }

        return points;
    }
}


// https://jd.papermc.io/paper/1.21.4/org/bukkit/event/player/PlayerFishEvent.html#getCaught()
// https://minecraft.wiki/w/Fishing