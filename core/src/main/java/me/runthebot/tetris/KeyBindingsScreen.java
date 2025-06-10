package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.*;

/**
 * Screen for customizing keyboard controls.
 * Allows rebinding of all game actions.
 */
public class KeyBindingsScreen implements Screen {
    private final Tetris game;
    private final ConfigManager configManager;
    private final GameConfig config;
    private Stage stage;

    // UI elements
    private VisTextButton[] keyButtons;
    private int currentRebindIndex = -1;

    // Key names for display
    private final String[] actionNames = {
        "Move Left", "Move Right", "Soft Drop", "Rotate Clockwise",
        "Rotate Counter-Clockwise", "Rotate 180°", "Hard Drop", "Hold Piece", "Hold Piece (Alt)"
    };

    /**
     * Constructor for the KeyBindingsScreen.
     * @param game The main game class.
     */
    public KeyBindingsScreen(final Tetris game) {
        this.game = game;
        this.configManager = ConfigManager.getInstance();
        this.config = configManager.getConfig();
    }

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);

        // Title label
        VisLabel titleLabel = new VisLabel("Key Bindings");
        titleLabel.setFontScale(2.2f);

        // Initialize button array to hold key binding buttons
        keyButtons = new VisTextButton[actionNames.length];

        // Create a table for each key binding row
        Table keysTable = new Table();
        keysTable.defaults().pad(10).left();

        // Create rows for each action with its current key binding
        setupKeyBindingRows(keysTable);

        // Reset to defaults button
        VisTextButton resetButton = new VisTextButton("Reset to Defaults");
        resetButton.addListener(event -> {
            if (resetButton.isPressed()) {
                resetToDefaults();
                return true;
            }
            return false;
        });

        // Back button
        VisTextButton backButton = new VisTextButton("Back");
        backButton.addListener(event -> {
            if (backButton.isPressed()) {
                configManager.saveConfig();
                game.setScreen(new SettingsScreen(game));
                return true;
            }
            return false;
        });

        // Create button table
        Table buttonTable = new Table();
        buttonTable.add(resetButton).width(180).height(50).padRight(30);
        buttonTable.add(backButton).width(180).height(50);

        // Main table
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        mainTable.add(titleLabel).padBottom(40f).row();
        mainTable.add(keysTable).padBottom(30f).row();
        mainTable.add(buttonTable).padTop(20f);

        stage.addActor(mainTable);

        // Set up the input processor with the stage
        setupInputProcessor();
    }

    private void setupKeyBindingRows(Table keysTable) {
        int[] keyValues = {
            config.KEY_MOVE_LEFT,
            config.KEY_MOVE_RIGHT,
            config.KEY_MOVE_DOWN,
            config.KEY_ROTATE_CW,
            config.KEY_ROTATE_CCW,
            config.KEY_ROTATE_180,
            config.KEY_HARD_DROP,
            config.KEY_HOLD,
            config.KEY_HOLD_ALT
        };

        for (int i = 0; i < actionNames.length; i++) {
            final int index = i;

            // Action name label
            VisLabel actionLabel = new VisLabel(actionNames[i]);
            actionLabel.setAlignment(1); // Right-aligned

            // Current key button
            keyButtons[i] = new VisTextButton(Input.Keys.toString(keyValues[i]));
            keyButtons[i].addListener(event -> {
                if (keyButtons[index].isPressed()) {
                    startRebinding(index);
                    return true;
                }
                return false;
            });

            // Add to table
            keysTable.add(actionLabel).width(200).right();
            keysTable.add(keyButtons[i]).width(120).left().row();
        }
    }

    private void setupInputProcessor() {
        // We'll use an InputAdapter that only handles keyboard inputs for rebinding
        InputAdapter keyBindingProcessor = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (currentRebindIndex >= 0) {
                    // Don't allow ESC to be bound as it's used to cancel
                    if (keycode != Input.Keys.ESCAPE) {
                        saveNewKeyBinding(currentRebindIndex, keycode);
                        System.out.println("Key bound: " + Input.Keys.toString(keycode));
                    }

                    stopRebinding();
                    return true;
                }
                return false;
            }

            // Don't capture any mouse events
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }
        };

        // Use a multiplexer to combine the stage's input processor with our key binding processor
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(stage); // Stage first to handle UI interactions

        // Only add the key binding processor if we're in rebinding mode
        if (currentRebindIndex >= 0) {
            multiplexer.addProcessor(keyBindingProcessor);
        }

        Gdx.input.setInputProcessor(multiplexer);
    }

    private void startRebinding(int index) {
        currentRebindIndex = index;
        keyButtons[index].setText("Press any key...");
        keyButtons[index].setColor(Color.YELLOW);

        // Change input processor to catch the next key press
        setupInputProcessor();
    }

    private void stopRebinding() {
        if (currentRebindIndex >= 0) {
            keyButtons[currentRebindIndex].setColor(Color.WHITE);
            currentRebindIndex = -1;

            // Reset input processor to just the stage
            setupInputProcessor();
        }
    }

    private void saveNewKeyBinding(int index, int keycode) {
        // Update the config object with the new key
        switch (index) {
            case 0: config.KEY_MOVE_LEFT = keycode; break;
            case 1: config.KEY_MOVE_RIGHT = keycode; break;
            case 2: config.KEY_MOVE_DOWN = keycode; break;
            case 3: config.KEY_ROTATE_CW = keycode; break;
            case 4: config.KEY_ROTATE_CCW = keycode; break;
            case 5: config.KEY_ROTATE_180 = keycode; break;
            case 6: config.KEY_HARD_DROP = keycode; break;
            case 7: config.KEY_HOLD = keycode; break;
            case 8: config.KEY_HOLD_ALT = keycode; break;
        }

        // Update the button text
        keyButtons[index].setText(Input.Keys.toString(keycode));

        // Save the config
        configManager.saveConfig();
    }

    private void resetToDefaults() {
        // Reset to default values
        GameConfig defaultConfig = new GameConfig();

        // Copy default values
        config.KEY_MOVE_LEFT = defaultConfig.KEY_MOVE_LEFT;
        config.KEY_MOVE_RIGHT = defaultConfig.KEY_MOVE_RIGHT;
        config.KEY_MOVE_DOWN = defaultConfig.KEY_MOVE_DOWN;
        config.KEY_ROTATE_CW = defaultConfig.KEY_ROTATE_CW;
        config.KEY_ROTATE_CCW = defaultConfig.KEY_ROTATE_CCW;
        config.KEY_ROTATE_180 = defaultConfig.KEY_ROTATE_180;
        config.KEY_HARD_DROP = defaultConfig.KEY_HARD_DROP;
        config.KEY_HOLD = defaultConfig.KEY_HOLD;
        config.KEY_HOLD_ALT = defaultConfig.KEY_HOLD_ALT;

        // Update UI
        updateButtonLabels();

        // Save changes
        configManager.saveConfig();
    }

    private void updateButtonLabels() {
        int[] keyValues = {
            config.KEY_MOVE_LEFT,
            config.KEY_MOVE_RIGHT,
            config.KEY_MOVE_DOWN,
            config.KEY_ROTATE_CW,
            config.KEY_ROTATE_CCW,
            config.KEY_ROTATE_180,
            config.KEY_HARD_DROP,
            config.KEY_HOLD,
            config.KEY_HOLD_ALT
        };

        for (int i = 0; i < keyButtons.length; i++) {
            keyButtons[i].setText(Input.Keys.toString(keyValues[i]));
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();

        // If escape is pressed while rebinding, cancel the rebinding
        if (currentRebindIndex >= 0 && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            stopRebinding();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        configManager.saveConfig();
        if (stage != null) stage.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
