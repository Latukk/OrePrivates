package org.latuk.oreprivates.handler;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.latuk.oreprivates.OrePrivates;
import org.latuk.oreprivates.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleEventHandler implements Listener {
    private final OrePrivates plugin; // Изменяем тип на OrePrivates
    private final Utils utils;

    public SimpleEventHandler(OrePrivates plugin, Utils utils) {
        this.plugin = plugin;
        this.utils = utils;
    }
    @EventHandler
    public void placeBlockEvent(BlockPlaceEvent event) { // Обработка установки блоков
        Block block = event.getBlock();
        World world = block.getWorld();
        Material blockMaterial = block.getType();
        Player player = event.getPlayer();
        if (utils.isBlockInRegion(block)) { // Если блок находится в регионе
            String regionName = utils.getRegionByBlock(block);
            if (!utils.isPlayerOwnerOfRegion(player, regionName) && !utils.isPlayerMemberOfRegion(player, regionName)) { // Если у игрока нет прав ломать блоки в этом регионе
                event.setCancelled(true);
                String message = utils.getMessageFromConfig("you-are-not-member-of-region");
                player.sendMessage(ChatColor.RED + message);
            }
        }
        if (utils.isMaterialIsPrivateBlock(blockMaterial) && world.getEnvironment() == World.Environment.NORMAL) { // Если игрок поставил блок привата
            Map<String, Object> result = regionClaimHandler(block, player);
            boolean isCreated = (boolean) result.get("created");
            String message = (String) result.get("message");

            if (isCreated) {
                player.sendMessage(ChatColor.GREEN + message);
            } else {
                player.sendMessage(ChatColor.RED + message);
            }
        }
    }

    @EventHandler
    public void breakBlockEvent(BlockBreakEvent event) { // Обработка поломки блоков
        Block block = event.getBlock();
        World world = block.getWorld();
        Player player = event.getPlayer();
        if (utils.isBlockInRegion(block)) {
            String regionName = utils.getRegionByBlock(block);
            if (!utils.isPlayerMemberOfRegion(player, regionName) && !utils.isPlayerOwnerOfRegion(player, regionName)) {
                event.setCancelled(true);
                String message = utils.getMessageFromConfig("you-are-not-member-of-region");
                player.sendMessage(ChatColor.RED + message);
            }
            if (utils.isBlockPrivate(block, regionName)) {
                Map<String, Object> result = regionRemoveHandler(regionName, player);
                boolean isRemoved = (boolean) result.get("removed");
                String message = (String) result.get("message");
                if (isRemoved) {
                    player.sendMessage(ChatColor.GREEN + message);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + message);
                }
            }
        }
    }

    @EventHandler
    public void explodeBlockEvent(EntityExplodeEvent event) {
        List<Block> blocksToRemove = new ArrayList<>();

        for (Block eblock : event.blockList()) {
            if (utils.isBlockInRegion(eblock)) {
                String regionName = utils.getRegionByBlock(eblock);

                if (utils.isBlockPrivate(eblock, regionName)) {
                    blocksToRemove.add(eblock); // Добавляем блок в список на удаление
                }
            }
        }

        // Удаляем блоки из event.blockList()
        event.blockList().removeAll(blocksToRemove);
    }

    @EventHandler
    public void interactPlayerEvent(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK && utils.isBlockInRegion(block)) {
            String regionName = utils.getRegionByBlock(block);
            if (!utils.isPlayerOwnerOfRegion(player, regionName) && !utils.isPlayerMemberOfRegion(player, regionName)) {
                event.setCancelled(true);
                String message = utils.getMessageFromConfig("you-are-not-member-of-region");
                player.sendMessage(ChatColor.RED + message);
            }
        }
    }


    public Map<String, Object> regionRemoveHandler(String regionName, Player player) {
        Map<String, Object> result = new HashMap<>();

        if (utils.isPlayerOwnerOfRegion(player, regionName)) {
            utils.removeRegion(regionName);

            String message = utils.getMessageFromConfig("region-removed-successfully");
            result.put("removed", true);
            result.put("message", message);
        } else {
            String message = utils.getMessageFromConfig("you-are-not-owner-of-region");
            result.put("removed", false);
            result.put("message", message);
        }

        return result;
    }

    public Map<String, Object> regionClaimHandler(Block block, Player player) {
        Map<String, Object> result = new HashMap<>();
        World world = block.getWorld();
        Material blockMaterial = block.getType();


        int radius = utils.getPrivateBlockRadius(blockMaterial); // Получение радиуса у блока привата
        int[] regionCoordinates = utils.getRegionCoordinates(block, radius); // Получение будущих границ региона

        if (utils.isBlockInRegion(block)) { // Если блок в регионе
            String message = utils.getMessageFromConfig("private-block-in-region");
            result.put("created", false);
            result.put("message", message);
        } else if (utils.isRegionOverlapping(regionCoordinates)) { // Если регионы пересекаются
            String message = utils.getMessageFromConfig("regions-overlaps");
            result.put("created", false);
            result.put("message", message);
        } else if (!utils.isBlockInRegion(block) && !utils.isRegionOverlapping(regionCoordinates)) { // Если блок не в регионе и регионы не пересекаются
            String message = utils.getMessageFromConfig("region-created-successfully");
            result.put("created", true);
            result.put("message", message);

            utils.saveRegion(player.getName(), regionCoordinates); // Сохранение региона в конфиг
        }
        return result;
    }




}