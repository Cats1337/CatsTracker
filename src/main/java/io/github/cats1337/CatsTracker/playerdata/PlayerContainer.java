package io.github.cats1337.CatsTracker.playerdata;

import com.marcusslover.plus.lib.container.extra.InitialLoading;
import com.marcusslover.plus.lib.container.type.MapContainer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@InitialLoading
public class PlayerContainer extends MapContainer<UUID, ServerPlayer> {

    public PlayerContainer() {
        super(UUID::toString, UUID::fromString, ServerPlayer.class);
    }

    @Override
    protected @NotNull ServerPlayer emptyValue(@NotNull UUID key) {
        return new ServerPlayer(key);
    }

}