package io.github.cats1337.CatsTracker.commands;

import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.points.PointsManager;
import io.github.cats1337.CatsTracker.utils.ITabCompleterHelper;
import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.utils.PointLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Command(name = "points", aliases = {"point"}, description = "Tracked points for things.")
public class PointsCommand implements ICommand {
    private final PointLogger pointLogger;
    private final List<String> categories;
    private final List<String> subCmdsAndCategories = new ArrayList<>();
                

    public PointsCommand() {
        this.pointLogger = PointLogger.getInstance();
        this.categories = CatsTracker.getInstance().getConfig().getStringList("placeholders");
        List<String> subCommands = List.of("set", "reset", "add", "remove", "lb");
        this.subCmdsAndCategories.addAll(subCommands);
        this.subCmdsAndCategories.addAll(categories);
    }

    private String getCategory(String arg) {
        switch (arg.toLowerCase()) { // Future proofing for when we add more categories that are abbreviated
            case "adv": return "Advancement";
            default: return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
        }
    }

    private void sendPointsUpdate(Player p, String action, String category) {
        Text.of("&eYour &b" + getCategory(category) + "&e points have been updated to &a" + PointsManager.getPoints(p, category) + "&e. &7[" + action + "]").send(p);

    }

    private void handlePointsModification(CommandSender sender, Player p, int amount, String action, String category) {
        if (sender.hasPermission("ctracker.admin")) {
            int oldAmount = PointsManager.getPoints(p, category);
            String amtType = switch (action) {
                case "add" -> "&a+" + amount;
                case "remove" -> "&c-" + amount;
                case "set" -> "&6" + amount;
                default -> "&7" + amount;
            };

            if (action.equals("add")){
                PointsManager.addPoints(p, amount, action, category);
            }
            if (action.equals("remove")){
                PointsManager.removePoints(p, amount, action, category);
            }
            if (action.equals("set")){
                PointsManager.setPoints(p, category, amount);
            }
            if (action.equals("reset")){
                PointsManager.setPoints(p, category, 0);
            }

            Text.of("&eChanged &a" + p.getName() + "&e's &b" + getCategory(category) + "&e points from &a" + oldAmount + "&e 🠚 " + amount + " &7[" + amtType + "&7]").send(sender);

            sendPointsUpdate(p, action, category);
        }
    }

    @Override
    public boolean execute(@NotNull CommandContext cmd) {
        CommandSender sender = cmd.sender();
        String[] args = cmd.args();

        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                Text.of("This command can only be executed by a player").send(sender);
                return true;
            }
            Text.of("&cPlease specify a category!").send(p);
            return true;
        } else {
            String arg = args[0];

            // If the command is just /points <category>, display the player's points for that category
            if (args.length == 1 && sender instanceof Player p) {
                String category = arg.toLowerCase();

                // Validate if the category exists in the placeholders list
                if (!categories.contains(category)) {
                    Text.of("&cUnknown or invalid category: " + category + ". Please check the available categories.").send(sender);
                    return true;
                }

                // Get the points for the given category
                int points = PointsManager.getPoints(p, category);
                Text.of("&eYou have &a" + points + " &b" + getCategory(category) + "&e points.").send(p);
                return true;
            }

            if (arg.equals("lb")) {
                if (args.length < 2 || args.length > 3) {
                    Text.of("&c/points lb <category> [page]").send(sender);
                    return true;
                }
                String category = args[1];
                int page = 1;
                if (args.length == 3) {
                    try {
                        page = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        Text.of("&cPage must be a number.").send(sender);
                        return true;
                    }
                }
                List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(PointsManager.getAllSortedScores(category));
                int startIndex = (page - 1) * 10;
                int endIndex = Math.min(startIndex + 10, sortedEntries.size());

                Text.of("&b&l" + getCategory(category) + " Leaderboard &7[Page " + page + "]").send(sender);
                for (int i = startIndex; i < endIndex; i++) {
                    Map.Entry<String, Integer> entry = sortedEntries.get(i);
                    Text.of("&e" + (i + 1) + ". &a" + entry.getKey() + " &7- &6" + entry.getValue()).send(sender);
                }
                return true;
            }

            if (sender.hasPermission("ctracker.admin")) {

                if (args.length < 2) {
                    Text.of("&cInsufficient arguments.").send(sender);
                    return true;
                }

                Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    Player offlinePlayer = Bukkit.getOfflinePlayer(args[1]).getPlayer();
                    if (offlinePlayer == null) {
                        Text.of("&cPlayer not found.").send(sender);
                        return true;
                    } else {
                        p = offlinePlayer; // Explicitly assign offline player here
                    }
                }

                if (args.length == 2) {
                    String category = arg.toLowerCase();
                    if (!categories.contains(category)) {
                        Text.of("&cUnknown or invalid category: " + category + ". Please check the available categories.").send(sender);
                        return true;
                    }
                    int points = PointsManager.getPoints(p, category);
                    Text.of("&e" + p.getName() + " has &a" + points + " &b" + getCategory(category) + "&e points.").send(sender);
                    return true;
                }

                switch (arg) {
                    case "reset" -> {
                        if (args.length != 3) {
                            Text.of("&c/points reset <name> <category>").send(sender);
                            return true;
                        }
                        String category = args[2];
                        PointsManager.setPoints(p, category, 0);
                        handlePointsModification(sender, p, 0, arg, category);
                        pointLogger.addEntry(p.getName() + ": Reset | " + getCategory(category) + " pts by " + sender.getName());
                    }

                    case "add", "remove", "set" -> {
                        if (args.length != 4) {
                            Text.of("&c/points " + arg + " <name> <category> <amount>").send(sender);
                            return true;
                        }
                        String category = args[2];
                        int amount;
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            Text.of("&cAmount must be a number.").send(sender);
                            return true;
                        }
                        handlePointsModification(sender, p, amount, arg, category);
                    }

                    default -> {
                        Text.of("&cUnknown subcommand. Try /points help for a list of commands.").send(sender);
                        return true;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        CommandSender sender = tab.sender();
        @NotNull String[] args = tab.args();

        // If args[0] is "lb", complete the category names for the leaderboard command
        if (args.length == 2 && args[0].equals("lb")) {
            return ITabCompleterHelper.tabComplete(args[1], categories);
        }

        // If the sender is a player and it's the first argument (category), complete from available categories
        if (args.length == 1) {
            if(sender.hasPermission("ctracker.admin")){
                return ITabCompleterHelper.tabComplete(args[0], subCmdsAndCategories);
            } else {
                return ITabCompleterHelper.tabComplete(args[0], categories);
            }
        }

        // Admin commands (set, add, remove, reset)
        if (sender.hasPermission("ctracker.admin")) {
            // Completing player names for set, add, remove, reset (second argument)
            if (args.length == 2 && (args[0].equals("set") || args[0].equals("add") || args[0].equals("remove") || args[0].equals("reset"))) {
                return ITabCompleterHelper.tabComplete(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            }

            // Completing categories for set, add, remove (third argument)
            if (args.length == 3 && (args[0].equals("set") || args[0].equals("add") || args[0].equals("remove"))) {
                return ITabCompleterHelper.tabComplete(args[2], categories);
            }
        }

        return new ArrayList<>();
    }

}
