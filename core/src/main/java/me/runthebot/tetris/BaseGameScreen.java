package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.*;

public abstract class BaseGameScreen implements Screen {
    protected final Tetris game;
    protected final ConfigManager configManager = ConfigManager.getInstance();
    protected final GameConfig config = configManager.getConfig();

    // Track key press times and last move times
    protected long leftPressTime = 0;
    protected long rightPressTime = 0;
    protected long lastLeftMoveTime = 0;
    protected long lastRightMoveTime = 0;

    protected final ShapeRenderer shapeRenderer;
    protected final SpriteBatch spriteBatch;
    protected final BitmapFont font;

    protected final Grid grid;
    protected Piece currentPiece;
    protected Piece ghostPiece;
    protected Piece holdPiece;
    protected boolean canHold = true;
    protected Queue<Tetrimino> nextPieces;

    protected long lastFallTime;
    protected float gravity = 1f;

    protected boolean gameOver = false;

    // Lock delay settings
    protected boolean lockDelayActive = false;
    protected long lockDelayStartTime = 0;
    protected final long LOCK_DELAY = 500;
    protected int lockResets = 0;
    protected final int MAX_LOCK_RESETS = 15;

    public BaseGameScreen(final Tetris game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        grid = new Grid(Tetris.GRID_WIDTH, Tetris.GRID_HEIGHT);
        nextPieces = new LinkedList<>();
        fillBag();
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();
    }

    protected void renderHoldPiece() {
        if (holdPiece == null) return;

        boolean[][] shape = holdPiece.getType().getShape();
        Color color = holdPiece.getType().getColor();

        // Hold position - on the left side of the grid
        float gridOffset = Grid.CENTER_OFFSET     ;
        float gridCenterX = gridOffset + Tetris.GRID_WIDTH / 2.0f; // Center of the grid with offset
        float holdX = gridCenterX - 10; // Position left of the grid
        float holdY = 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw a background rectangle for the hold piece area
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(holdX - 0.25f, 0.25f, 4.5f, 6);

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

    protected void renderNextPiece() {
        if (nextPieces.isEmpty()) return;

        // get the next piece
        Tetrimino nextPiece = nextPieces.peek();

        boolean[][] shape = nextPiece.getShape();
        Color color = nextPiece.getColor();

        // Next piece position - on the right side of the grid
        float gridOffset = Grid.CENTER_OFFSET;
        float gridCenterX = gridOffset + Tetris.GRID_WIDTH / 2.0f; // Center of the grid with offset
        float previewX = gridCenterX+6; // Position right of the grid
        // Position next piece at the top of the visible area
        float previewY = 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);

        // Draw a background rectangle for the next piece area
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(previewX - 0.25f, 0.25f, 4.5f, 6);


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
                    float blockY = previewY - row + offsetY;
                    shapeRenderer.rect(blockX, blockY, 1, 1);
                }
            }
        }
        shapeRenderer.end();
    }

    public abstract void placePiece();

    protected void holdPiece() {
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

    protected void handleInput() {
        if (gameOver) return; // Ignore input if game is over

        long currentTime = TimeUtils.millis();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, this));
            return;
        }

        // Hold piece (configured keys)
        if (Gdx.input.isKeyJustPressed(config.KEY_HOLD) || Gdx.input.isKeyJustPressed(config.KEY_HOLD_ALT)) {
            holdPiece();
            return;
        }

        // Hard drop (configured key)
        if (Gdx.input.isKeyJustPressed(config.KEY_HARD_DROP)) {
            currentPiece.hardDrop(grid);
            placePiece();
            return;
        }

        // Soft drop (faster fall) - configured key
        if (Gdx.input.isKeyPressed(config.KEY_MOVE_DOWN)) {
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

        // Left movement with DAS - configured key
        if (Gdx.input.isKeyPressed(config.KEY_MOVE_LEFT)) {
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

        // Right movement with DAS - configured key
        if (Gdx.input.isKeyPressed(config.KEY_MOVE_RIGHT)) {
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

        // Rotation - Clockwise (configured key)
        if (Gdx.input.isKeyJustPressed(config.KEY_ROTATE_CW)) {
            if (currentPiece.rotate(grid)) {
                updateGhostPiece();
                // Reset lock delay when piece is rotated
                if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                    lockDelayStartTime = TimeUtils.millis();
                    lockResets++;
                }
            }
        }

        // Rotation - Counterclockwise (configured key)
        if (Gdx.input.isKeyJustPressed(config.KEY_ROTATE_CCW)) {
            if (currentPiece.rotateCounterclockwise(grid)) {
                updateGhostPiece();
                // Reset lock delay when piece is rotated
                if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                    lockDelayStartTime = TimeUtils.millis();
                    lockResets++;
                }
            }
        }

        // Rotation - 180 degrees (configured key)
        if (Gdx.input.isKeyJustPressed(config.KEY_ROTATE_180)) {
            if (currentPiece.rotate180(grid)) {
                updateGhostPiece();
                // Reset lock delay when piece is rotated
                if (lockDelayActive && lockResets < MAX_LOCK_RESETS) {
                    lockDelayStartTime = TimeUtils.millis();
                    lockResets++;
                }
            }
        }
    }

    /**
     * Generates a new shuffled bag of all 7 Tetriminos and adds them to the queue
     */
    protected void fillBag() {
        List<Tetrimino> bag = new ArrayList<>(Arrays.asList(Tetrimino.values()));
        Collections.shuffle(bag);
        nextPieces.addAll(bag);
    }

    protected void spawnNewPiece() {
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

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
