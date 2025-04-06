package io.github.cats1337.CatsTracker.commands;

import io.github.cats1337.CatsTracker.CatsTracker;
import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.utils.ITabCompleterHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Command(name = "ctracker", aliases = {"ctrack", "ctr", "ct"})
public class UtilCommands implements ICommand {
    FileConfiguration config = CatsTracker.getInstance().getConfig();
    private final String cmdName = "ctracker";
    private final List<String> subCommands = List.of("help", "log", "toggle", "reload");
    private final List<String> categories = config.getStringList("placeholders");

    // get placeholders from config

    @Override
    public boolean execute(@NotNull CommandContext cmd) {
        CommandSender sender = cmd.sender();
        String[] args = cmd.args();
        if (!sender.hasPermission("ctracker.admin")) {
            Text.of("&cYou do not have permission to use this command").send(sender);
            return true;
        }
        if (args.length == 0) {
            Text.of("&cInvalid usage! Use /" + cmdName + " help for more info.").send(sender);
            return true;
        }

        String arg = args[0];
        switch (arg) {
            case "help" -> {
                Text.of("&a/" + cmdName + " log <true/false> &r- &6Toggles console logging").send(sender);
                Text.of("&a/" + cmdName + " toggle <category> <on/off> &r- &6Toggles point tracking").send(sender);
                Text.of("&a/" + cmdName + " reload &r- &6Reloads the config").send(sender);
                return true;
            }
            case "log" -> {
                if (args.length != 2) {
                    Text.of("&c/" + cmdName + " log <true/false>").send(sender);
                    return true;
                }
                String value = args[1];
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    boolean logMessages = Boolean.parseBoolean(value);
                    CatsTracker.getInstance().getConfig().set("logMessages", logMessages);
                    CatsTracker.getInstance().saveConfig();
                    Text.of(logMessages ? "&aLog messages enabled" : "&cLog messages disabled").send(sender);
                } else {
                    Text.of("&c/" + cmdName + " log <true/false>").send(sender);
                }
            }
            case "reload" -> {
                CatsTracker.getInstance().reload();
                Text.of("&aReloaded config").send(sender);
                return true;
            }
            case "toggle" -> {
                // arg[0] = toggle
                // arg[1] = category
                // arg[2] = on/off
                // length = 3
                String category = args[1];
                if (args.length != 3) {
                    boolean state = toggleState(category);
                    Text.of("&ePoint tracking for " + category + (state ? " &aenabled" : " &cdisabled")).send(sender);
//                    Text.of(state ? "&aPoint tracking enabled" : "&cPoint tracking disabled").send(sender);

                    return true;
                }
                String value = args[2];
                if(value.equalsIgnoreCase("on")) {
                    toggleState(true, category);
                    Text.of("&aPoint tracking enabled").send(sender);
                    return true;
                }
                if(value.equalsIgnoreCase("off")) {
                    toggleState(false, category);
                    Text.of("&cPoint tracking disabled").send(sender);
                    return true;
                }
                else {
                    Text.of("&c/" + cmdName + "toggle <category> [on/off]").send(sender);
                }
                return true;
            }

            default -> {
                Text.of("&cUnknown subcommand: " + arg).send(sender);
                return true;
            }
        }
        return true;
    }

    /**
     * Toggles the state of a category in the config.
     * @param category The category to toggle.
     * @return The new state of the category.
     */
    private boolean toggleState(String category) {
        boolean currentState = CatsTracker.getInstance().getConfig().getBoolean("trackPoints." + category);
        CatsTracker.getInstance().getConfig().set("trackPoints." + category , !currentState);
        CatsTracker.getInstance().saveConfig();
        return !currentState;
    }

    /**
     * Sets the state of a category in the config.
     * @param state The new state to set.
     * @param category The category to set.
     */
    private void toggleState(boolean state, String category) {
        CatsTracker.getInstance().getConfig().set("trackPoints." + category, state);
        CatsTracker.getInstance().saveConfig();
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        CommandSender sender = tab.sender();
        @NotNull String[] args = tab.args();

        if (sender.hasPermission("ctracker.admin")) {
            if (args.length == 1) {
                return getSubCommandSuggestions(args[0]);
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("log")) {
                    return List.of("true", "false");
                }
            }
            if (args.length == 3 && args[0].equals("toggle")) {
                return ITabCompleterHelper.tabComplete(args[1], categories);
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("toggle")) {
                return ITabCompleterHelper.tabComplete(args[1], categories);
            }
        }
        return new ArrayList<>();
    }

    private List<String> getSubCommandSuggestions(String arg) {
        List<String> suggestions = new ArrayList<>();
        for (String subCommand : subCommands) {
            if (subCommand.startsWith(arg.toLowerCase())) {
                suggestions.add(subCommand);
            }
        }
        return suggestions;
    }
}
