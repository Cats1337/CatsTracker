package io.github.cats1337.CatsTracker.Events;

import io.github.cats1337.CatsTracker.points.PointsManager;
import io.github.cats1337.CatsTracker.CatsTracker;
import com.marcusslover.plus.lib.text.Text;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListener implements Listener {

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent e) {
        FileConfiguration config = CatsTracker.getInstance().getConfig();
        boolean trackPoints = config.getBoolean("trackPoints.adv");
        if (!trackPoints) return;

        Advancement advancement = e.getAdvancement();
        Player p = e.getPlayer();

        if (advancement.getDisplay() == null) return;
        AdvancementDisplay display = advancement.getDisplay();
        AdvancementDisplay.Frame frame = display.frame();
        Advancement fullTab = advancement.getRoot();

        String tabRoot = fullTab.getKey().toString();
        String tab = tabRoot.substring(0, tabRoot.indexOf(":"));

        for(String excludedTab : config.getStringList("excludedTabs")) {
            if(excludedTab.toUpperCase().contains(tab.toUpperCase())) return;
        }

        switch (frame) {
            case CHALLENGE -> {
                int amount = config.getInt("challenge");

                String achName = PlainTextComponentSerializer.plainText().serialize(display.displayName());
                achName = achName.replaceAll("&[0-9a-fk-or]", "").replaceAll("[\\[\\]]", "");

                String achDesc = PlainTextComponentSerializer.plainText().serialize(display.description());

                Text name = Text.of("&5" + achName).hover(Text.of("&5" + achName + "\n&5" + achDesc));
                Text amountText = Text.of("&5] &7[+" + amount + "]");

                Text.of("You made the advancement &5[").append(name).append(amountText).send(p);

                PointsManager.getInstance().addPoints(p, amount, achName, "adv");
            }

            case GOAL -> {
                int amount = config.getInt("goal");

                String achName = PlainTextComponentSerializer.plainText().serialize(display.displayName());
                achName = achName.replaceAll("&[0-9a-fk-or]", "").replaceAll("[\\[\\]]", "");

                String achDesc = PlainTextComponentSerializer.plainText().serialize(display.description());

                Text name = Text.of("&#75E1FF" + achName).hover(Text.of("&#75E1FF" + achName + "\n&e" + achDesc));
                Text amountText = Text.of("&#75E1FF] &7[+" + amount + "]");

                Text.of("You made the advancement &#75E1FF[").append(name).append(amountText).send(p);

                PointsManager.getInstance().addPoints(p, amount, achName, "adv");
            }

            case TASK -> {
                int amount = config.getInt("task");

                String achName = PlainTextComponentSerializer.plainText().serialize(display.displayName());
                achName = achName.replaceAll("&[0-9a-fk-or]", "").replaceAll("[\\[\\]]", "");

                String achDesc = PlainTextComponentSerializer.plainText().serialize(display.description());

                Text name = Text.of("&a" + achName).hover(Text.of("&a" + achName + "\n&a" + achDesc));
                Text amountText = Text.of("&a] &7[+" + amount + "]");

                Text.of("You made the advancement &a[").append(name).append(amountText).send(p);

                PointsManager.getInstance().addPoints(p, amount, achName, "adv");
            }
        }
    }

}