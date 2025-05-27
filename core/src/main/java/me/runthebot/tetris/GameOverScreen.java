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

public class GameOverScreen implements Screen {
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

    public GameOverScreen(final Tetris game, String gameType) {
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
    }

    /**
     * Constructor with game stats
     */
    public GameOverScreen(final Tetris game, String gameType, int score, int level, int linesCleared,
                         long time, float currentSpeed, float maxSpeed, int linesLeft) {
        this.game = game;
        this.gameType = gameType;
        this.score = score;
        this.level = level;
        this.linesCleared = linesCleared;
        this.time = time;
        this.currentSpeed = currentSpeed;
        this.maxSpeed = maxSpeed;
        this.linesLeft = linesLeft;
    }

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        // take user input
        Gdx.input.setInputProcessor(stage);

        // game over title
        VisLabel menuLabel = new VisLabel("Game Over");
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
            statsTable.add(new VisLabel("SPRINT MODE STATS")).colspan(2).center().padBottom(10).row();
            statsTable.add(new VisLabel("Time:")).padRight(10);
            statsTable.add(new VisLabel(timeString)).row();
            statsTable.add(new VisLabel("Lines Cleared:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(linesCleared))).row();
            statsTable.add(new VisLabel("Lines Left:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(linesLeft))).row();
            statsTable.add(new VisLabel("Pace:")).padRight(10);
            if (time > 0) {
                float pace = (float) linesCleared / (time / 60000.0f);
                statsTable.add(new VisLabel(String.format("%.2f lpm", pace))).row();
            } else {
                statsTable.add(new VisLabel("0.00 lpm")).row();
            }
        } else if (gameType.equals("classic")) {
            statsTable.add(new VisLabel("MARATHON MODE STATS")).colspan(2).center().padBottom(10).row();
            statsTable.add(new VisLabel("Score:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(score))).row();
            statsTable.add(new VisLabel("Level:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(level))).row();
            statsTable.add(new VisLabel("Lines Cleared:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(linesCleared))).row();
            statsTable.add(new VisLabel("Time:")).padRight(10);
            statsTable.add(new VisLabel(timeString)).row();
        } else if (gameType.equals("arcade")) {
            statsTable.add(new VisLabel("ARCADE MODE STATS")).colspan(2).center().padBottom(10).row();
            statsTable.add(new VisLabel("Score:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(score))).row();
            statsTable.add(new VisLabel("Level:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(level))).row();
            statsTable.add(new VisLabel("Lines Cleared:")).padRight(10);
            statsTable.add(new VisLabel(String.valueOf(linesCleared))).row();
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
