package io.github.cats1337.CatsTracker.Events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.commands.SizeCommand;
import io.github.cats1337.CatsTracker.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerListener implements Listener {
    private final Map<UUID, Map<UUID, Long>> killCooldowns = new HashMap<>();
    private final Map<UUID, LinkedList<UUID>> killHistory = new HashMap<>();
    private final Map<UUID, Long> respawnTimers = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        boolean trackPoints = CatsTracker.getInstance().getConfig().getBoolean("trackPoints.purge");
        e.getEntity();
        if (!trackPoints) return;

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
            PointsManager.addPoints(killer, 3, "Kill - " + victim.getName(), "purge");
            Text.of("&eYou killed &c" + victim.getName() + "&e! &7[&a+3&7]").send(killer);

            String command = "crate key give " + killer.getName() + " purge";
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, command);

            applyCooldown(killerId, victimId, cooldownDuration);
            recordKill(killerId, victimId);
            PointsManager.addPoints(victim, -5, "Death - " + killer.getName(), "purge");
            Text.of("&eYou were killed by &c" + killer.getName() + "&e! &7[&c-5&7]").send(victim);
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

    // Combat Tag tracking
    // when player takes damage, check if it's from another player, if so, check their size, and if it's not 1.0, force them to 1.0 via ./makeme normal
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(e.getDamager() instanceof Player attacker)) return;

        if (victim.getUniqueId().equals(attacker.getUniqueId())) return; // Prevent self-damage

        if (CatsTracker.getInstance().getConfig().getBoolean("sizeInCombat")) {
            if (SizeCommand.getScale(victim) != 1.0f) {
                SizeCommand.setScale(victim, 1.0f);
                Text.of("&cYou have been forced to normal size due to combat!").send(victim);
            }
        }


    }


    // Kill History Logic
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

    final long MESSAGE_COOLDOWN = 10000; // 10 seconds in milliseconds
    final HashMap<UUID, Long> lastMessageTime = new HashMap<>();
    public static final boolean EnabledStatus = CatsTracker.getInstance().getConfig().getBoolean("notify");

    @EventHandler
    public void EntityPotionEffectEvent(EntityPotionEffectEvent e) {
        if(!EnabledStatus) {
            return;
        }

        if (!(e.getEntity() instanceof Player p)) return;
        PotionEffect newEffect = e.getNewEffect();
        if (newEffect != null) {
            if (e.getNewEffect().getType().equals(PotionEffectType.STRENGTH) && e.getNewEffect().getAmplifier() >=1) {
                e.setCancelled(true);
                int duration = e.getNewEffect().getDuration();
                p.removePotionEffect(PotionEffectType.STRENGTH);
                PotionEffect strength1 = new PotionEffect(PotionEffectType.STRENGTH, duration, 0, false, true, true);
                p.addPotionEffect(strength1);
                if (!lastMessageTime.containsKey(p.getUniqueId()) || System.currentTimeMillis() - lastMessageTime.get(p.getUniqueId()) >= MESSAGE_COOLDOWN) {
                    if (e.getCause() == EntityPotionEffectEvent.Cause.BEACON) {return;}
                    Text.of("&cYou can't have &6Strength &e>1&c! &7[Converted]").send(p);
                    lastMessageTime.put(p.getUniqueId(), System.currentTimeMillis());
                }
            }
            if (e.getNewEffect().getType().equals(PotionEffectType.RESISTANCE) && e.getNewEffect().getAmplifier() >=1) {
                e.setCancelled(true);
                int duration = e.getNewEffect().getDuration();
                p.removePotionEffect(PotionEffectType.RESISTANCE);
                PotionEffect resistance1 = new PotionEffect(PotionEffectType.RESISTANCE, duration, 0, false, true, true);
                p.addPotionEffect(resistance1);
                if (!lastMessageTime.containsKey(p.getUniqueId()) || System.currentTimeMillis() - lastMessageTime.get(p.getUniqueId()) >= MESSAGE_COOLDOWN) {
                    if (e.getCause() == EntityPotionEffectEvent.Cause.BEACON) {return;}
                    Text.of("&cYou can't have &6Resistance &e>1&c! &7[Converted]").send(p);
                    lastMessageTime.put(p.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

}
