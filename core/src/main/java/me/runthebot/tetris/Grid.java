package me.runthebot.tetris;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Getter;

public class Grid {
    @Getter
    private final int width;
    @Getter
    private final int height;
    private final boolean[][] cells;
    private final Color[][] colors;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
        this.colors = new Color[height][width];
    }

    public boolean isOccupied(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return cells[y][x];
    }

    public void lockPiece(Piece piece) {
        boolean[][] shape = piece.getGrid();
        Color color = piece.getType().getColor();
        int px = piece.getX();
        int py = piece.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[0].length; col++) {
                if (shape[row][col]) {
                    int gx = px + col;
                    int gy = py + row;
                    if (gx >= 0 && gx < width && gy >= 0 && gy < height) {
                        cells[gy][gx] = true;
                        colors[gy][gx] = color;
                    }
                }
            }
        }
    }

    /**
     * Checks for filled lines and clears them.
     *
     * @return The number of lines cleared
     */
    public int checkAndClearLines() {
        int linesCleared = 0;

        // Check each row from bottom to top
        for (int y = height - 1; y >= 0; y--) {
            // Check if the line is full
            boolean lineFull = true;
            for (int x = 0; x < width; x++) {
                if (!cells[y][x]) {
                    lineFull = false;
                    break;
                }
            }

            if (lineFull) {
                // Clear the line
                clearLine(y);
                linesCleared++;
                // We need to recheck this row since everything above moved down
                y++;
            }
        }

        return linesCleared;
    }

    /**
     * Clears a line and moves all lines above it down.
     *
     * @param lineY The y-coordinate of the line to clear
     */
    private void clearLine(int lineY) {
        // Move all lines above down
        for (int y = lineY; y > 0; y--) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = cells[y-1][x];
                colors[y][x] = colors[y-1][x];
            }
        }

        // Clear the top line
        for (int x = 0; x < width; x++) {
            cells[0][x] = false;
            colors[0][x] = null;
        }
    }

    public void render(ShapeRenderer renderer) {
        // Draw grid lines
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.DARK_GRAY);
        for (int y = 0; y <= height; y++) {
            renderer.line(0, y * GameScreen.BLOCK_SIZE,
                width * GameScreen.BLOCK_SIZE, y * GameScreen.BLOCK_SIZE);
        }
        for (int x = 0; x <= width; x++) {
            renderer.line(x * GameScreen.BLOCK_SIZE, 0,
                x * GameScreen.BLOCK_SIZE, height * GameScreen.BLOCK_SIZE);
        }
        renderer.end();

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cells[y][x]) {
                    Color c = colors[y][x] != null ? colors[y][x] : Color.WHITE;
                    renderer.setColor(c);
                    renderer.rect(x * GameScreen.BLOCK_SIZE, (GameScreen.GRID_HEIGHT - y - 1) * GameScreen.BLOCK_SIZE,
                        GameScreen.BLOCK_SIZE, GameScreen.BLOCK_SIZE);
                }
            }
        }
        renderer.end();
    }

}
