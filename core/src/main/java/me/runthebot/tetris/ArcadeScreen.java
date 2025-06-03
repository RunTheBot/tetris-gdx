package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.*;

/**
 * Implements the "Arcade" game mode for Tetris.
 * Handles arcade-specific rules, scoring, and rendering.
 */
public class ArcadeScreen extends BaseGameScreen {
    // Game stats specific to Arcade mode
    private int linesCleared = 0;
    private int score = 0;
    private int level = 1;
    private long startTime;
    private long currentTime;
    private float currentSpeed = 0;
    private float maxSpeed = 0;
    private int highScore = 0;

    // Power up/down system
    private static final int POWER_SPAWN_CHANCE = 50; // % chance per second
    private static final long POWER_DURATION = 30000; // 30 seconds effect
    private static final long POWER_LIFETIME = 60000; // 60 seconds on board

    private enum PowerType {
        POWER_UP(Color.GREEN),
        POWER_DOWN(Color.RED);

        private final Color color;

        PowerType(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    private static class PowerItem {
        int x, y;
        PowerType type;
        long spawnTime;

        PowerItem(int x, int y, PowerType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.spawnTime = TimeUtils.millis();
        }

        boolean isExpired() {
            return TimeUtils.timeSinceMillis(spawnTime) > POWER_LIFETIME;
        }
    }

    private List<PowerItem> powerItems = new ArrayList<>();
    private Map<PowerType, Long> activePowers = new HashMap<>();
    private Random random = new Random();
    private long lastPowerSpawnCheck = 0;

    public ArcadeScreen(final Tetris game) {
        super(game);

        // Initialize stats tracking
        startTime = TimeUtils.millis();
        currentTime = 0;
        currentSpeed = 0;
        maxSpeed = 0;

        // Initialize gravity based on starting level
        updateGravity();
    }

    @Override
    public void render(float delta) {
        if (gameOver) {
            // Capture final stats
            long finalTime = TimeUtils.millis() - startTime;

            // Pass game stats to the game over screen
            game.setScreen(new GameOverScreen(game, "arcade", score, level, linesCleared,
                            finalTime, currentSpeed, maxSpeed, 0));
            return;
        }

        handleInput();
        update();
        updatePowers(); // Add this line to update powers

        // Update game stats
        currentTime = TimeUtils.millis() - startTime;
        if (currentTime > 0) {
            currentSpeed = (float) linesCleared / (currentTime / 1000.0f);
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
            }
        }

        // Update high score if current score is higher
        if (score > highScore) {
            highScore = score;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        shapeRenderer.setProjectionMatrix(game.camera.combined);

        // Render game elements
        grid.render(shapeRenderer);

        if (config.showGhostPiece) {
            // Render ghost piece with transparency
            ghostPiece.render(shapeRenderer, 0.3f);  // Pass alpha value for transparency
        }

        // Render power items
        renderPowerItems();
        currentPiece.render(shapeRenderer);

        // End shape rendering started in this method
        shapeRenderer.end();

        // Hold and Next pieces have their own begin/end calls
        renderHoldPiece();
        renderNextPiece();

        // Render UI including powers
        renderUI();
        collectPowers();
    }

    private void renderUI() {
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        // Format time as mm:ss.ms
        String timeString = String.format("%02d:%02d.%d",
                (currentTime / 60000),
                (currentTime / 1000) % 60,
                (currentTime / 100) % 10);

        // Display all arcade mode stats
        font.draw(spriteBatch, "ARCADE MODE", 20, Gdx.graphics.getHeight() - 20);
        font.draw(spriteBatch, "Score: " + score, 20, Gdx.graphics.getHeight() - 50);
        font.draw(spriteBatch, "Level: " + level, 20, Gdx.graphics.getHeight() - 80);
        font.draw(spriteBatch, "Lines: " + linesCleared, 20, Gdx.graphics.getHeight() - 110);
        font.draw(spriteBatch, "Time: " + timeString, 20, Gdx.graphics.getHeight() - 140);
        font.draw(spriteBatch, "Speed: " + String.format("%.2f", currentSpeed) + " lps", 20, Gdx.graphics.getHeight() - 170);
        font.draw(spriteBatch, "Max Speed: " + String.format("%.2f", maxSpeed) + " lps", 20, Gdx.graphics.getHeight() - 200);

        // High score if available
        if (highScore > 0) {
            font.draw(spriteBatch, "High Score: " + highScore, 20, Gdx.graphics.getHeight() - 230);
        }

        // Draw active powers
        int yPos = Gdx.graphics.getHeight() - 270;
        for (Map.Entry<PowerType, Long> power : activePowers.entrySet()) {
            long timeLeft = (power.getValue() - TimeUtils.millis()) / 1000;
            if (timeLeft <= 0) continue;

            if (power.getKey() == PowerType.POWER_UP) {
                font.setColor(PowerType.POWER_UP.getColor());
                font.draw(spriteBatch, "POWER UP: x2 ("+timeLeft+"s)", 20, yPos);
            } else {
                font.setColor(PowerType.POWER_DOWN.getColor());
                font.draw(spriteBatch, "POWER DOWN: x-1 ("+timeLeft+"s)", 20, yPos);
            }
            yPos -= 40;
        }

        spriteBatch.end();
    }

    private void renderPowerItems() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (PowerItem power : powerItems) {
            shapeRenderer.setColor(power.type.getColor());
            // Convert to rendering coordinates
            float renderX = power.x + Grid.CENTER_OFFSET;
            float renderY = (Tetris.GRID_HEIGHT - power.y - 1);
            shapeRenderer.rect(renderX, renderY, 1, 1);
        }
        shapeRenderer.end();
    }

