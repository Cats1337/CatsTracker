package io.github.cats1337.CatsTracker.playerdata;
// Credit https://github.com/bumpyJake
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class ServerPlayer {
    private final UUID uuid;

    @Setter(AccessLevel.PUBLIC)
    private String name;
    private int advPoints;
    private int fishPoints;
    private int mobPoints;
    private int purgePoints;

    public @Nullable Player getPlayer() { // Unused? Delete?
        return Bukkit.getPlayer(uuid);
    }

}
