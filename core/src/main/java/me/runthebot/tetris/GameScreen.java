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

import java.util.*;

public class GameScreen implements Screen {
    final Tetris game;

    // DAS and ARR constants (in milliseconds)
    private static final long DAS_DELAY = 170; // Delayed Auto Shift initial delay
    private static final long ARR_DELAY = 30;  // Auto Repeat Rate delay

    // Track key press times and last move times
    private long leftPressTime = 0;
    private long rightPressTime = 0;
    private long lastLeftMoveTime = 0;
    private long lastRightMoveTime = 0;

    private final ShapeRenderer shapeRenderer;

    private final Grid grid;
    private Piece currentPiece;
    private Piece ghostPiece;  // Ghost piece for landing preview
    private Queue<Tetrimino> nextPieces;

    private long lastFallTime;
    private float gravity = 0.5f; // Tiles per second

    private boolean gameOver = false; // Track game over state

    public GameScreen(final Tetris game) {
        this.game = game;

        shapeRenderer = new ShapeRenderer();
        grid = new Grid(Tetris.GRID_WIDTH, Tetris.GRID_HEIGHT);
        nextPieces = new LinkedList<>();
        fillBag(); // Initialize with first bag
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();
    }

    @Override
    public void render(float delta) {
        if (gameOver) {
            // Render game over screen
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            game.camera.update();
            shapeRenderer.setProjectionMatrix(game.camera.combined);

            // Optionally, render the final grid
            grid.render(shapeRenderer);

            shapeRenderer.end();
            game.setScreen(new GameOverScreen(game));

            return;
        }

        handleInput();
        update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        shapeRenderer.setProjectionMatrix(game.camera.combined);
        grid.render(shapeRenderer);

        // Render ghost piece with transparency
        ghostPiece.render(shapeRenderer, 0.3f);  // Pass alpha value for transparency
        currentPiece.render(shapeRenderer);

        // TODO: this does not render because the camera doesnt add stuff that is off grid
        renderNextPiece();

        shapeRenderer.end();
    }

    private void renderNextPiece() {
        if (nextPieces.isEmpty()) return;

        // get the next piece
        Tetrimino nextPiece = nextPieces.peek();

        boolean[][] shape = nextPiece.getShape();
        Color color = nextPiece.getColor();

        // preview position
        float previewX = Tetris.GRID_WIDTH * Tetris.BLOCK_SIZE + Tetris.BUFFER_SIZE;
        float previewY = Tetris.GRID_HEIGHT * Tetris.BLOCK_SIZE - Tetris.BUFFER_SIZE;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);

        // render the next piece
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    float blockX = previewX + col * Tetris.BLOCK_SIZE;
                    float blockY = previewY - row * Tetris.BLOCK_SIZE;
                    shapeRenderer.rect(blockX, blockY, Tetris.BLOCK_SIZE, Tetris.BLOCK_SIZE);
                }
            }
        }
        shapeRenderer.end();
    }

    /**
     * Generates a new shuffled bag of all 7 Tetriminos and adds them to the queue
     */
    private void fillBag() {
        List<Tetrimino> bag = new ArrayList<>(Arrays.asList(Tetrimino.values()));
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
        ghostPiece = new Piece(t);  // Create ghost piece with the same shape
        updateGhostPiece();  // Position the ghost

        // Game over check: if the new piece collides immediately, game over
        if (!isValidPosition(currentPiece)) {
            gameOver = true;
        }
    }

    /**
     * Checks if the piece's current position is valid (not colliding or out of bounds)
     */
    private boolean isValidPosition(Piece piece) {
        boolean[][] shape = piece.getShape();
        int px = piece.getX();
        int py = piece.getY();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    int x = px + col;
                    int y = py + row;
                    // Check bounds
                    if (x < 0 || x >= Tetris.GRID_WIDTH || y < 0 || y >= Tetris.GRID_HEIGHT) {
                        return false;
                    }
                    // Check collision with locked blocks
                    if (grid.isOccupied(x, y)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Updates the ghost piece to show where the current piece would land
     */
    private void updateGhostPiece() {
        // Create a fresh copy of the current piece to ensure correct shape/rotation
        ghostPiece = new Piece(currentPiece.getType());
        ghostPiece.setRotation(currentPiece.getRotation());
        ghostPiece.setPosition(currentPiece.getX(), currentPiece.getY());

        // Drop the ghost piece as far as it can go
        while (ghostPiece.move(0, 1, grid)) { }
    }

    public void placePiece(){
        grid.lockPiece(currentPiece);

        // Check for line clears after locking the piece
        int linesCleared = grid.checkAndClearLines();
        // TODO: Update score based on linesCleared

        spawnNewPiece();

        // Check for game over condition
        // (handled in spawnNewPiece)
    }

    private void handleInput() {
        if (gameOver) return; // Ignore input if game is over

        long currentTime = TimeUtils.millis();

        // Hard drop (Space key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            int rowsDropped = currentPiece.hardDrop(grid);

            placePiece();

            return;
        }

        // Soft drop (faster fall)
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            boolean moved = currentPiece.move(0, 1, grid);
            if (moved) {
                updateGhostPiece();
            }
        }

        // Left movement with DAS
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            // Initial press
            if (leftPressTime == 0) {
                leftPressTime = currentTime;
                lastLeftMoveTime = currentTime;
                if (currentPiece.move(-1, 0, grid)) {
                    updateGhostPiece();
                }
            } else {
                long elapsedSincePress = currentTime - leftPressTime;
                long elapsedSinceLastMove = currentTime - lastLeftMoveTime;

                // If we've passed the DAS delay, move all the way to the left edge
                if (elapsedSincePress > DAS_DELAY && elapsedSinceLastMove >= ARR_DELAY) {
                    boolean moved = false;
                    // Move all the way to the left until it can't move anymore
                    while (currentPiece.move(-1, 0, grid)) {
                        moved = true;
                    }
                    if (moved) updateGhostPiece();
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
                if (currentPiece.move(1, 0, grid)) {
                    updateGhostPiece();
                }
            } else {
                long elapsedSincePress = currentTime - rightPressTime;
                long elapsedSinceLastMove = currentTime - lastRightMoveTime;

                // If we've passed the DAS delay and it's time for ARR movement
                if (elapsedSincePress > DAS_DELAY && elapsedSinceLastMove >= ARR_DELAY) {
                    boolean moved = false;
                    // Move all the way to the right until it can't move anymore
                    while (currentPiece.move(1, 0, grid)) {
                        moved = true;
                    }
                    if (moved) updateGhostPiece();
                    lastRightMoveTime = currentTime;
                }
            }
        } else {
            rightPressTime = 0;  // Reset only when key is released
        }

        // Rotation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (currentPiece.rotate(grid)) {
                updateGhostPiece();
            }
        }
    }

    private void update() {
        if (gameOver) return; // Stop updates if game is over
        if (TimeUtils.timeSinceMillis(lastFallTime) > 1000 / gravity) {
            boolean moved = currentPiece.move(0, 1, grid);
            if (!moved) {
                // If the piece can't move down, place it
                placePiece();
            }
            lastFallTime = TimeUtils.millis();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {
        game.viewport.update(width, height);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