    private void updatePowers() {
        long currentTime = TimeUtils.millis();

        // Check for power spawning
        if (currentTime - lastPowerSpawnCheck > 1000) {  // Check once per second
            lastPowerSpawnCheck = currentTime;

            // Random chance to spawn a power
            if (random.nextInt(100) < POWER_SPAWN_CHANCE) {
                spawnRandomPower();
            }
        }

        // Remove expired powers
        Iterator<PowerItem> iter = powerItems.iterator();
        while (iter.hasNext()) {
            PowerItem power = iter.next();
            if (power.isExpired()) {
                iter.remove();
            }
        }

        // Remove expired active effects
        activePowers.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }

    private void spawnRandomPower() {
        // Find a random empty cell on the grid
        List<int[]> emptyCells = new ArrayList<>();
        for (int x = 0; x < Tetris.GRID_WIDTH; x++) {
            for (int y = 0; y < Tetris.GRID_HEIGHT; y++) {
                if (!grid.isOccupied(x, y)) {
                    emptyCells.add(new int[]{x, y});
                }
            }
        }

        if (emptyCells.isEmpty()) return; // No empty cells

        // Select a random empty cell
        int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));

        // 50/50 chance for power up or down
        PowerType type = random.nextBoolean() ? PowerType.POWER_UP : PowerType.POWER_DOWN;

