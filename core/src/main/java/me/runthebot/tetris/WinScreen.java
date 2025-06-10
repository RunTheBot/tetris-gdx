package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

/**
 * Screen displayed when the player wins a game mode.
 * Shows final stats and allows returning to the menu or starting a new game.
 */
public class WinScreen implements Screen {
    private final Tetris game;
    private Stage stage;
    private String gameType;

    // Game stats to display
    private int score;
    private int level;
    private int linesCleared;
    private long time;
    private float currentSpeed;
    private float maxSpeed;
    private int linesLeft;
    private int highScore;

    /**
     * WinScreen class.
     * @param game
     * @param gameType
     */
    public WinScreen(final Tetris game, String gameType) {
        this.game = game;
        this.gameType = gameType;

        // In a real implementation, these would be passed from the game screen
        // This is just a placeholder until we implement a proper way to pass the stats
        this.score = 0;
        this.level = 1;
        this.linesCleared = 0;
        this.time = 0;
        this.currentSpeed = 0;
        this.maxSpeed = 0;
        this.linesLeft = 0;
        this.highScore = 0;
    }

    /**
     * Renders the WinScreen.
     * @param game
     * @param gameType
     * @param score
     * @param level
     * @param linesCleared
     * @param time
     * @param currentSpeed
     * @param maxSpeed
     * @param highScore
     */
    public WinScreen(final Tetris game, String gameType, int score, int level, int linesCleared,
                    long time, float currentSpeed, float maxSpeed, int highScore) {
        this.game = game;
        this.gameType = gameType;
        this.score = score;
        this.level = level;
        this.linesCleared = linesCleared;
        this.time = time;
        this.currentSpeed = currentSpeed;
        this.maxSpeed = maxSpeed;
        this.highScore = highScore;
        this.linesLeft = 0; // Always 0 in win screen
    }

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        // take user input
        Gdx.input.setInputProcessor(stage);

        // Win title
        VisLabel menuLabel = new VisLabel("You Won!");
        menuLabel.setFontScale(3f);

        // Game stats table
        Table statsTable = new Table();
        statsTable.defaults().pad(5).left();

        // Format time as mm:ss.ms
        String timeString = String.format("%02d:%02d.%d",
                (time / 60000),
                (time / 1000) % 60,
                (time / 100) % 10);

        // Add stats based on game type
        if (gameType.equals("sprint")) {
            // Celebrate with congratulatory message
            VisLabel congratsLabel = new VisLabel("Congratulations! You completed all " + linesCleared + " lines!");
            congratsLabel.setColor(Color.YELLOW);

            statsTable.add(congratsLabel).colspan(2).center().padBottom(15).row();
            statsTable.add(new VisLabel("SPRINT MODE STATS")).colspan(2).center().padBottom(10).row();
            statsTable.add(new VisLabel("Final Time:")).padRight(10);
            statsTable.add(new VisLabel(timeString)).row();

            // Add new record indication if applicable
            if (highScore > 0 && time <= highScore) {
                VisLabel newRecordLabel = new VisLabel("NEW RECORD!");
                newRecordLabel.setColor(Color.GREEN);
                statsTable.add(newRecordLabel).colspan(2).center().padTop(10).row();
            } else if (highScore > 0) {
                String recordTime = String.format("%02d:%02d.%d",
                    (highScore / 60000),
                    (highScore / 1000) % 60,
                    (highScore / 100) % 10);
                statsTable.add(new VisLabel("Record Time:")).padRight(10);
                statsTable.add(new VisLabel(recordTime)).row();
            }

            // Show pace
            if (time > 0) {
                float pace = (float) linesCleared / (time / 60000.0f);
                statsTable.add(new VisLabel("Pace:")).padRight(10);
                statsTable.add(new VisLabel(String.format("%.2f lpm", pace))).row();
            }
        } else if (gameType.equals("classic") || gameType.equals("arcade")) {
            String modeTitle = gameType.equals("classic") ? "MARATHON MODE STATS" : "ARCADE MODE STATS";

            // Celebrate with congratulatory message
            VisLabel congratsLabel = new VisLabel("Congratulations! You reached level " + level + "!");
            congratsLabel.setColor(Color.YELLOW);

            statsTable.add(congratsLabel).colspan(2).center().padBottom(15).row();
            statsTable.add(new VisLabel(modeTitle)).colspan(2).center().padBottom(10).row();
            statsTable.add(new VisLabel("Final Score:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(score))).row();

            // Add new high score indication if applicable
            if (highScore > 0 && score >= highScore) {
                VisLabel newRecordLabel = new VisLabel("NEW HIGH SCORE!");
                newRecordLabel.setColor(Color.GREEN);
                statsTable.add(newRecordLabel).colspan(2).center().padTop(10).row();
            } else if (highScore > 0) {
                statsTable.add(new VisLabel("High Score:")).padRight(10);
                statsTable.add(new VisLabel(String.valueOf(highScore))).row();
            }

            statsTable.add(new VisLabel("Level Reached:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(level))).row();
            statsTable.add(new VisLabel("Lines Cleared:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(linesCleared))).row();
            statsTable.add(new VisLabel("Time Played:")).padRight(10);
            statsTable.add(new VisLabel(timeString)).row();
        }

        // Add common stats
        statsTable.add(new VisLabel("Current Speed:")).padRight(10);
        statsTable.add(new VisLabel(String.format("%.2f lps", currentSpeed))).row();
        statsTable.add(new VisLabel("Max Speed:")).padRight(10);
        statsTable.add(new VisLabel(String.format("%.2f lps", maxSpeed))).row();

        // Buttons
        VisTextButton playAgainButton = new VisTextButton("Play Again");
        playAgainButton.addListener(event -> {
            if (playAgainButton.isPressed()) {
                if (gameType.equals("classic")) {
                    game.setScreen(new GameScreen(game));
                } else if (gameType.equals("sprint")) {
                    game.setScreen(new SprintScreen(game));
                } else if (gameType.equals("arcade")) {
                    game.setScreen(new ArcadeScreen(game));
                }
                return true;
            }
            return false;
        });

        VisTextButton backButton = new VisTextButton("Back to Menu");
        backButton.addListener(event -> {
            if (backButton.isPressed()) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
            return false;
        });

        // create main table
        Table table = new Table();
        table.setFillParent(true);
        table.add(menuLabel).padBottom(30f).row();
        table.add(statsTable).padBottom(30f).row();
        table.add(playAgainButton).width(180).height(60).padBottom(20f).row();
        table.add(backButton).width(180).height(60);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
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
