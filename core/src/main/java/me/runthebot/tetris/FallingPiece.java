package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;


/**
 * Represents a Tetrimino piece that falls in the background of the menu screen for animation.
 * Used for visual effects, not for gameplay logic.
 */
public class FallingPiece {
    private final Tetrimino type;
    private Vector2 position;
    private final Vector2 velocity;
    private final Color color;
    private float rotation;
    private final float rotationSpeed;
    private final float BLOCK_SIZE = 32;

    /**
     * Constructs a new FallingPiece for menu animation.
     * @param type The Tetrimino type
     * @param position The starting position
     * @param velocity The falling velocity
     */
    public FallingPiece(Tetrimino type, Vector2 position, Vector2 velocity) {
        this.type = type;
        this.position = position;
        this.velocity = velocity;
        this.color = type.getColor();
        this.rotation = 0;
        this.rotationSpeed = (float) (Math.random() * 50 - 25);
    }

    /**
     * Updates the position and rotation of the falling piece.
     *
     * @param delta the time delta
     */
    public void update(float delta) {
        position.add(0, velocity.y * delta);
        rotation += rotationSpeed * delta;

        // If the piece falls below the screen, reset its position to the top
        if (position.y < -BLOCK_SIZE * type.getShape().length) {
            position.y = Gdx.graphics.getHeight();
            position.x = Gdx.graphics.getWidth() / 2f + (float) (Math.random() * 200 - 100);
        }
    }

    /**
     * Renders the falling piece using the ShapeRenderer.
     * @param renderer The ShapeRenderer to use
     */
    public void render(ShapeRenderer renderer) {
        renderer.setColor(color);
        boolean[][] shape = type.getShape();
        renderer.identity(); // reset the piece transformation
        renderer.translate(position.x, position.y, 0);
        renderer.rotate(0, 0, 1, rotation);

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    float x = col * BLOCK_SIZE * 1.5f;
                    float y = -row * BLOCK_SIZE * 1.5f;
                    renderer.rect(x, y, BLOCK_SIZE * 1.5f, BLOCK_SIZE * 1.5f);
                }
            }
        }
        renderer.identity();
    }
}
