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
    /** Width of the Tetris grid. */
    public static final int GRID_WIDTH = 10;
    /** Size of the buffer region above the visible grid. */
    public static final int BUFFER_SIZE = 20;
    /** Height of the Tetris grid including the buffer. */
    public static final int GRID_HEIGHT = 20 + BUFFER_SIZE;
    /** Width of the viewport in world units. */
    public static float VIEWPORT_WIDTH = GRID_WIDTH * 2.5f;
    /** Height of the viewport in world units. */
    public static float VIEWPOET_HEIGHT = (GRID_HEIGHT - BUFFER_SIZE + 2);
//    public static final int BLOCK_SIZE = 30;

    /** Used for drawing sprites and textures. */
    public SpriteBatch batch;
    /** Font used for drawing text. */
    public BitmapFont font;
    /** Manages the viewport for different screen sizes. */
    public FitViewport viewport;
    /** Camera used for the viewport. */
    public OrthographicCamera camera;

    /**
     * Called when the game is created. Initializes game resources and sets the initial screen.
     */
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

    /**
     * Called every frame. Renders the current screen.
     */
    public void render() {
        super.render();
    }

    /**
     * Called when the game is disposed. Releases all resources.
     */
    @Override
    public void dispose() {
        super.dispose();
        VisUI.dispose();
        if (screen != null) {
            screen.dispose();
        }
    }
}
