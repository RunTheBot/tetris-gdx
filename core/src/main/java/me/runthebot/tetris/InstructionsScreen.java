package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Screen displaying game instructions and controls.
 */
public class InstructionsScreen implements Screen {
    private final Tetris game;
    private final ConfigManager configManager;
    private final GameConfig config;
    private Stage stage;

    public InstructionsScreen(final Tetris game) {
        this.game = game;
        this.configManager = ConfigManager.getInstance();
        this.config = configManager.getConfig();
    }

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);

        // Title
        VisLabel titleLabel = new VisLabel("How to Play");
        titleLabel.setFontScale(2.2f);

        // Scroll hint
        VisLabel scrollHintLabel = new VisLabel("(Scroll down to see more)");
        scrollHintLabel.setColor(Color.LIGHT_GRAY);

        // Create content for instructions
        Table contentTable = createInstructionsContent();

        // Create scrollable area for instructions
        scrollPane = new ScrollPane(contentTable); // Initialize the class variable
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false); // Allow both horizontal and vertical scrolling
        scrollPane.setOverscroll(false, true);
        scrollPane.setScrollbarsVisible(true);
        scrollPane.setScrollbarsOnTop(false);
        scrollPane.setFlickScroll(true); // Enable flick scrolling for touch/mouse drag
        scrollPane.setSmoothScrolling(true);

        // Button to jump to Power-ups section
        VisTextButton powerupsButton = new VisTextButton("Power-ups & Debuffs");
        powerupsButton.addListener(event -> {
            if (powerupsButton.isPressed()) {
                // Approximate scroll position for power-ups section based on content
                scrollPane.setScrollY(1200); // Adjusted value for better targeting
                return true;
            }
            return false;
        });

        // Back button
        VisTextButton backButton = new VisTextButton("Back");
        backButton.addListener(event -> {
            if (backButton.isPressed()) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
            return false;
        });

        // Create button table
        Table buttonTable = new Table();
        buttonTable.add(powerupsButton).width(200).height(40).padRight(30);
        buttonTable.add(backButton).width(180).height(40);

        // Main table
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top();

        // Make sure the title is positioned correctly
        mainTable.add(titleLabel).padTop(20f).padBottom(5f).row();
        mainTable.add(scrollHintLabel).padBottom(10f).row();

        // Set a fixed size for the scroll pane and allow it to scroll
        mainTable.add(scrollPane)
            .width(Gdx.graphics.getWidth() * 0.9f)
            .height(Gdx.graphics.getHeight() * 0.8f)
            .expand() // This helps with the scroll behavior
            .fill()   // Fill available space
            .row();

        mainTable.add(buttonTable).padTop(10f).padBottom(10f);

        stage.addActor(mainTable);
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Creates the instructions content
     * @return
     */
    private Table createInstructionsContent() {
        Table content = new Table();
        content.defaults().pad(7).align(Align.left).expandX(); // Add expandX to fill width

        // Game objective section
        VisLabel objectiveTitle = new VisLabel("Game Objective");
        objectiveTitle.setFontScale(1.5f);

        VisLabel objectiveText = new VisLabel(
            "Tetris is a puzzle game where you must arrange falling tetromino pieces to create complete horizontal lines. " +
            "When a line is completed, it clears from the playing field, giving you more space and points. " +
            "The game ends when the pieces stack up to the top of the playing field."
        );
        objectiveText.setWrap(true);

        // Controls section
        VisLabel controlsTitle = new VisLabel("Controls");
        controlsTitle.setFontScale(1.5f);

        Table controlsTable = new Table();
        controlsTable.defaults().pad(5);

        addControlRow(controlsTable, "Move Left", Input.Keys.toString(config.KEY_MOVE_LEFT));
        addControlRow(controlsTable, "Move Right", Input.Keys.toString(config.KEY_MOVE_RIGHT));
        addControlRow(controlsTable, "Soft Drop", Input.Keys.toString(config.KEY_MOVE_DOWN));
        addControlRow(controlsTable, "Hard Drop", Input.Keys.toString(config.KEY_HARD_DROP));
        addControlRow(controlsTable, "Rotate Clockwise", Input.Keys.toString(config.KEY_ROTATE_CW));
        addControlRow(controlsTable, "Rotate Counter-Clockwise", Input.Keys.toString(config.KEY_ROTATE_CCW));
        addControlRow(controlsTable, "Rotate 180°", Input.Keys.toString(config.KEY_ROTATE_180));
        addControlRow(controlsTable, "Hold Piece", Input.Keys.toString(config.KEY_HOLD) + " or " + Input.Keys.toString(config.KEY_HOLD_ALT));

        // Game modes section
        VisLabel modesTitle = new VisLabel("Game Modes");
        modesTitle.setFontScale(1.5f);

        VisLabel marathonLabel = new VisLabel("Marathon");
        marathonLabel.setFontScale(1.2f);

        VisLabel marathonText = new VisLabel(
            "Classic endless Tetris. Play until you top out. " +
            "As you level up, the pieces fall faster, increasing the difficulty."
        );
        marathonText.setWrap(true);

        VisLabel sprintLabel = new VisLabel("Sprint");
        sprintLabel.setFontScale(1.2f);

        VisLabel sprintText = new VisLabel(
            "Race against the clock to clear 40 lines as quickly as possible. " +
            "Your final time is your score."
        );
        sprintText.setWrap(true);

        VisLabel arcadeLabel = new VisLabel("Arcade");
        arcadeLabel.setFontScale(1.2f);

        VisLabel arcadeText = new VisLabel(
            "Marathon mode with power-ups and debuffs! Clear lines to earn special items that can help or hinder your gameplay. " +
            "Power-ups include line clears, slowing down pieces, or getting only favorable pieces. " +
            "But beware of debuffs that speed up falling pieces, lock random blocks in place, or scramble your controls. " +
            "Try to survive as long as possible while dealing with these random events!"
        );
        arcadeText.setWrap(true);        // Power-ups and Debuffs section
        VisLabel powerupsTitle = new VisLabel("Power-ups & Debuffs");
        powerupsTitle.setFontScale(1.5f);
        powerupsTitle.setColor(Color.YELLOW);

        VisLabel powerupsHeaderLabel = new VisLabel("POWER-UPS:");
        powerupsHeaderLabel.setFontScale(1.1f);
        powerupsHeaderLabel.setColor(Color.GREEN);

        VisLabel powerupsList = new VisLabel(
            "• Line Clear: Removes bottom rows of blocks\n" +
            "• Slow Fall: Temporarily slows down falling pieces\n" +
            "• Favorable Pieces: Temporarily gives only useful pieces\n" +
            "• Extra Points: Multiplies your score for a limited time"
        );
        powerupsList.setWrap(true);

        VisLabel debuffsHeaderLabel = new VisLabel("DEBUFFS:");
        debuffsHeaderLabel.setFontScale(1.1f);
        debuffsHeaderLabel.setColor(Color.RED);

        VisLabel debuffsList = new VisLabel(
            "• Speed Up: Temporarily increases falling speed\n" +
            "• Random Blocks: Places random blocks on your field\n" +
            "• Control Scramble: Temporarily changes your controls\n" +
            "• Restricted Rotation: Limits piece rotation temporarily"
        );
        debuffsList.setWrap(true);

        // Scoring section
        VisLabel scoringTitle = new VisLabel("Scoring");
        scoringTitle.setFontScale(1.5f);

        VisLabel scoringText = new VisLabel(
            "1 Line: 100 points × level\n" +
            "2 Lines: 300 points × level\n" +
            "3 Lines: 500 points × level\n" +
            "4 Lines (Tetris): 800 points × level\n\n" +
            "Soft drop: +1 point per cell dropped\n" +
            "Hard drop: +2 points per cell dropped"
        );

        // Tips section
        VisLabel tipsTitle = new VisLabel("Tips");
        tipsTitle.setFontScale(1.5f);

        VisLabel tipsText = new VisLabel(
            "• Plan ahead: Use the preview pieces to plan your next moves\n" +
            "• Use hold strategically: Save important pieces for when you need them\n" +
            "• Build flat: Try to keep your stack as flat as possible\n" +
            "• Create a well: Leave a column empty for a potential Tetris (4 lines)\n" +
            "• Practice T-spins: Advanced technique for bonus points\n" +
            "• In Arcade mode: Be ready to adapt quickly when power-ups or debuffs appear\n" +
            "• Customize controls: Change key bindings in the settings menu"
        );
        tipsText.setWrap(true);

        // Add all sections to content table
        content.add(objectiveTitle).row();
        content.add(objectiveText).width(Gdx.graphics.getWidth() * 0.75f).row();

        content.add(controlsTitle).padTop(15).row();
        content.add(controlsTable).row();

        content.add(modesTitle).padTop(15).row();
        content.add(marathonLabel).row();
        content.add(marathonText).width(Gdx.graphics.getWidth() * 0.75f).row();
        content.add(sprintLabel).padTop(7).row();
        content.add(sprintText).width(Gdx.graphics.getWidth() * 0.75f).row();
        content.add(arcadeLabel).padTop(7).row();
        content.add(arcadeText).width(Gdx.graphics.getWidth() * 0.75f).row();
        content.add(powerupsTitle).padTop(15).row();
        content.add(powerupsHeaderLabel).padTop(3).row();
        content.add(powerupsList).width(Gdx.graphics.getWidth() * 0.75f).row();
        content.add(debuffsHeaderLabel).padTop(7).row();
        content.add(debuffsList).width(Gdx.graphics.getWidth() * 0.75f).row();

        content.add(scoringTitle).padTop(15).row();
        content.add(scoringText).row();

        content.add(tipsTitle).padTop(15).row();
        content.add(tipsText).width(Gdx.graphics.getWidth() * 0.75f).row();

        return content;
    }

    /**
     * Adds the control row to the main table.
     * @param table
     * @param action
     * @param key
     */
    private void addControlRow(Table table, String action, String key) {
        VisLabel actionLabel = new VisLabel(action);
        VisLabel keyLabel = new VisLabel(key);

        table.add(actionLabel).width(250).left();
        table.add(keyLabel).width(100).left().row();
    }

    private ScrollPane scrollPane;
    private float maxScrollY = 0;

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // Handle mouse wheel scrolling
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            scrollPane.setScrollY(scrollPane.getScrollY() - 15);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            scrollPane.setScrollY(scrollPane.getScrollY() + 15);
        }

        // Record max scroll position for use with the buttons
        if (scrollPane.getScrollY() > maxScrollY) {
            maxScrollY = scrollPane.getScrollY();
        }

        stage.act(delta);
        stage.draw();
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
        if (stage != null) stage.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
