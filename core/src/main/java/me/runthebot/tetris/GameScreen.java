package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {
    public static final int GRID_WIDTH = 10;
    public static final int GRID_HEIGHT = 20;
    public static final int BLOCK_SIZE = 30;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;

    private final Grid grid;
    private Piece currentPiece;

    private long lastFallTime;
    private float gravity = 0.5f; // Tiles per second

    public GameScreen() {
        camera = new OrthographicCamera();
//        camera.setToOrtho(false, GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE);

        camera.setToOrtho(false, GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE);

        viewport = new FitViewport(GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE, camera); // logical size
        viewport.apply();

        shapeRenderer = new ShapeRenderer();

        grid = new Grid(GRID_WIDTH, GRID_HEIGHT);
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();
    }

    private void spawnNewPiece() {
        Tetrimino t = Tetrimino.values()[(int) (Math.random() * Tetrimino.values().length)];
        currentPiece = new Piece(t);
    }

    @Override
    public void render(float delta) {
        handleInput();
        update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        grid.render(shapeRenderer);
        currentPiece.render(shapeRenderer);
        shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            currentPiece.move(-1, 0, grid);
        } if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            currentPiece.move(1, 0, grid);
        } if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            currentPiece.move(0, 1, grid);
        } if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            currentPiece.rotate(grid);
        }
    }

    private void update() {
        if (TimeUtils.timeSinceMillis(lastFallTime) > 1000 / gravity) {
            boolean moved = currentPiece.move(0, 1, grid);
            if (!moved) {
                grid.lockPiece(currentPiece);
                spawnNewPiece();
            }
            lastFallTime = TimeUtils.millis();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {
        viewport.update(width, height);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
