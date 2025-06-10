package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * The pause screen for the Tetris game.
 * Allows the player to resume the game, quit to the menu, or exit the game.
 */
public class PauseScreen implements Screen {
    private final Tetris game;
    private final Screen previousScreen;
    private  final Stage stage;

    /**
     * Constructor for the PauseScreen.
     * @param game The main Tetris game instance.
     * @param previousScreen The screen to return to when resuming.
     */
    public PauseScreen(Tetris game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        // load visui?

        stage = new Stage(new ScreenViewport(), new SpriteBatch());
        Gdx.input.setInputProcessor(stage);

        createUI();
    }

    /**
     * Creates the UI elements for the pause screen.
     */
    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        VisLabel titleLabel = new VisLabel("PAUSED");
        titleLabel.setFontScale(3);

        VisTextButton resumeButton = new VisTextButton("Resume");
        VisTextButton quitButton = new VisTextButton("Quit to menu");
        VisTextButton exitButton = new VisTextButton("Exit game");

        resumeButton.getLabel().setFontScale(2f);
        quitButton.getLabel().setFontScale(2f);
        exitButton.getLabel().setFontScale(2f);

        resumeButton.addListener(event -> {
            if (resumeButton.isPressed()) {
                game.setScreen(previousScreen);
                return true;
            }
            return false;
        });

        quitButton.addListener(event -> {
            if (quitButton.isPressed()) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
            return false;
        });

        exitButton.addListener(event -> {
            if (exitButton.isPressed()) {
                Gdx.app.exit();
                return true;
            }
            return false;
        });

        table.add(titleLabel).padBottom(30).row();
        table.add(resumeButton).width(300).height(70).pad(10).row();
        table.add(quitButton).width(300).height(70).pad(10).row();
        table.add(exitButton).width(300).height(70).pad(10);

        stage.addActor(table);
    }


    /**
     * Renders the pause screen.
     * @param delta The time in seconds since the last frame.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0,0.8f);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Resizes the viewport when the screen size changes.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Called when this screen becomes the current screen for a {@link Game}.
     */
    @Override
    public void show() {}

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    @Override
    public void hide() {}

    /**
     * Called when the {@link Application} is paused.
     */
    @Override
    public void pause() {}

    /**
     * Called when the {@link Application} is resumed.
     */
    @Override
    public void resume() {}

    /**
     * Disposes of the resources used by this screen.
     */
    @Override
    public void dispose() {
        stage.dispose();
        if (VisUI.isLoaded()) {
            VisUI.dispose();
        }
    }
}
