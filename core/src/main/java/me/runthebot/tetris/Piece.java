package me.runthebot.tetris;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Piece {
    private final Tetrimino type;
    private boolean[][] grid;
    private int x, y;

    public Piece(Tetrimino type) {
        this.type = type;
        this.grid = type.getShape();
        this.x = 3;
        this.y = 0;
    }

    public boolean move(int dx, int dy, Grid field) {
        int newX = x + dx;
        int newY = y + dy;
        if (!collides(newX, newY, grid, field)) {
            x = newX;
            y = newY;
            return true;
        }
        return false;
    }

    public void rotate(Grid field) {
        boolean[][] rotated = rotate90(grid);
        if (!collides(x, y, rotated, field)) {
            grid = rotated;
        }
    }

    private boolean collides(int tx, int ty, boolean[][] shape, Grid field) {
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[0].length; col++) {
                if (!shape[row][col]) continue;
                int gx = tx + col;
                int gy = ty + row;
                if (gx < 0 || gx >= field.getWidth() || gy < 0 || gy >= field.getHeight() || field.isOccupied(gx, gy)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean[][] rotate90(boolean[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        boolean[][] rotated = new boolean[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = input[i][j];
            }
        }
        return rotated;
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(getType().getColor());
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col]) {
                    int gx = x + col;
                    int gy = y + row;
                    renderer.rect(gx * GameScreen.BLOCK_SIZE, (GameScreen.GRID_HEIGHT - gy - 1) * GameScreen.BLOCK_SIZE,
                        GameScreen.BLOCK_SIZE, GameScreen.BLOCK_SIZE);
                }
            }
        }
    }
}
