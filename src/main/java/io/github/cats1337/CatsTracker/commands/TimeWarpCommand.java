package io.github.cats1337.CatsTracker.commands;

import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.CatsTracker.CatsTracker;
import io.github.cats1337.CatsTracker.utils.ITabCompleterHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Command(name = "timewarp")
public class TimeWarpCommand implements ICommand {
    private static final Map<World, Integer> taskIDs = new HashMap<>();
    private static final Map<World, Double> accumulators = new HashMap<>();
    private static int taskId;
    private static double daySpeed = CatsTracker.getInstance().getConfig().getDouble("timewarp.daySpeed", 0.0);
    private static double nightSpeed = CatsTracker.getInstance().getConfig().getDouble("timewarp.nightSpeed", 0.0);

    @Override
    public boolean execute(CommandContext cmd) {
        CommandSender sender = cmd.sender();
        String[] args = cmd.args();

        if (!sender.hasPermission("timewarp.use")) {
            Text.of("&cYou do not have permission to use this command.").send(sender);
            return true;
        }

        if (args.length == 1) {
            String sub = args[0].toLowerCase();

            switch (sub) {
                case "start" -> {
                    startTimeWarp();
                    Text.of("&aTime warp started (Day: " + daySpeed + " | Night: " + nightSpeed + ").").send(sender);
                }
                case "stop" -> {
                    stopTimeWarp();
                    Text.of("&cTime warp stopped.").send(sender);
                }
                case "status" -> {
                    if (taskIDs.isEmpty()) {
                        Text.of("&cTime warp is not active.").send(sender);
                    } else {
                        StringBuilder worlds = new StringBuilder();
                        taskIDs.keySet().forEach(world -> worlds.append(world.getName()).append(", "));
                        worlds.setLength(worlds.length() - 2);
                        Text.of("&aTime warp active (Day: " + daySpeed + " | Night: " + nightSpeed + ") in: " + worlds).send(sender);
                    }
                }
                default -> {
                    Text.of("&cUnknown subcommand. Use: start, stop, set, or status.").send(sender);
                }
            }

            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String period = args[1].toLowerCase();
            try {
                double level = Double.parseDouble(args[2]);
                if (level < 0 || level > 100) throw new NumberFormatException();

                switch (period) {
                    case "day" -> {
                        daySpeed = level;
                        Text.of("&aDay speed set to " + level + ".").send(sender);
                    }
                    case "night" -> {
                        nightSpeed = level;
                        Text.of("&aNight speed set to " + level + ".").send(sender);
                    }
                    default -> {
                        Text.of("&cUnknown period. Use 'day' or 'night'.").send(sender);
                        return true;
                    }
                }

                stopTimeWarp(); // Reset if changing values during runtime
//                save config
                CatsTracker.getInstance().getConfig().set("timewarp.daySpeed", daySpeed);
                CatsTracker.getInstance().getConfig().set("timewarp.nightSpeed", nightSpeed);
                CatsTracker.getInstance().saveConfig();

            } catch (NumberFormatException e) {
                Text.of("&cInvalid speed. Use a number between 0 and 100.").send(sender);
            }
            return true;
        }

        Text.of("&cUsage: /timewarp <start|stop|set|status>").send(sender);
        return true;
    }

    private void startTimeWarp() {
        stopTimeWarp();

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) continue;

            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CatsTracker.getInstance(), () -> {
                long time = world.getTime();
                boolean isDay = time >= 0 && time < 12300;

                // Scale warpLevel: higher = faster day, slower night (or flip depending on intent)
                double speed = isDay ? daySpeed : nightSpeed;

                double acc = accumulators.getOrDefault(world, 0.0);
                acc += speed;

                while (acc >= 1.0) {
                    time += 1;
                    if (time >= 24000) time -= 24000;
                    acc -= 1.0;
                }

                accumulators.put(world, acc);
                world.setTime(time);

            }, 1L, 1L);


            taskIDs.put(world, taskId);
        }
    }

    public static void stopTimeWarp() {
        taskIDs.values().forEach(Bukkit.getScheduler()::cancelTask);
        taskIDs.clear();
        accumulators.clear();
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        CommandSender sender = tab.sender();
        @NotNull String[] args = tab.args();

        if (!sender.hasPermission("timewarp.use")) return new ArrayList<>();
        if (args.length == 1) return ITabCompleterHelper.tabComplete(args[0], List.of("start", "stop", "set", "status"));
        if (args[0].equalsIgnoreCase("set") && args.length == 2) {
            return ITabCompleterHelper.tabComplete(args[1], List.of("day", "night"));
        }

        return new ArrayList<>();
    }

}
