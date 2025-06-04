package io.github.cats1337.CatsTracker.commands;

import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.attribute.Attribute;

import java.util.Objects;

@Command(name = "makeme")
public class SizeCommand implements ICommand {
// /makeme <small, tall, normal>
    @Override
    public boolean execute(CommandContext cmd) {
        CommandSender sender = cmd.sender();
        if (!(sender instanceof Player) && cmd.args().length < 2) {
            Text.of("This command can only be executed by a player").send(sender);
            return true;
        }

        if (!sender.hasPermission("makeme.use")) {
            Text.of("&cYou do not have permission to use this command.").send(sender);
            return true;
        }

        String[] args = cmd.args();
        if (args.length == 0) {
            Text.of("&cYou must specify a subcommand. \n &cValid Subcommands: <small,mini|tall,normal>").send(sender);
            if (sender.hasPermission("makeme.admin")) {
                Text.of("&cValid Subcommands: <small|tall|normal|scale> [player]");
            }
            return true;
        }

        String sizeArg = args[0];
        Player target;
        // If specifying a player, check if they are online
        if (args.length == 2) {
            if (isNotAdmin(sender)) return true;

            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Text.of("&cPlayer not found or is offline.").send(sender);
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                Text.of("This command can only be executed by a player").send(sender);
                return true;
            }
            target = player;
        }

        switch (sizeArg.toLowerCase()) {
            case "small", "mini" -> {
                if (getScale(target) == 0.66) {
                    if (sender == target) {
                        Text.of("&5&k!! &7You're already &dsmall&7 height.").send(sender);
                    } else {
                        Text.of("&5&k!! &7" + target.getName() + " is already &dsmall&7 height.").send(sender);
                    }
                    return true;
                }
                setScale(target, 0.66);
                if (sender == target) {
                    Text.of("&5&k!! &7You're now &dsmall&7 height.").send(sender);
                } else {
                    Text.of("&5&k!! &7" + target.getName() + " you're now &dsmall&7 height.").send(sender);
                }
            }
            case "tall", "normal" -> {
                if (getScale(target) == 1.0) {
                    if (sender == target) {
                        Text.of("&5&k!! &7You're already &bnormal&7 height.").send(sender);
                    } else {
                        Text.of("&5&k!! &7" + target.getName() + " is already &bnormal&7 height.").send(sender);
                    }
                    return true;
                }
                setScale(target, 1.0);
                if (sender == target) {
                    Text.of("&5&k!! &7You're now &bnormal&7 height.").send(sender);
                } else {
                    Text.of("&5&k!! &7" + target.getName() + " you're now &bnormal&7 height.").send(sender);
                }
            }
            default -> {
                try {
                    if (isNotAdmin(sender)) return true;

                    double scale = Double.parseDouble(sizeArg);
                    scale = Math.min(Math.max(scale, 0.01), 100);

                    if (getScale(target) == scale) {
                        Text.of("&5&k!! &7" + target.getName() + " is already at that height.").send(sender);
                        return true;
                    }

                    setScale(target, scale);
                    Text.of("&5&k!! &7" + target.getName() + " is now set to &6" + scale + "&7 height.").send(sender);
                } catch (NumberFormatException e) {
                    Text.of("&cInvalid argument! Please specify a valid height.").send(sender);
                }
            }
        }

        return true;
    }

    // if they have the admin permission, continue, otherwise send a message
    // and return true
    public static boolean isNotAdmin(CommandSender sender) {
        if (sender.hasPermission("makeme.admin")) {
            return false;
        } else {
            Text.of("&cYou do not have permission to use this command.").send(sender);
            return true;
        }
    }

    public static double getScale(Player p) {
        return Objects.requireNonNull(p.getAttribute(Attribute.SCALE)).getBaseValue();
    }

    public static void setScale(Player p, double scale) {
        Objects.requireNonNull(p.getAttribute(Attribute.SCALE)).setBaseValue(scale);
    }
}
