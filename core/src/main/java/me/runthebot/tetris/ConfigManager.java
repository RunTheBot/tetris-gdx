package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton for loading, saving, and providing access to game configuration settings.
 * Handles persistence of user preferences.
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "config.txt";
    private static ConfigManager instance;
    private GameConfig config;

    private ConfigManager() {
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public GameConfig getConfig() {
        return config;
    }

    /**
     * Saves the current config to a file
     */
    public void saveConfig() {
        FileHandle file = Gdx.files.local(CONFIG_FILE);
        StringBuilder data = new StringBuilder();

        data.append("DAS_DAY=").append(config.DAS_DELAY).append("\n");
        data.append("ARR_DELAY=").append(config.ARR_DELAY).append("\n");
        data.append("showGhostPiece=").append(config.showGhostPiece).append("\n");

        file.writeString(data.toString(), false);
    }

    /**
     * Loads the config from the file
     */
    private void loadConfig() {
        FileHandle file = Gdx.files.local(CONFIG_FILE);
        config = new GameConfig();

        if (file.exists()) {
            Map<String, String> values = new HashMap<>();
            String[] lines = file.readString().split("\n");

            for (String line : lines) {
                String[] keyValue = line.split("=");
                if (keyValue.length == 2) {
                    values.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            config.DAS_DELAY = Float.parseFloat(values.getOrDefault("DAS_DELAY", "170"));
            config.ARR_DELAY = Float.parseFloat(values.getOrDefault("ARR_DELAY", "30"));
            config.showGhostPiece = Boolean.parseBoolean(values.getOrDefault("showGhostPiece", "true"));
        } else {
            saveConfig();
        }
    }
}
