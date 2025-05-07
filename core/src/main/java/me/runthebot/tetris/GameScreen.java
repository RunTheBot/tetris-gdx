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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameScreen implements Screen {
    public static final int GRID_WIDTH = 10;
    public static final int GRID_HEIGHT = 20;
    public static final int BLOCK_SIZE = 30;

    // DAS and ARR constants (in milliseconds)
    private static final long DAS_DELAY = 170; // Delayed Auto Shift initial delay
    private static final long ARR_DELAY = 30;  // Auto Repeat Rate delay

    // Track key press times and last move times
    private long leftPressTime = 0;
    private long rightPressTime = 0;
    private long lastLeftMoveTime = 0;
    private long lastRightMoveTime = 0;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;

    private final Grid grid;
    private Piece currentPiece;
    private Queue<Tetrimino> nextPieces;

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
        nextPieces = new LinkedList<>();
        fillBag(); // Initialize with first bag
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();
    }

    /**
     * Generates a new shuffled bag of all 7 Tetriminos and adds them to the queue
     */
    private void fillBag() {
        List<Tetrimino> bag = new ArrayList<>();
        for (Tetrimino tetrimino : Tetrimino.values()) {
            bag.add(tetrimino);
        }
        Collections.shuffle(bag);
        nextPieces.addAll(bag);
    }

    private void spawnNewPiece() {
        // Check if we need to refill the bag
        if (nextPieces.size() < 7) {
            fillBag();
        }

        // Get the next piece from the queue
        Tetrimino t = nextPieces.poll();
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
        long currentTime = TimeUtils.millis();

        // Hard drop (Space key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            int rowsDropped = currentPiece.hardDrop(grid);
            // rowsDropped can be used for scoring in the future
            grid.lockPiece(currentPiece);

            // Check for line clears after locking the piece
            int linesCleared = grid.checkAndClearLines();
            // TODO: Update score based on linesCleared

            spawnNewPiece();
            return;
        }

        // Soft drop (faster fall)
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            currentPiece.move(0, 1, grid);
        }

        // Left movement with DAS
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            // Initial press
            if (leftPressTime == 0) {
                leftPressTime = currentTime;
                lastLeftMoveTime = currentTime;
                currentPiece.move(-1, 0, grid);
            } else {
                long elapsedSincePress = currentTime - leftPressTime;
                long elapsedSinceLastMove = currentTime - lastLeftMoveTime;

                // If we've passed the DAS delay, move all the way to the left edge
                if (elapsedSincePress > DAS_DELAY && elapsedSinceLastMove >= ARR_DELAY) {
                    // Move all the way to the left until it can't move anymore
                    while (currentPiece.move(-1, 0, grid)) {}
                    lastLeftMoveTime = currentTime;
                }
            }
        } else {
            leftPressTime = 0;  // Reset only when key is released
        }

        // Right movement with DAS
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            // Initial press
            if (rightPressTime == 0) {
                rightPressTime = currentTime;
                lastRightMoveTime = currentTime;
                currentPiece.move(1, 0, grid);
            } else {
                long elapsedSincePress = currentTime - rightPressTime;
                long elapsedSinceLastMove = currentTime - lastRightMoveTime;

                // If we've passed the DAS delay and it's time for ARR movement
                if (elapsedSincePress > DAS_DELAY && elapsedSinceLastMove >= ARR_DELAY) {
                    // Move all the way to the right until it can't move anymore
                    while (currentPiece.move(1, 0, grid)) {}
                    lastRightMoveTime = currentTime;
                }
            }
        } else {
            rightPressTime = 0;  // Reset only when key is released
        }

        // Rotation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            currentPiece.rotate(grid);
        }
    }

    private void update() {
        if (TimeUtils.timeSinceMillis(lastFallTime) > 1000 / gravity) {
            boolean moved = currentPiece.move(0, 1, grid);
            if (!moved) {
                grid.lockPiece(currentPiece);

                // Also check for line clears here for consistency
                int linesCleared = grid.checkAndClearLines();
                // TODO: Update score based on linesCleared

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
