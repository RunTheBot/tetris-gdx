package me.runthebot.tetris;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.VisUI;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Tetris extends Game {
    public static final int GRID_WIDTH = 10;
    public static final int BUFFER_SIZE = 20;
    public static final int GRID_HEIGHT = 20 + BUFFER_SIZE;
    public static final int BLOCK_SIZE = 30;

    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;
    public OrthographicCamera camera;

    @Override
    public void create() {
        // load UI library
        VisUI.load(VisUI.SkinScale.X1);

        // initialize batch, font, camera
        batch = new SpriteBatch();
        font = new BitmapFont();
        camera = new OrthographicCamera();

        // set up the camera and viewport
        camera.setToOrtho(false, GRID_WIDTH * BLOCK_SIZE, (GRID_HEIGHT - BUFFER_SIZE + 2) * BLOCK_SIZE);
        viewport = new FitViewport(GRID_WIDTH * BLOCK_SIZE, (GRID_HEIGHT - BUFFER_SIZE + 2) * BLOCK_SIZE, camera); // logical size
        viewport.apply();

        // return the menu screen
        this.setScreen(new MenuScreen(this));
    }

    public void render() {
        super.render();
    }

    // this seems right
    @Override
    public void dispose() {
        super.dispose();
        VisUI.dispose();
        if (screen != null) {
            screen.dispose();
        }
    }
}
