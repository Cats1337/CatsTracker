package io.github.cats1337.CatsTracker.playerdata;
// Credit https://github.com/bumpyJake
import io.github.cats1337.CatsTracker.CatsTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class PlayerHandler implements Listener {

    private static PlayerHandler instance;

    public PlayerHandler() {
        instance = this;
    }

    public static PlayerHandler getInstance() {
        return Objects.requireNonNullElseGet(instance, PlayerHandler::new);
    }

    public @NotNull Collection<ServerPlayer> getGamePlayers() {
        return getContainer().getValues();
    }

    public PlayerContainer getContainer() {
        if (!CatsTracker.getInstance().getContainerManager().getByType(PlayerContainer.class).isPresent()){
            return null;
        }
        return CatsTracker.getInstance().getContainerManager().getByType(PlayerContainer.class).get();
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        ServerPlayer serverPlayer = getContainer().loadData(uuid);
        if (serverPlayer.getName() == null) {
            serverPlayer.setName(p.getName());
            getContainer().writeData(uuid, serverPlayer);
        } else {
            serverPlayer.setName(p.getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        ServerPlayer serverPlayer = getContainer().loadData(uuid);
        getContainer().writeData(uuid, serverPlayer);
    }
}