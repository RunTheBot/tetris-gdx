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
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    /**
     * Constructor for the MenuScreen.
     * @param game The main Tetris game instance.
     */
    public MenuScreen(final Tetris game) {
        this.game = game;
    }

    // creates styled buttons for the main menu
    /**
     * Creates a styled VisTextButton with the given text and action.
     * @param text The text to display on the button.
     * @param action The Runnable action to execute when the button is pressed.
     * @return The created VisTextButton.
     */
    VisTextButton createStyledButton(String text, Runnable action) {
        VisTextButton button = new VisTextButton(text);
        button.getLabel().setFontScale(2f);
//        button.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        button.addListener(event -> {
            if (button.isPressed()) {
                action.run();
                return true;
            }
            return false;
        });
        button.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(1f)));
        return button;
    }

    @Override
    public void show() {
        // Set up the viewport and stage
        ScreenViewport viewport = new ScreenViewport();
        stage = new Stage(viewport);
        // take input from this screen
        Gdx.input.setInputProcessor(stage);

        // shape animations
        shapeRenderer = new ShapeRenderer();
        fallingPieces = new ArrayList<>();

        // Define the restricted zone where pieces shouldn't spawn
        float restrictedZoneTop = Gdx.graphics.getHeight() / 2f + 200;
        float restrictedZoneBottom = Gdx.graphics.getHeight() / 2f - 200;

        // Define the left and right spawn ranges
        float leftSpawnRange = Gdx.graphics.getWidth() * 0.25f;
        float rightSpawnRange = Gdx.graphics.getWidth() * 0.75f;

        // use 7-bag randomizer
        List<Tetrimino> bag = new ArrayList<>(Arrays.asList(Tetrimino.values()));
        Collections.shuffle(bag);

        // Create falling pieces
        for (int i = 0; i < 5; i++) {
            Tetrimino type = bag.get(i % bag.size()); // cycle through bag
            Vector2 position;

            // randomly choose between left and right spawn ranges
            if (Math.random() < 0.5) {
                position = new Vector2((float) Math.random() * leftSpawnRange,
                    (float) Math.random() * Gdx.graphics.getHeight());
            } else {
                position = new Vector2(rightSpawnRange + (float) Math.random() * (Gdx.graphics.getWidth() - rightSpawnRange),
                    (float) Math.random() * Gdx.graphics.getHeight());
            }

            // Ensure the piece is not spawned in the restricted zone
            while (position.y < restrictedZoneTop && position.y > restrictedZoneBottom) {
                position.y = (float) Math.random() * Gdx.graphics.getHeight();
            }

            Vector2 velocity = new Vector2(0, -50); // fall speed
            fallingPieces.add(new FallingPiece(type, position, velocity));
        }

        // main title
        VisLabel menuLabel = new VisLabel("Tetris");
        menuLabel.setFontScale(3f);
        menuLabel.getColor().a = 0;
        menuLabel.addAction(Actions.fadeIn(1f));

        // Create buttons for different game modes and options
        VisTextButton classicPlayButton = createStyledButton("Classic Mode", () -> game.setScreen(new GameScreen(game)));
        VisTextButton sprintPlayButton = createStyledButton("Sprint Mode", () -> game.setScreen(new SprintScreen(game)));
        VisTextButton arcadePlayButton = createStyledButton("Arcade Mode", () -> game.setScreen(new ArcadeScreen(game)));
        VisTextButton instructionsButton = createStyledButton("How to Play", () -> game.setScreen(new InstructionsScreen(game)));
        VisTextButton settingsButton = createStyledButton("Settings", () -> game.setScreen(new SettingsScreen(game)));
        VisTextButton quitButton = createStyledButton("Exit Game", () -> Gdx.app.exit());

        // each button starts transparent
        classicPlayButton.getColor().a = 0;
        sprintPlayButton.getColor().a = 0;
        arcadePlayButton.getColor().a = 0;
        instructionsButton.getColor().a = 0;
        settingsButton.getColor().a = 0;
        quitButton.getColor().a = 0;

        // add fade-in actions for the buttons
        classicPlayButton.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(1f)));
        sprintPlayButton.addAction(Actions.sequence(Actions.delay(1f), Actions.fadeIn(1f)));
        arcadePlayButton.addAction(Actions.sequence(Actions.delay(1.5f), Actions.fadeIn(1f)));
        instructionsButton.addAction(Actions.sequence(Actions.delay(2f), Actions.fadeIn(1f)));
        settingsButton.addAction(Actions.sequence(Actions.delay(2.5f), Actions.fadeIn(1f)));
        quitButton.addAction(Actions.sequence(Actions.delay(3f), Actions.fadeIn(1f)));

        // create the main table
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // add each element to the table, pad each button
        table.add(menuLabel).padBottom(80f).row();
        table.add(classicPlayButton).width(300).height(70).padBottom(30f).row();
        table.add(sprintPlayButton).width(300).height(70).padBottom(30f).row();
        table.add(arcadePlayButton).width(300).height(70).padBottom(30f).row();
        table.add(instructionsButton).width(300).height(70).padBottom(30f).row();
        table.add(settingsButton).width(300).height(70).padBottom(30f).row();
        table.add(quitButton).width(300).height(70).padBottom(60f).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
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
