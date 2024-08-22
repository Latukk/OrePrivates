package org.latuk.oreprivates;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Completer implements TabCompleter {

    private final OrePrivates plugin; // Изменяем тип на OrePrivates
    private final Utils utils;

    public Completer(OrePrivates plugin, Utils utils) {
        this.plugin = plugin;
        this.utils = utils;
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("rg")) {
                if (args.length == 1) {
                    List<String> subcommands = new ArrayList<>();
                    subcommands.add("info");
                    // subcommands.add("list");
                    subcommands.add("removemember");
                    subcommands.add("addmember");
                    return subcommands;
                }

                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("addmember") || args[0].equalsIgnoreCase("removemember")) { // Проверяем args[0]
                        Location location = player.getLocation();
                        if (utils.isLocationInRegion(location)) {
                            String regionName = utils.getRegionByLocation(location);
                            List<String> argument = new ArrayList<>();
                            argument.add(regionName);
                            return argument;
                        }
                    }
                }
            }
        }

        return null;
    }

}
