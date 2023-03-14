package org.twistermc.contraband.crate.command;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.twistermc.contraband.Main;
import org.twistermc.contraband.crate.CrateManager;
import org.twistermc.contraband.utils.Color;

import java.util.Objects;

@Getter
public class ContrabandCommand implements CommandExecutor {

    private final Main plugin;
    private final CrateManager crateManager;

    public ContrabandCommand(Main plugin) {
        this.plugin = plugin;
        this.crateManager = new CrateManager(this.plugin);
    }

    @Override
    @Deprecated
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            wrongUsage(sender);
        } else if (args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("contraband.setlocation")) {
                sender.sendMessage(Color.translate(this.plugin.getConfigConfiguration().getString("no-permissions")));
                return false;
            }

            this.crateManager.createCrate((Player) sender);
            sender.sendMessage(Color.translate(this.plugin.getConfigConfiguration().getString("MESSAGES.SET")));
        } else if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("contraband.give")) {
                sender.sendMessage(Color.translate(this.plugin.getConfigConfiguration().getString("no-permissions")));
                return false;
            }

            if (args.length == 1) {
                wrongUsage(sender);
            } else {
                String tier = (args[1]);
                if (args.length == 2) {
                    wrongUsage(sender);
                } else {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage(Color.translate(Objects.requireNonNull(this.plugin.getConfigConfiguration().getString("MESSAGES.OFFLINE"))
                                .replace("%player%", Objects.requireNonNull(target).getName())));
                        return false;
                    }
                    if (args.length == 3) {
                        wrongUsage(sender);
                    } else {
                        int amount = Integer.parseInt(args[3]);
                        this.crateManager.giveCrate(target, amount, tier);
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("contraband.reload")) {
                sender.sendMessage(Color.translate(this.plugin.getConfigConfiguration().getString("no-permissions")));
                return false;
            }

            this.crateManager.reload();
        } else if (args[0].equalsIgnoreCase("giveall")) {
            if (!sender.hasPermission("contraband.giveall")) {
                sender.sendMessage(Color.translate(this.plugin.getConfigConfiguration().getString("no-permissions")));
                return false;
            }

            if (args.length == 1) {
                wrongUsage(sender);
            } else {
                String tier = (args[1]);
                if (args.length == 2) {
                    wrongUsage(sender);
                } else {
                    int amount = Integer.parseInt(args[2]);
                    this.crateManager.giveAllCrate(amount, tier);
                }
            }
        }

        return true;
    }

    private void wrongUsage(CommandSender sender){
        for (final String lines : this.plugin.getConfigConfiguration().getStringList("usage")) {
            sender.sendMessage(Color.translate(lines));
        }
    }
}
