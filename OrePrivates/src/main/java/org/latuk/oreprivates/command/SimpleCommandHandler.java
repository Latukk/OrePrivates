package org.latuk.oreprivates.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.latuk.oreprivates.OrePrivates;
import org.latuk.oreprivates.Utils;

import java.util.List;

public class SimpleCommandHandler implements CommandExecutor {

    private final OrePrivates plugin;
    private final Utils utils;

    public SimpleCommandHandler(OrePrivates plugin, Utils utils) {
        this.plugin = plugin;
        this.utils = utils;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("rg")) {
                if (args.length == 0) { // Проверяем, есть ли аргументы
                    String error_message = utils.getMessageFromConfig("unknown-command");
                    sender.sendMessage(ChatColor.RED + error_message);
                    return true;
                }

                // if (args[0].equalsIgnoreCase("list")) {
                //     rgListHandler(player);
                if (args[0].equalsIgnoreCase("info")) {
                    rgInfoHandler(player);
                } else if (args[0].equalsIgnoreCase("addmember")) {
                    rgAddMemberHandler(player, args);
                } else if (args[0].equalsIgnoreCase("removemember")) {
                    rgRemoveMemberHandler(player, args);
                } else {
                    String error_message = utils.getMessageFromConfig("unknown-command");
                    sender.sendMessage(ChatColor.RED + error_message);
                }
            }
        }
        return true;
    }


    public void rgListHandler(Player player) {
        List<String> regions = utils.getRegionList();
        if (regions.isEmpty()) {
            String error_message = utils.getMessageFromConfig("region-list-not-found");
            player.sendMessage(ChatColor.RED + error_message);
        } else {
            StringBuilder message = new StringBuilder("Список регионов: " + ChatColor.YELLOW + "\n");
            for (int i = 0; i < regions.size(); i++) {
                message.append(i + 1).append(". ").append(regions.get(i)).append("\n");
            }
            player.sendMessage(message.toString());
        }
    }

    public void rgInfoHandler(Player player) {
        Location location = player.getLocation();
        if (utils.isLocationInRegion(location)) {
            String regionName = utils.getRegionByLocation(location);
            String regionInfo = utils.getRegionInfoString(regionName);
            player.sendMessage(regionInfo);
        } else {
            String error_message = utils.getMessageFromConfig("region-info-rg-not-found");
            player.sendMessage(ChatColor.RED + error_message);
        }
    }

    public void rgAddMemberHandler(Player player, String[] args) {
        if (args.length < 3) { // Проверяем, что передано хотя бы 3 аргумента
            String message = utils.getMessageFromConfig("rg-am-how-to-use");
            player.sendMessage(ChatColor.RED + message);
            return;
        }

        String regionName = args[1];
        String playerToAddName = args[2];

        if (regionName.isEmpty()) {
            String message = utils.getMessageFromConfig("rg-am-enter-rg-name");
            player.sendMessage(ChatColor.RED + message);
            return;
        }

        if (playerToAddName.isEmpty()) {
            String message = utils.getMessageFromConfig("rg-am-enter-player-name");
            player.sendMessage(ChatColor.RED + message);
            return;
        }

        if (utils.isPlayerOwnerOfRegion(player, regionName)) {
            utils.addMember(playerToAddName, regionName);
            String message = utils.getMessageFromConfig("rg-am-player-added-to-region");
            player.sendMessage(ChatColor.GREEN + message);
        } else {
            String message = utils.getMessageFromConfig("you-are-not-owner-of-region");
            player.sendMessage(ChatColor.RED + message);
        }
    }


    public void rgRemoveMemberHandler(Player player, String[] args) {
        if (args.length < 3) { // Проверяем, что передано хотя бы 3 аргумента
            String message = utils.getMessageFromConfig("rg-rm-how-to-use");
            player.sendMessage(ChatColor.RED + message);
            return;
        }

        String regionName = args[1];
        String playerToRemoveName = args[2];

        if (regionName.isEmpty()) {
            String message = utils.getMessageFromConfig("rg-am-enter-rg-name");
            player.sendMessage(ChatColor.RED + message);
            return;
        }

        if (playerToRemoveName.isEmpty()) {
            String message = utils.getMessageFromConfig("rg-am-enter-player-name");
            player.sendMessage(ChatColor.RED + message);
            return;
        }

        if (utils.isPlayerOwnerOfRegion(player, regionName)) {
            utils.removeMember(playerToRemoveName, regionName);
            String message = utils.getMessageFromConfig("rg-rm-player-removed-from-region");
            player.sendMessage(ChatColor.GREEN + message);
        } else {
            String message = utils.getMessageFromConfig("you-are-not-owner-of-region");
            player.sendMessage(ChatColor.RED + message);
        }
    }

}
