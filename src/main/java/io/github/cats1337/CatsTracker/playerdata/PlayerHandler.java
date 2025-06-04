package io.github.cats1337.CatsTracker.playerdata;
// Credit https://github.com/bumpyJake for the original code, which has been modified
import io.github.cats1337.CatsTracker.CatsTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;
import java.util.UUID;

public class PlayerHandler implements Listener {

    private static PlayerHandler instance;

    public PlayerHandler() {
        instance = this;
    }

    public PlayerContainer getContainer() {
        if (CatsTracker.getInstance() == null || CatsTracker.getInstance().getContainerManager() == null)
            return null;

        return CatsTracker.getInstance().getContainerManager()
                .getByType(PlayerContainer.class)
                .orElse(null);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        PlayerContainer container = getContainer();
        if (container == null) return;

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
        PlayerContainer container = getContainer();
        if (container == null) return;

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        ServerPlayer serverPlayer = getContainer().loadData(uuid);
        getContainer().writeData(uuid, serverPlayer);
    }

    public static PlayerHandler getInstance() {return Objects.requireNonNullElseGet(instance, PlayerHandler::new);}
}