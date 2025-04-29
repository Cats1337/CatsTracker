package io.github.cats1337.CatsTracker.commands;

import io.github.cats1337.CatsTracker.CatsTracker;
import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.utils.ITabCompleterHelper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Command(name = "ctracker", aliases = {"ctrack", "ctr", "ct"})
public class UtilCommands implements ICommand {
    private final String cmdName = "ctracker";
    private final List<String> subCommands = List.of("help", "log", "toggle", "reload");
    private final List<String> categories;

    public UtilCommands() {
        this.categories = CatsTracker.getInstance().getConfig().getStringList("placeholders");
    }

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

                if (args.length == 2) {
                    boolean newState = toggleState(category);
                    Text.of("&ePoint tracking for &b" + category + (newState ? " &aenabled" : " &cdisabled")).send(sender);
                } else {
                    String value = args[2].toLowerCase();
                    if (value.equals("on")) {
                        toggleState(true, category);
                        Text.of("&aPoint tracking enabled for &b" + category).send(sender);
                    } else if (value.equals("off")) {
                        toggleState(false, category);
                        Text.of("&cPoint tracking disabled for &b" + category).send(sender);
                    } else {
                        Text.of("&cInvalid value. Use 'on' or 'off'.").send(sender);
                    }
                }
            }

            case "notify" ->{
                if (args.length != 2) {
                    Text.of("&cUsage: /" + cmdName + " notify <true/false>").send(sender);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
                    boolean notify = Boolean.parseBoolean(args[1]);
                    CatsTracker.getInstance().getConfig().set("notify", notify);
                    CatsTracker.getInstance().saveConfig();
                    Text.of(notify ? "&aNotify enabled" : "&cNotify disabled").send(sender);
                } else {
                    Text.of("&cInvalid value. Use true or false.").send(sender);
                }
            }

            default -> {
                Text.of("&cUnknown subcommand: " + arg).send(sender);
            }
        }
        return true;
    }

    private boolean toggleState(String category) {
        boolean current = CatsTracker.getInstance().getConfig().getBoolean("trackPoints." + category);
        CatsTracker.getInstance().getConfig().set("trackPoints." + category, !current);
        CatsTracker.getInstance().saveConfig();
        return !current;
    }

    private void toggleState(boolean state, String category) {
        CatsTracker.getInstance().getConfig().set("trackPoints." + category, state);
        CatsTracker.getInstance().saveConfig();
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        CommandSender sender = tab.sender();
        @NotNull String[] args = tab.args();

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
