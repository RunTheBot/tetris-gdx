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
    public static final int GRID_HEIGHT = 20;
    public static final int BLOCK_SIZE = 30;

    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;
    public OrthographicCamera camera;

    @Override
    public void create() {
        // load UI library
        VisUI.load();

        batch = new SpriteBatch();
        font = new BitmapFont();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE);
        viewport = new FitViewport(GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE, camera); // logical size
        viewport.apply();
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
