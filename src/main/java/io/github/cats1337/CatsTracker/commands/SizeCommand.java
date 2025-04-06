package io.github.cats1337.CatsTracker.commands;

import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import com.marcusslover.plus.lib.text.Text;
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
        if (!(sender instanceof Player p)) {
            Text.of("This command can only be executed by a player").send(sender);
            return true;
        }
        if (!sender.hasPermission("makeme.use")) {
            Text.of("&cYou do not have permission to use this command").send(sender);
            return true;
        }
        String[] args = cmd.args();

        if (args.length == 0) {
            Text.of("&cYou must specify a subcommand").send(sender);
            return true;
        }
        String arg = args[0];
        switch (arg) {
            case "small" -> {
                if (getScale(p) == .66) {
                    Text.of("&5&k!! &7You are already &dsmall&7 height. &6Goober!").send(sender);
                    return true;
                }
                setScale(p, .66);
                Text.of("&5&k!! &7You are now &dsmall&7 height.").send(sender);
            }
            case "tall", "normal" -> {
                if (getScale(p) == 1) {
                    Text.of("&5&k!! &7You are already &bnormal&7 height. &6Goober!").send(sender);
                    return true;
                }
                setScale(p, 1);
                Text.of("&5&k!! &7You are now &bnormal&7 height.").send(sender);
            }
            default -> {
                try {
                    // Check if the player has permission to set custom scale
                    if (!sender.hasPermission("makeme.admin")) {
                        Text.of("&cYou do not have permission to set a custom height.").send(sender);
                        return true;
                    }

                    double scale = Double.parseDouble(arg);
                    // Adjust scale if it's outside the valid range
                    if (scale < 0.01) {
                        scale = 0.01;
                    } else if (scale > 100) {
                        scale = 100;
                    }
                    if (getScale(p) == scale) {
                        Text.of("&5&k!! &7You are already at that height. Silly").send(sender);
                        return true;
                    }
                    setScale(p, scale);
                    Text.of("&5&k!! &7You are now set to &6" + scale + "&7 height.").send(sender);
                } catch (NumberFormatException e) {
                    Text.of("&cInvalid argument! Please specify a valid height.").send(sender);
                }
            }
        }
        return true;
    }

    double getScale(Player p) {return Objects.requireNonNull(p.getAttribute(Attribute.SCALE)).getBaseValue();}
    void setScale(Player p, double scale) {Objects.requireNonNull(p.getAttribute(Attribute.SCALE)).setBaseValue(scale);}

}
