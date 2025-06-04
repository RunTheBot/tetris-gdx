package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Implements the "Sprint" game mode where the player must clear a set number of lines as fast as possible.
 * Handles game logic, rendering, and user input for this mode.
 */
public class SprintScreen extends BaseGameScreen {
    // Sprint mode specific variables
    private int linesCleared = 0;
    private int targetLines = 40;
    private int linesLeft;
    private long startTime;
    private long currentTime;
    private float pace;
    private float currentSpeed;
    private float maxSpeed = 0;
    private int highScore = 0;
    private int level = 1;

    public SprintScreen(final Tetris game) {
        super(game);

        // Initialize sprint mode stats
        startTime = TimeUtils.millis();
        linesLeft = targetLines - linesCleared;
        pace = 0;
        currentSpeed = 0;
        gravity = 0.5f; // Fixed gravity for sprint mode
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

        // Check win condition
        if (linesCleared >= targetLines) {
            // Capture final time and pass stats to win screen
            long finalTime = TimeUtils.millis() - startTime;

            // Pass game type and stats to the win screen
            game.setScreen(new WinScreen(game, "sprint", 0, level, linesCleared, finalTime,
                         currentSpeed, maxSpeed, 0));
            return;
        }

        handleInput();
        update();

        // Update game stats
        currentTime = TimeUtils.millis() - startTime;
        if (currentTime > 0) {
            // Calculate pace in lines per minute
            pace = (float) linesCleared / (currentTime / 60000.0f);
            currentSpeed = (float) linesCleared / (currentTime / 1000.0f);
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
            }
        }

        // Update high score if current time is better than previous best
        if (highScore == 0 || currentTime < highScore) {
            highScore = (int) currentTime;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        shapeRenderer.setProjectionMatrix(game.camera.combined);

        // Render game elements
        grid.render(shapeRenderer);

        if (config.showGhostPiece) {
            // Render ghost piece with transparency
            ghostPiece.render(shapeRenderer, 0.3f);
        }

        currentPiece.render(shapeRenderer);
        shapeRenderer.end();

        // Hold and Next pieces have their own begin/end calls
        renderHoldPiece();
        renderNextPiece();

        // Render UI
        renderUI();
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

        // Calculate lines left to clear
        linesLeft = targetLines - linesCleared;

        // Display Sprint mode stats
        font.draw(spriteBatch, "SPRINT MODE", 20, Gdx.graphics.getHeight() - 20);
        font.draw(spriteBatch, "Lines Left: " + linesLeft + "/" + targetLines, 20, Gdx.graphics.getHeight() - 50);
        font.draw(spriteBatch, "Time: " + timeString, 20, Gdx.graphics.getHeight() - 80);

        if (currentTime > 0) {
            font.draw(spriteBatch, "Pace: " + String.format("%.2f lpm", pace), 20, Gdx.graphics.getHeight() - 110);
        }

        spriteBatch.end();
    }

    public void placePiece() {
        grid.lockPiece(currentPiece);

        // Check for line clears after locking the piece
        int lines = grid.checkAndClearLines();
        linesCleared += lines;

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
            }
        }

        // Handle lock delay
        if (lockDelayActive) {
            // Check if the lock delay time has passed or max resets reached
            if (TimeUtils.millis() - lockDelayStartTime > LOCK_DELAY || lockResets >= MAX_LOCK_RESETS) {
                // Before placing, make a final check if the piece can move down
                if (!currentPiece.move(0, 1, grid)) {
                    // Still cannot move down, so place the piece
                    placePiece();
                } else {
                    // Piece was able to move down
                    lastFallTime = TimeUtils.millis();
                    lockDelayActive = false;
                    lockResets = 0;
                    updateGhostPiece();
                }
            }
        }
    }

    private void updateGhostPiece() {
        // Create a fresh copy of the current piece to ensure correct shape/rotation
        ghostPiece = new Piece(currentPiece.getType());
        ghostPiece.setRotation(currentPiece.getRotation());
        ghostPiece.setPosition(currentPiece.getX(), currentPiece.getY());

        // Drop the ghost piece as far as it can go
        while (ghostPiece.move(0, 1, grid)) { }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
