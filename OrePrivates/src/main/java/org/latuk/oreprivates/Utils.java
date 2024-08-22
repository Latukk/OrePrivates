package org.latuk.oreprivates;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Utils {

    private final OrePrivates plugin;
    private File configFile;
    private FileConfiguration config;


    public Utils(OrePrivates plugin) {
        this.plugin = plugin;
        createConfig();
    }

    public boolean isMaterialIsPrivateBlock(Material block) { // Проверка является ли блок блоком привата
        FileConfiguration config = plugin.getConfig(); // Получение конфига
        for (String key : config.getConfigurationSection("blocks").getKeys(false)) { // Все под-пути в blocks
            if (block.name().equals(key)) { // Используем equals для сравнения строк
                return true;
            }
        }
        return false;
    }

    public int getPrivateBlockRadius(Material block) { // Получение размера привата у блока из конфига
        FileConfiguration config = plugin.getConfig(); // Получение конфига
        for (String key : config.getConfigurationSection("blocks").getKeys(false)) { // Все под-пути в blocks
            if (block.name().equals(key)) { // Используем equals для сравнения строк
                int radius = config.getInt("blocks." + key); // Радиус блока
                return radius;
            }
        }
        return 0;
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "regions.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("regions.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static int[] getRegionCoordinates(Block block, int radius) {
        // Получаем координаты блока
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        // Вычисляем координаты углов региона
        int x1 = x - radius;
        int y1 = y - radius;
        int z1 = z - radius;

        int x2 = x + radius;
        int y2 = y + radius;
        int z2 = z + radius;

        // Возвращаем массив с координатами
        return new int[]{x1, y1, z1, x2, y2, z2, x, y, z};
    }

    public void saveRegion(String regionOwner, int[] regionCoordinates) { // Добавление региона в конфиг
        FileConfiguration config = plugin.getRegionsConfig();
        String regionName = regionOwner + "|" + regionCoordinates[6] + "," + regionCoordinates[7] + "," + regionCoordinates[8];
        config.set("regions." + regionName + ".owner", regionOwner); // Добавление значения в конфиг
        config.set("regions." + regionName + ".members", new ArrayList<String>()); // Добавление значения в конфиг
        config.set("regions." + regionName + ".bx", regionCoordinates[6]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".by", regionCoordinates[7]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".bz", regionCoordinates[8]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".x1", regionCoordinates[0]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".y1", regionCoordinates[1]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".z1", regionCoordinates[2]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".x2", regionCoordinates[3]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".y2", regionCoordinates[4]); // Добавление значения в конфиг
        config.set("regions." + regionName + ".z2", regionCoordinates[5]); // Добавление значения в конфиг

        plugin.saveRegionsConfig();
    }

    public void removeRegion(String regionName) { // Удаление региона из конфига
        FileConfiguration config = plugin.getRegionsConfig();

        if (config.contains("regions." + regionName)) {
            config.set("regions." + regionName, null);
        }

        plugin.saveRegionsConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessageFromConfig(String message) {
        config = plugin.getConfig();
        String result = config.getString("messages." + message);
        return result != null ? result : "Произошла неизвестная ошибка!";
    }


    public List<String> getRegionList() {
        List<String> regionList = new ArrayList<>();

        if (plugin.getRegionsConfig().contains("regions")) { // Проверяем, есть ли секция "regions"
            for (String regionName : plugin.getRegionsConfig().getConfigurationSection("regions").getKeys(false)) {
                regionList.add(regionName);
            }
        }

        return regionList;
    }


    public Map<String, Object> getRegionInfo(String regionName) {
        FileConfiguration config = plugin.getRegionsConfig();
        String path = "regions." + regionName;

        if (config.contains(path)) {
            Map<String, Object> regionInfo = new HashMap<>();
            regionInfo.put("owner", config.getString(path + ".owner"));
            regionInfo.put("members", config.getStringList(path + ".members"));
            regionInfo.put("bx", config.getInt(path + ".bx"));
            regionInfo.put("by", config.getInt(path + ".by"));
            regionInfo.put("bz", config.getInt(path + ".bz"));
            regionInfo.put("x1", config.getInt(path + ".x1"));
            regionInfo.put("y1", config.getInt(path + ".y1"));
            regionInfo.put("z1", config.getInt(path + ".z1"));
            regionInfo.put("x2", config.getInt(path + ".x2"));
            regionInfo.put("y2", config.getInt(path + ".y2"));
            regionInfo.put("z2", config.getInt(path + ".z2"));

            return regionInfo;
        }

        return null; // Если региона с таким названием нет
    }

    public String getRegionInfoString(String regionName) {
        Map<String, Object> regionInfo = getRegionInfo(regionName);
        if (regionInfo == null) {
            plugin.getLogger().warning("No region info found for region: " + regionName);
            return ChatColor.RED + "Регион не найден.";
        }

        StringBuilder regionInfoString = new StringBuilder(ChatColor.GRAY + "Информация о регионе " + ChatColor.GREEN + regionName + ChatColor.GRAY +  ":\n");
        regionInfoString.append(ChatColor.GRAY + "Владелец: " + ChatColor.GREEN + regionInfo.get("owner") + "\n");
        regionInfoString.append(ChatColor.GRAY + "Участники: " + ChatColor.GREEN + String.join(", ", (List<String>) regionInfo.get("members")) + "\n");
        regionInfoString.append(ChatColor.GRAY + "Координаты: " + ChatColor.GREEN +
                "Центральный блок: " + ChatColor.GRAY + "(" + ChatColor.GREEN + regionInfo.get("bx") + ", " + regionInfo.get("by") + ", " + regionInfo.get("bz") + ChatColor.GRAY + ") \n");
        regionInfoString.append(ChatColor.GRAY + "Границы: " + ChatColor.GRAY +
                "(" + ChatColor.GREEN + regionInfo.get("x1") + ", " + regionInfo.get("y1") + ", " + regionInfo.get("z1") + ChatColor.GRAY + "), " +
                "(" + ChatColor.GREEN + regionInfo.get("x2") + ", " + regionInfo.get("y2") + ", " + regionInfo.get("z2") + ChatColor.GRAY + ")");

        return regionInfoString.toString();
    }

    public boolean isPlayerMemberOfRegion(Player player, String regionName) {
        FileConfiguration config = plugin.getRegionsConfig();
        String playerName = player.getName();
        // Получаем список участников региона из конфига
        List<String> members = config.getStringList("regions." + regionName + ".members");

        // Проверяем, есть ли игрок в этом списке
        return members.contains(playerName);
    }

    public boolean isPlayerOwnerOfRegion(Player player, String regionName) {
        if (!regionName.equals(".")) {
            FileConfiguration config = plugin.getRegionsConfig();
            String playerName = player.getName();
            // Получаем владельца региона из конфига
            String path = "regions." + regionName;
            if (config.contains(path)) {
                String owner = config.getString(path + ".owner");

                // Проверяем, есть ли игрок в этом списке
                return owner.equals(playerName);
            }
        }
        return false;
    }

    public void addMember(String playerName, String regionName) {
        FileConfiguration config = plugin.getRegionsConfig();
        String path = "regions." + regionName;

        if (config.contains(path)) {
            List<String> members = config.getStringList(path + ".members");
            if (!members.contains(playerName)) {
                members.add(playerName);

                config.set(path + ".members", members);
            }
            plugin.saveRegionsConfig();
        }
    }


    public void removeMember(String playerName, String regionName) {
        FileConfiguration config = plugin.getRegionsConfig();
        String path = "regions." + regionName;

        if (config.contains(path)) {
            List<String> members = config.getStringList(path + ".members");
            if (members.contains(playerName)) {
                members.remove(playerName);

                config.set(path + ".members", members);
            }
            plugin.saveRegionsConfig();
        }
    }



    public String getRegionByBlock(Block block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        FileConfiguration config = plugin.getRegionsConfig();

        for (String regionName : getRegionList()) {
            String path = "regions." + regionName;

            int x1 = config.getInt(path + ".x1");
            int y1 = config.getInt(path + ".y1");
            int z1 = config.getInt(path + ".z1");
            int x2 = config.getInt(path + ".x2");
            int y2 = config.getInt(path + ".y2");
            int z2 = config.getInt(path + ".z2");

            // Проверяем, находится ли блок внутри региона
            if (x >= Math.min(x1, x2) && x <= Math.max(x1, x2) &&
                    y >= Math.min(y1, y2) && y <= Math.max(y1, y2) &&
                    z >= Math.min(z1, z2) && z <= Math.max(z1, z2)) {
                return regionName;
            }
        }

        return ""; // Если регион не найден
    }



    public String getRegionByLocation(Location location) {
        FileConfiguration config = plugin.getRegionsConfig();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        if (config.contains("regions")) { // Проверяем, существует ли секция "regions"
            for (String regionName : config.getConfigurationSection("regions").getKeys(false)) {
                int x1 = config.getInt("regions." + regionName + ".x1");
                int y1 = config.getInt("regions." + regionName + ".y1");
                int z1 = config.getInt("regions." + regionName + ".z1");

                int x2 = config.getInt("regions." + regionName + ".x2");
                int y2 = config.getInt("regions." + regionName + ".y2");
                int z2 = config.getInt("regions." + regionName + ".z2");

                // Проверяем, входят ли координаты в границы региона
                if (x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2) {
                    return regionName;
                }
            }
        }

        return ""; // Возвращаем null, если координаты не входят ни в один регион
    }

    public boolean isBlockPrivate(Block block, String regionName) { // Является ли блок блоком привата в регионе
        // Получаем координаты блока
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        FileConfiguration config = plugin.getRegionsConfig();
        String path = "regions." + regionName;

        int bx = config.getInt(path + ".bx");
        int by = config.getInt(path + ".by");
        int bz = config.getInt(path + ".bz");

        return x == bx && y == by && z == bz;
    }

    public boolean isBlockInRegion(Block block) {
        // Получаем координаты блока
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        World world = block.getWorld();

        FileConfiguration config = plugin.getRegionsConfig();
        for (String regionName : getRegionList()) {
            String path = "regions." + regionName;

            int x1 = config.getInt(path + ".x1");
            int y1 = config.getInt(path + ".y1");
            int z1 = config.getInt(path + ".z1");
            int x2 = config.getInt(path + ".x2");
            int y2 = config.getInt(path + ".y2");
            int z2 = config.getInt(path + ".z2");

            // Проверяем, находится ли блок в пределах региона
            if ((x >= Math.min(x1, x2) && x <= Math.max(x1, x2)) &&
                    (y >= Math.min(y1, y2) && y <= Math.max(y1, y2)) &&
                    (z >= Math.min(z1, z2) && z <= Math.max(z1, z2)) &&
                    world.getEnvironment() == World.Environment.NORMAL) {
                return true; // Блок найден в регионе
            }
        }
        return false; // Если блок не найден ни в одном из регионов
    }


    public boolean isLocationInRegion(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        World world = location.getWorld();

        FileConfiguration config = plugin.getRegionsConfig();
        ConfigurationSection regionsSection = config.getConfigurationSection("regions");

        // Проверяем, существует ли раздел regions
        if (regionsSection == null) {
            plugin.getLogger().warning("No regions section found in the configuration.");
            return false;
        }

        Set<String> regionNames = regionsSection.getKeys(false);

        for (String regionName : regionNames) {
            String path = "regions." + regionName;

            if (config.isConfigurationSection(path)) {
                int x1 = config.getInt(path + ".x1");
                int y1 = config.getInt(path + ".y1");
                int z1 = config.getInt(path + ".z1");
                int x2 = config.getInt(path + ".x2");
                int y2 = config.getInt(path + ".y2");
                int z2 = config.getInt(path + ".z2");

                // Проверяем, находятся ли координаты в пределах региона
                if ((x >= Math.min(x1, x2) && x <= Math.max(x1, x2)) &&
                        (y >= Math.min(y1, y2) && y <= Math.max(y1, y2)) &&
                        (z >= Math.min(z1, z2) && z <= Math.max(z1, z2)) &&
                        world.getEnvironment() == World.Environment.NORMAL) {
                    return true; // Если координаты попадают в регион
                }
            } else {
                plugin.getLogger().warning("No configuration section found for path: " + path);
            }
        }
        return false; // Если координаты не попадают ни в один регион
    }


    public boolean isRegionOverlapping(int[] newRegionCoords) {
        FileConfiguration config = plugin.getRegionsConfig();
        // Извлекаем координаты нового региона
        int newX1 = newRegionCoords[0];
        int newY1 = newRegionCoords[1];
        int newZ1 = newRegionCoords[2];
        int newX2 = newRegionCoords[3];
        int newY2 = newRegionCoords[4];
        int newZ2 = newRegionCoords[5];

        for (String regionName : getRegionList()) {
            // Извлекаем координаты существующего региона из конфига
            int existingX1 = config.getInt("regions." + regionName + ".x1");
            int existingY1 = config.getInt("regions." + regionName + ".y1");
            int existingZ1 = config.getInt("regions." + regionName + ".z1");
            int existingX2 = config.getInt("regions." + regionName + ".x2");
            int existingY2 = config.getInt("regions." + regionName + ".y2");
            int existingZ2 = config.getInt("regions." + regionName + ".z2");

            // Проверяем, пересекаются ли регионы
            boolean xOverlap = Math.max(newX1, newX2) >= Math.min(existingX1, existingX2) &&
                    Math.min(newX1, newX2) <= Math.max(existingX1, existingX2);
            boolean yOverlap = Math.max(newY1, newY2) >= Math.min(existingY1, existingY2) &&
                    Math.min(newY1, newY2) <= Math.max(existingY1, existingY2);
            boolean zOverlap = Math.max(newZ1, newZ2) >= Math.min(existingZ1, existingZ2) &&
                    Math.min(newZ1, newZ2) <= Math.max(existingZ1, existingZ2);

            if (xOverlap && yOverlap && zOverlap) {
                // Если есть пересечение по всем осям, то регионы пересекаются
                return true;
            }
        }
        // Если пересечения не найдено
        return false;
    }







    public FileConfiguration getConfig() {
        return config;
    }
}