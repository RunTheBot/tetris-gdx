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
    // Path to the configuration file
    private static final String CONFIG_FILE = "config.txt";
    // Instance of ConfigManager
    private static ConfigManager instance;
    // Game configuration object
    private GameConfig config;

    /**
     * Loads the configuration when a ConfigManager is created
     */
    private ConfigManager() {
        loadConfig();
    }

    /**
     * Returns the current instance of the config manager
     * @return
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Returns the current game configuration
     * @return
     */
    public GameConfig getConfig() {
        return config;
    }

    /**
     * Saves the current config to a file
     */
    public void saveConfig() {
        FileHandle file = Gdx.files.local(CONFIG_FILE);
        StringBuilder data = new StringBuilder();

        data.append("DAS_DELAY=").append(config.DAS_DELAY).append("\n");
        data.append("ARR_DELAY=").append(config.ARR_DELAY).append("\n");
        data.append("showGhostPiece=").append(config.showGhostPiece).append("\n");

        // Save key bindings
        data.append("KEY_MOVE_LEFT=").append(config.KEY_MOVE_LEFT).append("\n");
        data.append("KEY_MOVE_RIGHT=").append(config.KEY_MOVE_RIGHT).append("\n");
        data.append("KEY_MOVE_DOWN=").append(config.KEY_MOVE_DOWN).append("\n");
        data.append("KEY_ROTATE_CW=").append(config.KEY_ROTATE_CW).append("\n");
        data.append("KEY_ROTATE_CCW=").append(config.KEY_ROTATE_CCW).append("\n");
        data.append("KEY_ROTATE_180=").append(config.KEY_ROTATE_180).append("\n");
        data.append("KEY_HARD_DROP=").append(config.KEY_HARD_DROP).append("\n");
        data.append("KEY_HOLD=").append(config.KEY_HOLD).append("\n");
        data.append("KEY_HOLD_ALT=").append(config.KEY_HOLD_ALT).append("\n");

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

            // Load key bindings with defaults if not present
            config.KEY_MOVE_LEFT = Integer.parseInt(values.getOrDefault("KEY_MOVE_LEFT", String.valueOf(config.KEY_MOVE_LEFT)));
            config.KEY_MOVE_RIGHT = Integer.parseInt(values.getOrDefault("KEY_MOVE_RIGHT", String.valueOf(config.KEY_MOVE_RIGHT)));
            config.KEY_MOVE_DOWN = Integer.parseInt(values.getOrDefault("KEY_MOVE_DOWN", String.valueOf(config.KEY_MOVE_DOWN)));
            config.KEY_ROTATE_CW = Integer.parseInt(values.getOrDefault("KEY_ROTATE_CW", String.valueOf(config.KEY_ROTATE_CW)));
            config.KEY_ROTATE_CCW = Integer.parseInt(values.getOrDefault("KEY_ROTATE_CCW", String.valueOf(config.KEY_ROTATE_CCW)));
            config.KEY_ROTATE_180 = Integer.parseInt(values.getOrDefault("KEY_ROTATE_180", String.valueOf(config.KEY_ROTATE_180)));
            config.KEY_HARD_DROP = Integer.parseInt(values.getOrDefault("KEY_HARD_DROP", String.valueOf(config.KEY_HARD_DROP)));
            config.KEY_HOLD = Integer.parseInt(values.getOrDefault("KEY_HOLD", String.valueOf(config.KEY_HOLD)));
            config.KEY_HOLD_ALT = Integer.parseInt(values.getOrDefault("KEY_HOLD_ALT", String.valueOf(config.KEY_HOLD_ALT)));
        } else {
            saveConfig();
        }
    }
}
