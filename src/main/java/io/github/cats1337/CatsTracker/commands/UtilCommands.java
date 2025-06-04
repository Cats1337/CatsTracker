package io.github.cats1337.CatsTracker.commands;

import io.github.cats1337.CatsTracker.CatsTracker;
import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.playerdata.PlayerContainer;
import io.github.cats1337.CatsTracker.playerdata.PlayerHandler;
import io.github.cats1337.CatsTracker.playerdata.ServerPlayer;
import io.github.cats1337.CatsTracker.utils.ITabCompleterHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Command(name = "ctracker", aliases = {"ctrack", "ct"})
public class UtilCommands implements ICommand {
    private final List<String> subCommands = List.of("help", "log", "toggle", "reload", "notify");
    private final List<String> categories;

    public UtilCommands() {
        this.categories = CatsTracker.getInstance().getConfig().getStringList("placeholders");
    }

    @Override
    public boolean execute(@NotNull CommandContext cmd) {
        CommandSender sender = cmd.sender();
        String[] args = cmd.args();

        if (!sender.hasPermission("ctracker.admin") && !(args.length > 0 && args[0].equalsIgnoreCase("notify"))) {
            Text.of("&cYou don't have permission to use this command.").send(sender);
            return true;
        }

        String cmdName = "ctracker";
        if (args.length == 0) {
            Text.of("&cUsage: /" + cmdName + " <" + String.join("/", subCommands) + ">").send(sender);
            return true;
        }

        String arg = args[0].toLowerCase();
        switch (arg) {
            case "help" -> {
                Text.of("&a/" + cmdName + " log <true/false> &7- &6Toggles console logging").send(sender);
                Text.of("&a/" + cmdName + " toggle <category> <on/off> &7- &6Toggles point tracking").send(sender);
                Text.of("&a/" + cmdName + " reload &7- &6Reloads the config").send(sender);
            }

            case "log" -> {
                if (args.length != 2) {
                    Text.of("&cUsage: /" + cmdName + " log <true/false>").send(sender);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
                    boolean logMessages = Boolean.parseBoolean(args[1]);
                    CatsTracker.getInstance().getConfig().set("logMessages", logMessages);
                    CatsTracker.getInstance().saveConfig();
                    Text.of(logMessages ? "&aLog messages enabled" : "&cLog messages disabled").send(sender);
                } else {
                    Text.of("&cInvalid value. Use true or false.").send(sender);
                }
            }

            case "reload" -> {
                CatsTracker.getInstance().reload();
                Text.of("&aReloaded config").send(sender);
            }

            case "toggle" -> {
                if (args.length < 2) {
                    Text.of("&cUsage: /" + cmdName + " toggle <category> [on/off]").send(sender);
                    return true;
                }

                String category = args[1];
                if (!categories.contains(category)) {
                    Text.of("&cUnknown category: " + category).send(sender);
                    return true;
                }

                String type = "trackPoints";
                if (args.length == 2) {
                    boolean newState = toggleState(type, category);
                    Text.of("&ePoint tracking for &b" + category + (newState ? " &aenabled" : " &cdisabled")).send(sender);
                } else {
                    String value = args[2].toLowerCase();
                    if (value.equals("on")) {
                        toggleState(type, true, category);
                        Text.of("&aPoint tracking enabled for &b" + category).send(sender);
                    } else if (value.equals("off")) {
                        toggleState(type, false, category);
                        Text.of("&cPoint tracking disabled for &b" + category).send(sender);
                    } else {
                        Text.of("&cInvalid value. Use 'on' or 'off'.").send(sender);
                    }
                }
            }

            case "notify" -> {
                if (!(sender instanceof Player player)) {
                    Text.of("&cThis command can only be used by players.").send(sender);
                    return true;
                }

                if (args.length < 2) {
                    Text.of("&cUsage: /" + cmdName + " notify <category> [on/off]").send(sender);
                    return true;
                }

                String category = args[1].toLowerCase();
                if (!categories.contains(category)) {
                    Text.of("&cUnknown category: " + category).send(sender);
                    return true;
                }

                PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
                ServerPlayer serverPlayer = playerContainer.loadData(player.getUniqueId());

//                if (args.length == 2) {
//                    // Toggle notification
//                    boolean newState = serverPlayer.toggleNotify(category);
//                    Text.of("&eNotifications for &b" + category + (newState ? " &aenabled" : " &cdisabled")).send(sender);
//                } else {
//                    // Set notification state
//                    String value = args[2].toLowerCase();
//                    if (value.equals("on")) {
//                        serverPlayer.setNotifyEnabled(category, true);
//                        Text.of("&aNotifications enabled for &b" + category).send(sender);
//                    } else if (value.equals("off")) {
//                        serverPlayer.setNotifyEnabled(category, false);
//                        Text.of("&cNotifications disabled for &b" + category).send(sender);
//                    } else {
//                        Text.of("&cInvalid value. Use 'on' or 'off'.").send(sender);
//                        return true;
//                    }
//                }

                // Save player data
                playerContainer.writeData(player.getUniqueId(), serverPlayer);
                return true;
            }

            default -> Text.of("&cUnknown subcommand: " + arg).send(sender);
        }
        return true;
    }

    private boolean toggleState(String type,String category) {
        boolean current = CatsTracker.getInstance().getConfig().getBoolean(type +"." + category);
        CatsTracker.getInstance().getConfig().set(type +"." + category, !current);
        CatsTracker.getInstance().saveConfig();
        return !current;
    }

    private void toggleState(String type, boolean state, String category) {
        CatsTracker.getInstance().getConfig().set(type + "." + category, state);
        CatsTracker.getInstance().saveConfig();
    }

@Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        CommandSender sender = tab.sender();
        @NotNull String[] args = tab.args();

        if (args[0].equalsIgnoreCase("notify")) {
            if (args.length == 2)
                return ITabCompleterHelper.tabComplete(args[1], categories);
            if (args.length == 3)
                return ITabCompleterHelper.tabComplete(args[2], List.of("on", "off"));
        }

        if (!sender.hasPermission("ctracker.admin")) return new ArrayList<>();

        if (args.length == 1) return ITabCompleterHelper.tabComplete(args[0], subCommands);

        if (args[0].equalsIgnoreCase("log") && args.length == 2)
            return List.of("true", "false");

        if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length == 2)
                return ITabCompleterHelper.tabComplete(args[1], categories);
            if (args.length == 3)
                return ITabCompleterHelper.tabComplete(args[2], List.of("on", "off"));
        }

        return new ArrayList<>();
    }
}
