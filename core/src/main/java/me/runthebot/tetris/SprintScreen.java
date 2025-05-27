package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.*;

public class SprintScreen implements Screen {
    final Tetris game;


    ConfigManager configManager = ConfigManager.getInstance();
    GameConfig config = configManager.getConfig();

    // Track key press times and last move times
    private long leftPressTime = 0;
    private long rightPressTime = 0;
    private long lastLeftMoveTime = 0;
    private long lastRightMoveTime = 0;

    private final ShapeRenderer shapeRenderer;

    private final Grid grid;
    private Piece currentPiece;
    private Piece ghostPiece;  // Ghost piece for landing preview
    private Piece holdPiece;   // Hold piece
    private boolean canHold = true;  // Flag to prevent multiple holds per piece
    private Queue<Tetrimino> nextPieces;

    private long lastFallTime;
    private float gravity = 0.5f; // Tiles per second

    private boolean gameOver = false; // Track game over state

    // Lock delay variables
    private boolean lockDelayActive = false;
    private long lockDelayStartTime = 0;
    private final long LOCK_DELAY = 500; // Lock delay in milliseconds
    private int lockResets = 0;
    private final int MAX_LOCK_RESETS = 15;

    // Game stats
    private int linesCleared = 0; // Track lines cleared
    private int targetLines = 40; // Default for sprint mode
    private int linesLeft; // Lines remaining to clear
    private long startTime; // When the game started
    private long currentTime; // Current game time
    private float pace; // Lines per minute pace
    private float currentSpeed; // Current pieces per second
    private float maxSpeed = 0; // Maximum speed achieved
    private int highScore = 0; // High score (fastest time)
    private int level = 1; // Current level

    private final BitmapFont font;
    private final SpriteBatch spriteBatch;

    public SprintScreen(final Tetris game) {
        this.game = game;

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        spriteBatch = new SpriteBatch();
        grid = new Grid(Tetris.GRID_WIDTH, Tetris.GRID_HEIGHT);
        nextPieces = new LinkedList<>();
        fillBag(); // Initialize with first bag
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();

        // Initialize sprint mode stats
        startTime = TimeUtils.millis();
        linesLeft = targetLines - linesCleared;
        pace = 0;
        currentSpeed = 0;

        // We don't need to manually set camera position, the viewport handles it
    }

    @Override
    public void render(float delta) {
        if (gameOver) {
            // Capture final stats
            long finalTime = TimeUtils.millis() - startTime;

            // Pass the game type and stats to game over screen
            game.setScreen(new GameOverScreen(game, "sprint", 0, level, linesCleared,
                            finalTime, currentSpeed, maxSpeed, targetLines - linesCleared));
            return;
        }

        handleInput();
        update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        shapeRenderer.setProjectionMatrix(game.camera.combined);

        // Begin shape rendering
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render game elements
        grid.render(shapeRenderer);

        if (config.showGhostPiece) {
            // Render ghost piece with transparency
            ghostPiece.render(shapeRenderer, 0.3f);  // Pass alpha value for transparency
        }

        currentPiece.render(shapeRenderer);

        // End shape rendering started in this method
        shapeRenderer.end();

        // Hold and Next pieces have their own begin/end calls
        renderHoldPiece();
        renderNextPiece();

        // Update statistics
        currentTime = TimeUtils.millis() - startTime;
        linesLeft = targetLines - linesCleared;

        // Calculate pace (lines per minute)
        if (currentTime > 0) {
            pace = (float) linesCleared / (currentTime / 60000.0f);

            // Calculate current speed (moves per second)
            currentSpeed = (float) linesCleared / (currentTime / 1000.0f);

            // Update max speed if current is higher
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
            }
        }

