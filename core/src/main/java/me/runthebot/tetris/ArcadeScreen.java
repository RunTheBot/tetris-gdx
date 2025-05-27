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

public class ArcadeScreen implements Screen {
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
    private float gravity = 1f; // Tiles per second

    private boolean gameOver = false; // Track game over state

    // Lock delay variables
    private boolean lockDelayActive = false;
    private long lockDelayStartTime = 0;
    private final long LOCK_DELAY = 500; // Lock delay in milliseconds

    // Game stats
    private int linesCleared = 0; // Track lines cleared
    private int score = 0; // Track player's score
    private int level = 1; // Track current level
    private long startTime; // When the game started
    private long currentTime; // Current game time
    private float currentSpeed = 0; // Current pieces per second
    private float maxSpeed = 0; // Maximum speed achieved
    private int highScore = 0; // High score (points)
    
    private final BitmapFont font;
    private final SpriteBatch spriteBatch;

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
        this.game = game;

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        spriteBatch = new SpriteBatch();
        grid = new Grid(Tetris.GRID_WIDTH, Tetris.GRID_HEIGHT);
        nextPieces = new LinkedList<>();
        fillBag(); // Initialize with first bag
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();
        
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
        if (gameOver) return;

        // Check if the piece can move down
        boolean canMoveDown = currentPiece.move(0, 1, grid);
        
        if (canMoveDown) {
            // Reset lock delay if piece is moved successfully
            lockDelayActive = false;
            lastFallTime = TimeUtils.millis();
            updateGhostPiece();
            
            // Move the piece back up
            currentPiece.move(0, -1, grid);
        } else if (!lockDelayActive) {
            // Activate lock delay when piece can't move down
            lockDelayActive = true;
            lockDelayStartTime = TimeUtils.millis();
        }
        
        // Apply gravity
        if (TimeUtils.timeSinceMillis(lastFallTime) > 1000 / gravity) {
            boolean moved = currentPiece.move(0, 1, grid);
            if (!moved && !lockDelayActive) {
                // If the piece can't move down and lock delay isn't active, activate it
                lockDelayActive = true;
                lockDelayStartTime = TimeUtils.millis();
            }
            lastFallTime = TimeUtils.millis();
        }

        // Handle lock delay
        if (lockDelayActive) {
            // Check if the lock delay time has passed
            if (TimeUtils.millis() - lockDelayStartTime > LOCK_DELAY) {
                lockDelayActive = false;
                // Place the piece when lock delay expires
                placePiece();
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
                    float blockY = holdY + row + offsetY;
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
                    float blockY = previewY + row + offsetY;
                    shapeRenderer.rect(blockX, blockY, 1, 1);
                }
            }
        }
        shapeRenderer.end();
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
                if (elapsedSincePress > config.DAS_DELAY && elapsedSinceLastMove >= config.ARR_DELAY) {
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
                if (elapsedSincePress > config.DAS_DELAY && elapsedSinceLastMove >= config.ARR_DELAY) {
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

        // Hold piece (C key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            holdPiece();
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
