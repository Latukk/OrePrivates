package org.latuk.oreprivates;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.latuk.oreprivates.command.SimpleCommandHandler;
import org.latuk.oreprivates.handler.SimpleEventHandler;

import java.io.File;
import java.io.IOException;

public final class OrePrivates extends JavaPlugin implements Listener {

    private Utils utils;
    private File regionsFile;
    private FileConfiguration regionsConfig;

    @Override
    public void onEnable() {
        utils = new Utils(this); // Передаем ссылку на плагин в Utils
        getServer().getPluginManager().registerEvents(new SimpleEventHandler(this, utils), this);
        getCommand("rg").setExecutor(new SimpleCommandHandler(this, utils));
        getCommand("rg").setTabCompleter(new Completer(this, utils));

        // Загружаем конфиг
        saveDefaultConfig();

        // Инициализация кастомного файла
        regionsFile = new File(getDataFolder(), "regions.yml");
        if (!regionsFile.exists()) {
            saveResource("regions.yml", false);
        }
        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);

    }

    // Получение кастомной конфигурации
    public FileConfiguration getRegionsConfig() {
        if (regionsConfig == null) {
            setupRegionsConfig();
        }
        return regionsConfig;
    }

    public void setupRegionsConfig() {
        regionsFile = new File(getDataFolder(), "regions.yml");

        if (!regionsFile.exists()) {
            saveResource("regions.yml", false); // Копируем regions.yml из ресурсов, если он не существует
        }

        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
    }

    public void saveRegionsConfig() {
        try {
            regionsConfig.save(regionsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}