        // Render UI with updated stats
        renderUI();
    }

    /**
     * Renders game statistics and UI
     */
    private void renderUI() {
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        // Format time as mm:ss.ms
        String timeString = String.format("%02d:%02d.%d",
                (currentTime / 60000),
                (currentTime / 1000) % 60,
                (currentTime / 100) % 10);

        // Display all sprint mode stats
        font.draw(spriteBatch, "SPRINT MODE", 20, Gdx.graphics.getHeight() - 20);
        font.draw(spriteBatch, "Time: " + timeString, 20, Gdx.graphics.getHeight() - 50);
        font.draw(spriteBatch, "Lines: " + linesCleared + "/" + targetLines, 20, Gdx.graphics.getHeight() - 80);
        font.draw(spriteBatch, "Lines Left: " + linesLeft, 20, Gdx.graphics.getHeight() - 110);
        font.draw(spriteBatch, "Pace: " + String.format("%.2f", pace) + " lpm", 20, Gdx.graphics.getHeight() - 140);
        font.draw(spriteBatch, "Speed: " + String.format("%.2f", currentSpeed) + " lps", 20, Gdx.graphics.getHeight() - 170);
        font.draw(spriteBatch, "Max Speed: " + String.format("%.2f", maxSpeed) + " lps", 20, Gdx.graphics.getHeight() - 200);

        spriteBatch.end();
    }

    private void renderNextPiece() {
        if (nextPieces.isEmpty()) return;

        // get the next piece
        Tetrimino nextPiece = nextPieces.peek();

        boolean[][] shape = nextPiece.getShape();
        Color color = nextPiece.getColor();

        // Next piece position - on the right side of the grid
        float gridOffset = Grid.CENTER_OFFSET;
        float gridCenterX = gridOffset + Tetris.GRID_WIDTH / 2.0f; // Center of the grid with offset
        float previewX = gridCenterX + 5.0f; // Position right of the grid
        // Position next piece at the top of the visible area
        float previewY = 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);

        // Draw a background rectangle for the next piece area
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(previewX - 0.25f, 0.25f, 4.5f, 6);

        // Draw "NEXT" text border
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(previewX - 0.25f, 6.5f, 4.5f, 1);

        // Draw "NEXT" text using shapes
        shapeRenderer.setColor(0, 0, 0, 1);
        // N
        shapeRenderer.rectLine(previewX + 0.5f, 7.0f, previewX + 0.5f, 7.35f, 0.15f);
        shapeRenderer.rectLine(previewX + 0.5f, 7.35f, previewX + 1.0f, 7.0f, 0.15f);
        shapeRenderer.rectLine(previewX + 1.0f, 7.0f, previewX + 1.0f, 7.35f, 0.15f);
        // E
        shapeRenderer.rectLine(previewX + 1.3f, 7.0f, previewX + 1.3f, 7.35f, 0.15f);
        shapeRenderer.rectLine(previewX + 1.3f, 7.0f, previewX + 1.8f, 7.0f, 0.15f);
        shapeRenderer.rectLine(previewX + 1.3f, 7.2f, previewX + 1.7f, 7.2f, 0.15f);
        shapeRenderer.rectLine(previewX + 1.3f, 7.35f, previewX + 1.8f, 7.35f, 0.15f);
        // X
        shapeRenderer.rectLine(previewX + 2.1f, 7.0f, previewX + 2.6f, 7.35f, 0.15f);
        shapeRenderer.rectLine(previewX + 2.1f, 7.35f, previewX + 2.6f, 7.0f, 0.15f);
        // T
        shapeRenderer.rectLine(previewX + 2.9f, 7.35f, previewX + 3.4f, 7.35f, 0.15f);
        shapeRenderer.rectLine(previewX + 3.15f, 7.35f, previewX + 3.15f, 7.0f, 0.15f);

        // reset to piece color
        shapeRenderer.setColor(color);

        // Center the piece in the preview area based on its width
        float offsetX = (4 - shape[0].length) / 2.0f;
        float offsetY = (4 - shape.length) / 2.0f;

        // render the next piece
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    float blockX = previewX + col + offsetX;
                    float blockY = previewY -row + offsetY;
                    shapeRenderer.rect(blockX, blockY, 1, 1);
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

        lockResets = 0; // Reset lock resets
        lockDelayActive = false; // Reset lock delay active

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
        int lines = grid.checkAndClearLines();
        linesCleared += lines;
        linesLeft = targetLines - linesCleared;

        // Update level (every 10 lines cleared)
        level = (linesCleared / 10) + 1;

        // Update speed tracking after each piece placement
        if (TimeUtils.millis() - startTime > 0) {
            currentSpeed = (float) linesCleared / ((TimeUtils.millis() - startTime) / 1000.0f);
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
            }
        }

        // Reset lock delay mechanism as piece is now placed
        lockDelayActive = false;
        lockResets = 0;

        spawnNewPiece();
        canHold = true; // Reset the hold flag after placing a piece
    }

    private void handleInput() {
        if (gameOver) return; // Ignore input if game is over

        long currentTime = TimeUtils.millis();

        // Hold piece (Shift key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
            holdPiece();
            return;
        }

        // Hard drop (Space key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            currentPiece.hardDrop(grid); // No need to store the return value

            placePiece();

            return;
        }

        // Soft drop (faster fall)
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            boolean moved = currentPiece.move(0, 1, grid);
            if (moved) {
                updateGhostPiece();
                // Reset lock delay when piece is moved by soft drop
                if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                    lockDelayStartTime = TimeUtils.millis();
                    lockResets++;
                }
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
                    // Reset lock delay when piece is moved
                    if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                        lockDelayStartTime = TimeUtils.millis();
                        lockResets++;
                    }
                }
            } else {
                long elapsedSincePress = currentTime - leftPressTime;
                long elapsedSinceLastMove = currentTime - lastLeftMoveTime;

                // If we've passed the DAS delay, move all the way to the left edge
                if (elapsedSincePress > config.DAS_DELAY && elapsedSinceLastMove >= config.ARR_DELAY) {
                    boolean moved = false;
                    // Move all the way to the left until it can't move anymore
                    while (currentPiece.move(-1, 0, grid)) {
                        moved = true;
                        // Reset lock delay when piece is moved
                        if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                            lockDelayStartTime = TimeUtils.millis();
                            lockResets++;
                        }
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
                    // Reset lock delay when piece is moved
                    if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                        lockDelayStartTime = TimeUtils.millis();
                        lockResets++;
                    }
                }
            } else {
                long elapsedSincePress = currentTime - rightPressTime;
                long elapsedSinceLastMove = currentTime - lastRightMoveTime;

                // If we've passed the DAS delay and it's time for ARR movement
                if (elapsedSincePress > config.DAS_DELAY && elapsedSinceLastMove >= config.ARR_DELAY) {
                    boolean moved = false;
                    // Move all the way to the right until it can't move anymore
                    while (currentPiece.move(1, 0, grid)) {
                        moved = true;
                        // Reset lock delay when piece is moved
                        if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                            lockDelayStartTime = TimeUtils.millis();
                            lockResets++;
                        }
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
                // Reset lock delay when piece is rotated
                if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                    lockDelayStartTime = TimeUtils.millis();
                    lockResets++;
                }
            }
        }

        // Hold piece (C key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            holdPiece();
        }
    }

    private void update() {
        if (gameOver) return; // Stop updates if game is over

        // Try to move the piece down due to gravity
        if (TimeUtils.timeSinceMillis(lastFallTime) >= (1000 / gravity)) {
            if (currentPiece.move(0, 1, grid)) { // Attempt to move piece down
                // Piece moved down successfully
                lastFallTime = TimeUtils.millis();
                lockDelayActive = false; // If piece is falling, it's not in lock delay
                updateGhostPiece();
            } else {
                // Piece could not move down (landed)
                if (!lockDelayActive) {
                    // Activate lock delay if not already active
                    lockDelayActive = true;
                    lockDelayStartTime = TimeUtils.millis();
                }
                // Note: lastFallTime is not reset here because the piece didn't fall.
                // The gravity timer effectively pauses while the piece is landed and lock delay is potentially active.
            }
        }

        // Handle lock delay
        if (lockDelayActive) {
            // Check if the lock delay time has passed or max resets reached
            if (TimeUtils.millis() - lockDelayStartTime > LOCK_DELAY || lockResets >= MAX_LOCK_RESETS) {
                // Before placing, make a final check if the piece can move down
                // This handles scenarios like a line clear opening space below
                if (!currentPiece.move(0, 1, grid)) {
                    // Still cannot move down, so place the piece
                    placePiece(); // This method should handle resetting lockDelayActive and lockResets
                } else {
                    // Piece was able to move down (e.g., space cleared below)
                    // It has now moved down one step.
                    lastFallTime = TimeUtils.millis(); // Reset fall time as it moved
                    lockDelayActive = false;           // No longer in lock delay
                    lockResets = 0;                    // Resets are cleared as it moved instead of locking
                    updateGhostPiece();
                    // The piece has already been moved down by currentPiece.move(0,1,grid)
                }
            }
        }
    }

    /**
     * Holds the current piece, allowing the player to swap it with the next piece.
     * The held piece is stored in the holdPiece variable, and the current piece is replaced
     * by a new piece from the nextPieces queue.
     */    private void holdPiece() {
        if (!canHold) return; // Can't hold twice in a row

        Tetrimino currentType = currentPiece.getType();

        if (holdPiece == null) {
            // First hold - no piece to swap
            holdPiece = new Piece(currentType);
            spawnNewPiece();
        } else {
            // Swap pieces
            Tetrimino holdType = holdPiece.getType();
            holdPiece = new Piece(currentType);
            currentPiece = new Piece(holdType);
            // Reset rotation and position for piece coming from hold
            currentPiece.setPosition(3, Tetris.BUFFER_SIZE - 2);
            ghostPiece = new Piece(holdType);
            updateGhostPiece();
        }

        canHold = false; // Prevent holding again until next piece
    }

    /**
     * Renders the hold piece
     */
    private void renderHoldPiece() {
        if (holdPiece == null) return;

        boolean[][] shape = holdPiece.getType().getShape();
        Color color = holdPiece.getType().getColor();

        // Hold position - on the left side of the grid
        float gridOffset = Grid.CENTER_OFFSET     ;
        float gridCenterX = gridOffset + Tetris.GRID_WIDTH / 2.0f; // Center of the grid with offset
        float holdX = gridCenterX - 9.0f; // Position left of the grid
        float holdY = 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw a background rectangle for the hold piece area
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(holdX - 0.25f, 0.25f, 4.5f, 6);        // Draw "HOLD" text border
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(holdX - 0.25f, 6.5f, 4.5f, 1);

        // Draw "HOLD" text using shapes
        shapeRenderer.setColor(0, 0, 0, 1);
        // H
        shapeRenderer.rectLine(holdX + 0.5f, 7.0f, holdX + 0.5f, 7.35f, 0.15f);
        shapeRenderer.rectLine(holdX + 0.5f, 7.2f, holdX + 1.0f, 7.2f, 0.15f);
        shapeRenderer.rectLine(holdX + 1.0f, 7.0f, holdX + 1.0f, 7.35f, 0.15f);
        // O
        shapeRenderer.rectLine(holdX + 1.3f, 7.0f, holdX + 1.3f, 7.35f, 0.15f);
        shapeRenderer.rectLine(holdX + 1.3f, 7.0f, holdX + 1.8f, 7.0f, 0.15f);
        shapeRenderer.rectLine(holdX + 1.8f, 7.0f, holdX + 1.8f, 7.35f, 0.15f);
        shapeRenderer.rectLine(holdX + 1.3f, 7.35f, holdX + 1.8f, 7.35f, 0.15f);
        // L
        shapeRenderer.rectLine(holdX + 2.1f, 7.0f, holdX + 2.1f, 7.35f, 0.15f);
        shapeRenderer.rectLine(holdX + 2.1f, 7.0f, holdX + 2.6f, 7.0f, 0.15f);
        // D
        shapeRenderer.rectLine(holdX + 2.9f, 7.0f, holdX + 2.9f, 7.35f, 0.15f);
        shapeRenderer.rectLine(holdX + 2.9f, 7.0f, holdX + 3.3f, 7.1f, 0.15f);
        shapeRenderer.rectLine(holdX + 3.3f, 7.1f, holdX + 3.3f, 7.25f, 0.15f);
        shapeRenderer.rectLine(holdX + 3.3f, 7.25f, holdX + 2.9f, 7.35f, 0.15f);

        // Set color to piece color (dimmed if can't hold)
        if (canHold) {
            shapeRenderer.setColor(color);
        } else {
            // Dimmed version of the color
            Color dimmed = new Color(color);
            dimmed.a = 0.5f;
            shapeRenderer.setColor(dimmed);
        }

        // Center the piece in the hold area based on its width
        float offsetX = (4 - shape[0].length) / 2.0f;
        float offsetY = (4 - shape.length) / 2.0f;

        // Render the hold piece
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    float blockX = holdX + col + offsetX;
                    float blockY = holdY -row + offsetY;
                    shapeRenderer.rect(blockX, blockY, 1, 1);
                }
            }
        }
        shapeRenderer.end();
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
        font.dispose();
        spriteBatch.dispose();
    }
}
