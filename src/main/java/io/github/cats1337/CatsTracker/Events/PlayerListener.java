package io.github.cats1337.CatsTracker.Events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.LocalDateTime;
import java.util.*;

public class PlayerListener implements Listener {

    private final Map<UUID, Map<UUID, Long>> killCooldowns = new HashMap<>();
    private final Map<UUID, LinkedList<UUID>> killHistory = new HashMap<>();
    private final Map<UUID, Long> respawnTimers = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.purge");
        if (!(e.getEntity() instanceof Player) || !trackPoints) return;

        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) return;

        UUID killerId = killer.getUniqueId();
        UUID victimId = victim.getUniqueId();

        long cooldownDuration = CatsTracker.getInstance().getConfig().getInt("purgeCooldown") * 60000L;
        long respawnDuration = CatsTracker.getInstance().getConfig().getLong("respawnCooldown") * 60000L;

        if (isInCooldown(killerId, victimId) || isInRespawnCooldown(victimId)) {
            long timeLeft = getCooldownRemaining(killerId, victimId);
            long mins = timeLeft / 60000;
            long secs = (timeLeft % 60000) / 1000;
            Text.of("&cYou must wait &b" + mins + ":" + secs + " &cbefore earning from &e" + victim.getName() + "&c.").send(killer);
        if (!hasKilledTwoOthersSince(killerId, victimId)){
            Text.of("&cYou must kill &b" + getKillAmount(killerId, victimId) + "&c others before earning from &e" + victim.getName() + "&c.").send(killer);
        }
        } else {
            PointsManager.getInstance().addPoints(killer, 3, "Player Kill", "purge");
            Text.of("&eYou killed " + victim.getName() + "! &7[&a+3&7]").send(killer);

            String command = "crate key give " + killer.getName() + " purge";
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, command);
            Text.of("&eYou received a &6&ncrate key&e for killing &b" + victim.getName()+ "&e.").send(killer);

            applyCooldown(killerId, victimId, cooldownDuration);
            recordKill(killerId, victimId);
            logKill(killer, victim);
            PointsManager.getInstance().addPoints(victim, -5, "Player Death", "purge");
            Text.of("&eYou were killed by " + killer.getName() + "! &7[&c-5&7]").send(victim);
        }
        applyRespawnCooldown(victimId, respawnDuration);
    }

    // Cooldown Logic

    private void applyCooldown(UUID killerId, UUID victimId, long durationMillis) {
        killCooldowns
                .computeIfAbsent(killerId, k -> new HashMap<>())
                .put(victimId, System.currentTimeMillis() + durationMillis);
    }

    private boolean isInCooldown(UUID killerId, UUID victimId) {
        return getCooldownRemaining(killerId, victimId) > 0;
    }

    private long getCooldownRemaining(UUID killerId, UUID victimId) {
        long expiry = killCooldowns
                .getOrDefault(killerId, Collections.emptyMap())
                .getOrDefault(victimId, 0L);
        return expiry - System.currentTimeMillis();
    }

    // Kill History (optional for rule enforcement, not used right now)

    private void recordKill(UUID killerId, UUID victimId) {
        LinkedList<UUID> history = killHistory
                .computeIfAbsent(killerId, k -> new LinkedList<>());
        history.addFirst(victimId);
        if (history.size() > 10) history.removeLast();
    }

    private boolean hasKilledTwoOthersSince(UUID killerId, UUID victimId) {
        LinkedList<UUID> history = killHistory.getOrDefault(killerId, new LinkedList<>());
        int count = 0;
        Set<UUID> seen = new HashSet<>();
        for (UUID id : history) {
            if (!id.equals(victimId) && seen.add(id)) count++;
            if (count >= 2) return true;
        }
        return false;
    }

    private int getKillAmount(UUID killerId, UUID victimId) {
        LinkedList<UUID> history = killHistory.getOrDefault(killerId, new LinkedList<>());
        int count = 3;
        for (UUID id : history) {
            if (id.equals(victimId)) count--;
        }
        return count;
    }

    // Respawn Cooldown

    private void applyRespawnCooldown(UUID playerId, long durationMillis) {
        respawnTimers.put(playerId, System.currentTimeMillis() + durationMillis);
    }

    private boolean isInRespawnCooldown(UUID playerId) {
        return respawnTimers.getOrDefault(playerId, 0L) > System.currentTimeMillis();
    }

    // Logging

    private void logKill(Player killer, Player victim) {
        String log = "[" + LocalDateTime.now() + "] " + killer.getName() + " killed " + victim.getName();
        CatsTracker.getInstance().getLogger().info(log);
    }
}
