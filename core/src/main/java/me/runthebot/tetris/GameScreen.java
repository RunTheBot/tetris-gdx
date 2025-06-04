package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Implements the "Marathon" or main game mode for Tetris.
 * Handles game logic, rendering, and user input for the main gameplay loop.
 */
public class GameScreen extends BaseGameScreen {
    // Game stats tracking specific to Marathon mode
    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;
    private long startTime;
    private long currentTime;
    private float currentSpeed = 0;
    private float maxSpeed = 0;
    private int highScore = 0;

    public GameScreen(final Tetris game) {
        super(game);

        // Initialize stats tracking
        startTime = TimeUtils.millis();
        currentTime = 0;

        // Initialize gravity based on starting level
        updateGravity();
    }

    @Override
    public void render(float delta) {
        if (gameOver) {
            // Pass game stats to the game over screen
            game.setScreen(new GameOverScreen(game, "classic", score, level, linesCleared,
                          currentTime, currentSpeed, maxSpeed, 0));
            return;
        }

        handleInput();
        update();

        // Update game stats
        currentTime = TimeUtils.millis() - startTime;
        if (currentTime > 0) {
            currentSpeed = (float) linesCleared / (currentTime / 1000.0f);
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
            }
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

        // Render score, level, and lines cleared
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

        // Display all marathon mode stats
        font.draw(spriteBatch, "MARATHON MODE", 20, Gdx.graphics.getHeight() - 20);
        font.draw(spriteBatch, "Score: " + score, 20, Gdx.graphics.getHeight() - 50);
        font.draw(spriteBatch, "Level: " + level, 20, Gdx.graphics.getHeight() - 80);
        font.draw(spriteBatch, "Lines: " + linesCleared, 20, Gdx.graphics.getHeight() - 110);
        font.draw(spriteBatch, "Time: " + timeString, 20, Gdx.graphics.getHeight() - 140);
        font.draw(spriteBatch, "Speed: " + String.format("%.2f", currentSpeed) + " lps", 20, Gdx.graphics.getHeight() - 170);
        font.draw(spriteBatch, "Max Speed: " + String.format("%.2f", maxSpeed) + " lps", 20, Gdx.graphics.getHeight() - 200);

        // Display gravity
        font.draw(spriteBatch, "Gravity: " + String.format("%.2f", gravity), 20, Gdx.graphics.getHeight() - 230);

        // Display high score if available
        if (highScore > 0) {
            font.draw(spriteBatch, "High Score: " + highScore, 20, Gdx.graphics.getHeight() - 260);
        }

        spriteBatch.end();
    }

    public void placePiece() {
        grid.lockPiece(currentPiece);

        // Check for line clears after locking the piece
        int lines = grid.checkAndClearLines();

        // Calculate score based on number of lines cleared
        int lineScore = 0;
        switch (lines) {
            case 1: lineScore = 40; break;
            case 2: lineScore = 100; break;
            case 3: lineScore = 300; break;
            case 4: lineScore = 1200; break;
        }

        // Apply level multiplier
        score += lineScore * level;
        linesCleared += lines;

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

        // Update level (every 10 lines cleared)
        level = (linesCleared / 10) + 1;

        // Update gravity based on new level
        updateGravity();

        // Reset lock delay mechanism as piece is now placed
        lockDelayActive = false;
        lockResets = 0;

        spawnNewPiece();
        canHold = true; // Reset the hold flag after placing a piece
    }

    private void updateGravity() {
        // Classic Tetris formula: gravity increases with level
        gravity = gravity + (level - 1) * 0.05f;
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
