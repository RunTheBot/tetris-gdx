package me.runthebot.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;


public class FallingPiece {
    private final Tetrimino type;
    private Vector2 position;
    private final Vector2 velocity;
    private final Color color;

    public FallingPiece(Tetrimino type, Vector2 position, Vector2 velocity) {
        this.type = type;
        this.position = position;
        this.velocity = velocity;
        this.color = type.getColor();
    }

    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);

        if (position.y < -Tetris.BLOCK_SIZE * type.getShape().length) {
            position.y = Gdx.graphics.getHeight();
            position.x = (float) Math.random() * (Gdx.graphics.getWidth() - Tetris.BLOCK_SIZE * type.getShape()[0].length);
        }
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(color);
        boolean[][] shape = type.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    float x = position.x + col * Tetris.BLOCK_SIZE;
                    float y = position.y - row * Tetris.BLOCK_SIZE;
                    renderer.rect(x, y, Tetris.BLOCK_SIZE, Tetris.BLOCK_SIZE);
                }
            }
        }
    }
}