        // Create and add the power
        powerItems.add(new PowerItem(cell[0], cell[1], type));
    }

    public void placePiece(){

        collectPowers();

        grid.lockPiece(currentPiece);

        // Calculate line clears with power effects
        int lines = grid.checkAndClearLines();

        // Calculate base score for lines cleared
        int lineScore = 0;
        switch (lines) {
            case 1: lineScore = 40; break;
            case 2: lineScore = 100; break;
            case 3: lineScore = 300; break;
            case 4: lineScore = 1200; break;
        }

        // Apply level multiplier to score
        int scoreGain = lineScore * level;

        // Apply power effects
        if (isPowerActive(PowerType.POWER_UP)) {
            linesCleared += lines * 2; // Double line clears
            score += scoreGain * 2; // Double score
        } else if (isPowerActive(PowerType.POWER_DOWN)) {
            linesCleared -= lines; // Negative line clears
            // No score deduction, just no score gain
        } else {
            linesCleared += lines; // Normal line clears
            score += scoreGain; // Normal score gain
        }

        // Update speed tracking after each piece placement
        if (TimeUtils.millis() - startTime > 0) {
            currentSpeed = (float) linesCleared / ((TimeUtils.millis() - startTime) / 1000.0f);
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
            }
        }

        // Update high score if current score is higher
        if (score > highScore) {
            highScore = score;
        }

        // Level up every 10 lines
        level = (linesCleared / 10) + 1;

        // Update gravity based on level
        updateGravity();

        spawnNewPiece();
        canHold = true;
    }

    private void collectPowers() {
        Iterator<PowerItem> iter = powerItems.iterator();
        while (iter.hasNext()) {
            PowerItem power = iter.next();

            // Convert power position to game coordinates
            int powerGameX = (int)(power.x);
            int powerGameY = (int)(power.y);

            // Check if the power is colliding with any part of the piece
            if (currentPiece.contains(powerGameX, powerGameY)) {
                // Power collected!
                activatePower(power.type);
                iter.remove();
                System.out.println("Power collected at: " + powerGameX + "," + powerGameY);
            }
        }
    }

    private void activatePower(PowerType type) {
        // Set or extend power duration
        System.out.println("Activated power: " + type);
        activePowers.put(type, TimeUtils.millis() + POWER_DURATION);
    }

    private boolean isPowerActive(PowerType type) {
        Long endTime = activePowers.get(type);
        return endTime != null && endTime > TimeUtils.millis();
    }

    private void update() {
        if (gameOver) return; // Stop updates if game is over

        // Try to move the piece down due to gravity
        if (TimeUtils.timeSinceMillis(lastFallTime) >= (1000 / gravity)) { // Changed currentGravity to gravity
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
     */
    private void holdPiece() {
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

    private void updateGhostPiece() {
        // Create a fresh copy of the current piece to ensure correct shape/rotation
        ghostPiece = new Piece(currentPiece.getType());
        ghostPiece.setRotation(currentPiece.getRotation());
        ghostPiece.setPosition(currentPiece.getX(), currentPiece.getY());

        // Drop the ghost piece as far as it can go
        while (ghostPiece.move(0, 1, grid)) { }
    }

    private void handleInput() {
        if (gameOver) return; // Ignore input if game is over

        long currentTime = TimeUtils.millis();

        // Hold piece (configured keys)
        if (Gdx.input.isKeyJustPressed(config.KEY_HOLD) || Gdx.input.isKeyJustPressed(config.KEY_HOLD_ALT)) {
            holdPiece();
            return;
        }

        // Hard drop (configured key)
        if (Gdx.input.isKeyJustPressed(config.KEY_HARD_DROP)) {
            currentPiece.hardDrop(grid); // No need to store the return value
            placePiece();
            return;
        }

        // Soft drop (faster fall) - configured key
        if (Gdx.input.isKeyPressed(config.KEY_MOVE_DOWN)) {
            boolean moved = currentPiece.move(0, 1, grid);
            if (moved) {
                updateGhostPiece();
                // Reset lock delay when manually moving down (soft drop)
                if (lockDelayActive) {
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
                    if (lockDelayActive) {
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
                        if (lockDelayActive) {
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
                    if (lockDelayActive) {
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
                        if (lockDelayActive) {
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
                if (lockDelayActive) {
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
                if (lockDelayActive) {
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
                if (lockDelayActive) {
                    lockDelayStartTime = TimeUtils.millis();
                    lockResets++;
                }
            }
        }
    }

    /**
     * Updates gravity (fall speed) based on current level
     */
    private void updateGravity() {
        // Classic Tetris formula: gravity increases with level
        gravity = gravity + (level - 1) * 0.05f;
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
