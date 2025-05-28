package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * The main menu screen for the Tetris game.
 * Displays animated falling pieces and UI for selecting game modes.
 */
public class MenuScreen implements Screen {
    private final Tetris game;
    private Stage stage;
    private ShapeRenderer shapeRenderer;
    private List<FallingPiece> fallingPieces;

    public MenuScreen(final Tetris game) {
        this.game = game;
    }

    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        // take input from this screen
        Gdx.input.setInputProcessor(stage);

        // shape animations
        shapeRenderer = new ShapeRenderer();
        fallingPieces = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Tetrimino type = Tetrimino.values()[(int) (Math.random() * Tetrimino.values().length)];
            Vector2 position = new Vector2((float) Math.random() * Gdx.graphics.getWidth(),
                (float) Math.random() * Gdx.graphics.getHeight());
            Vector2 velocity = new Vector2(0, -50); // speed
            fallingPieces.add(new FallingPiece(type, position, velocity));
        }

        // main title
        VisLabel menuLabel = new VisLabel("Tetris");
        menuLabel.setFontScale(3f);
        menuLabel.getColor().a = 0;
        menuLabel.addAction(Actions.fadeIn(1f));

        // button to play classic mode
        VisTextButton classicPlayButton = new VisTextButton("Play Classic");
        classicPlayButton.getLabel().setFontScale(2f);
        classicPlayButton.addListener(event -> {
            if (classicPlayButton.isPressed()) {
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        // button to play sprint mode
        VisTextButton sprintPlayButton = new VisTextButton("Play Sprint");
        sprintPlayButton.getLabel().setFontScale(2f);
        sprintPlayButton.addListener(event -> {
            if (sprintPlayButton.isPressed()) {
                game.setScreen(new SprintScreen(game));
                return true;
            }
            return false;
        });

        // button to play arcade mode
        VisTextButton arcadePlayButton = new VisTextButton("Play Arcade");
        arcadePlayButton.getLabel().setFontScale(2f);
        arcadePlayButton.addListener(event -> {
            if (arcadePlayButton.isPressed()) {
                game.setScreen(new ArcadeScreen(game));
                return true;
            }
            return false;
        });

        // button to open settings menu
        VisTextButton settingsButton = new VisTextButton("Settings");
        settingsButton.getLabel().setFontScale(2f);
        settingsButton.addListener(event -> {
            if (settingsButton.isPressed()) {
                game.setScreen(new SettingsScreen(game));
                return true;
            }
            return false;
        });

        // button to quit the game
        VisTextButton quitButton = new VisTextButton("Quit");
        quitButton.getLabel().setFontScale(2f);
        quitButton.addListener(event -> {
            if (quitButton.isPressed()) {
                Gdx.app.exit(); // quit the app
                return true;
            }
            return false;
        });

        classicPlayButton.getColor().a = 0;
        sprintPlayButton.getColor().a = 0;
        arcadePlayButton.getColor().a = 0;
        settingsButton.getColor().a = 0;
        quitButton.getColor().a = 0;

        classicPlayButton.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(1f)));
        sprintPlayButton.addAction(Actions.sequence(Actions.delay(1f), Actions.fadeIn(1f)));
        arcadePlayButton.addAction(Actions.sequence(Actions.delay(1.5f), Actions.fadeIn(1f)));
        settingsButton.addAction(Actions.sequence(Actions.delay(2f), Actions.fadeIn(1f)));
        quitButton.addAction(Actions.sequence(Actions.delay(2.5f), Actions.fadeIn(1f)));

        // create the main table
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // add each element to the table, pad each button
        table.add(menuLabel).padBottom(80f).row();
        table.add(classicPlayButton).width(360).height(90).padBottom(30f).row();
        table.add(sprintPlayButton).width(360).height(90).padBottom(30f).row();
        table.add(arcadePlayButton).width(360).height(90).padBottom(30f).row();
        table.add(settingsButton).width(360).height(90).padBottom(30f).row();
        table.add(quitButton).width(360).height(90).padBottom(60f).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // clear screen and render the main stage
        ScreenUtils.clear(Color.BLACK);

        // render falling pieces
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (FallingPiece piece : fallingPieces) {
            piece.update(delta);
            piece.render(shapeRenderer);
        }
        shapeRenderer.end();

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
        // is this needed
        if (shapeRenderer != null) shapeRenderer.dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
