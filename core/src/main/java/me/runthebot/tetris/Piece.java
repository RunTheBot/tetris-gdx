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
    // Position getters
    private int x, y;
    // Rotation getter and setter
    private int rotation;

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

    public boolean rotate(Grid field) {
        boolean[][] rotated = rotate90(grid);
        if (!collides(x, y, rotated, field)) {
            grid = rotated;
            rotation = (rotation + 1) % 4;
            return true;
        } else {
            // TODO: SRS+ kicks
            return false;
        }
    }

    /**
     * Drops the piece as far down as possible.
     *
     * @param grid The grid to check for collisions
     * @return The number of rows the piece was dropped
     */
    public int hardDrop(Grid grid) {
        int rowsDropped = 0;
        while (move(0, 1, grid)) {
            rowsDropped++;
        }
        return rowsDropped;
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
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(getType().getColor());
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col]) {
                    int gx = x + col;
                    int gy = y + row;
                    renderer.rect(gx * Tetris.BLOCK_SIZE, (Tetris.GRID_HEIGHT - gy - 1) * Tetris.BLOCK_SIZE,
                        Tetris.BLOCK_SIZE, Tetris.BLOCK_SIZE);
                }
            }
        }
        renderer.end();
    }

    // Method to render with transparency
    public void render(ShapeRenderer renderer, float alpha) {
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        // Use white/gray color for ghost piece instead of transparent version of piece color
        Color ghostColor = new Color(0.8f, 0.8f, 0.8f, alpha);
        renderer.setColor(ghostColor);

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col]) {
                    int gx = x + col;
                    int gy = y + row;
                    renderer.rect(gx * Tetris.BLOCK_SIZE,
                                 (Tetris.GRID_HEIGHT - gy - 1) * Tetris.BLOCK_SIZE,
                                 Tetris.BLOCK_SIZE,
                                 Tetris.BLOCK_SIZE);
                }
            }
        }
        renderer.end();
    }

    // Position setter
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setRotation(int rotation) {
        // Create a copy of the original shape
        boolean[][] newGrid = type.getShape();

        // Apply rotations to get to the desired rotation state
        for (int i = 0; i < rotation; i++) {
            newGrid = rotate90(newGrid);
        }

        this.grid = newGrid;
        this.rotation = rotation;
    }
}
