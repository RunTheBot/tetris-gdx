package me.runthebot.tetris;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.VisUI;

/**
 * Main entry point for the Tetris game, shared by all platforms.
 * Sets up the camera, viewport, and initial screen.
 */
public class Tetris extends Game {
    public static final int GRID_WIDTH = 10;
    public static final int BUFFER_SIZE = 20;
    public static final int GRID_HEIGHT = 20 + BUFFER_SIZE;
    public static float VIEWPORT_WIDTH = GRID_WIDTH * 2.5f;
    public static float VIEWPOET_HEIGHT = (GRID_HEIGHT - BUFFER_SIZE + 2);
//    public static final int BLOCK_SIZE = 30;

    // initialize the game features (used for screen features)
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

        // set up the camera and viewport (wider to accommodate both hold and next pieces)
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPOET_HEIGHT);
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPOET_HEIGHT, camera); // logical size
        viewport.apply();

        // return the menu screen
        this.setScreen(new MenuScreen(this));
    }

    public void render() {
        super.render();
    }

    // destroy the ui when the screen is closed
    @Override
    public void dispose() {
        super.dispose();
        VisUI.dispose();
        if (screen != null) {
            screen.dispose();
        }
    }
}
