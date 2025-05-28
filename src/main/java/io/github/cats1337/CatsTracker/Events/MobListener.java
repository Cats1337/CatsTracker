package io.github.cats1337.CatsTracker.Events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobListener implements Listener {
//    When player kills a mob (entity)

    // Silverfish, Slime - None/Ignored
    // Spider, Cave Spider, Zombie, Skeleton, Magma Cube, Pillager, Vex - 1 Point
    // Enderman, Endermite, Evoker, Vindicator - 2 Points
    // Creeper, Drown, Husk, Bogged, Stray, Blaze, Witch, Breeze (bypass spawner limitation), Wither Skeleton, Evoker - 3 Points
    // Ghast, Guardian, Piglin Brute, Ravager, Wither, Ender Dragon, Elder Guardian - 5 Points

    @EventHandler
    public void onEntityKill(EntityDeathEvent e) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.mob");
        if (!trackPoints) return;

        Player p = e.getEntity().getKiller();
        if (!(e.getEntity() instanceof Mob mob) || e.getEntity() instanceof Player || p == null || mob.getScoreboardTags().contains("spawner")) {
            return;
        }

        // Get mob type
        EntityType mobType = mob.getType();
        int points = getPoints(mobType);

        // Give the player the points
        if (points > 0) {
//           convert ENDER_DRAGON -> Ender Dragon
            String name = mobType.getName();
//            instead of using getName() use getKey().getKey() to get the name

            assert name != null;
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
//            name = name.replaceAll("&[0-9a-fk-or]", "").replaceAll("[\\[\\]]", "");
            PointsManager.addPoints(p, points, name, "mob");
            Text.of("You killed something! &7[&a+" + points + "&7]").send(p);
        }
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Mob) {
            if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                e.getEntity().addScoreboardTag("spawner");
            }
        }
    }

    private int getPoints(EntityType type) {
        int points = switch(type) {
            case SPIDER, CAVE_SPIDER, ZOMBIE, SKELETON, MAGMA_CUBE, PILLAGER, VEX -> 1;
            case ENDERMAN, ENDERMITE, EVOKER, VINDICATOR -> 2;
            case CREEPER, DROWNED, HUSK, BOGGED, STRAY, BLAZE, WITCH, BREEZE,
                    WITHER_SKELETON -> 3;
            case GHAST, GUARDIAN, PIGLIN_BRUTE, RAVAGER,
                    WITHER, ENDER_DRAGON, ELDER_GUARDIAN -> 5;
            default -> 0;
        };
        return points;
    }

}
