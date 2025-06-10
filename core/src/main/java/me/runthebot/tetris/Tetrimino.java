package me.runthebot.tetris;
import lombok.Getter;
import com.badlogic.gdx.graphics.Color;

/**
 * Enum representing all 7 Tetrimino types, their colors, and their shapes.
 * Provides methods for shape rotation and visual representation.
 */
@Getter
public enum Tetrimino {
    /** Represents the I Tetrimino. */
    I(Color.CYAN, new String[]{
        "....",
        "XXXX",
        "....",
        "...."
    }),
    /** Represents the O Tetrimino. */
    O(Color.YELLOW, new String[]{
        "XX",
        "XX"
    }),
    /** Represents the T Tetrimino. */
    T(Color.MAGENTA, new String[]{
        ".X.",
        "XXX",
        "..."
    }),
    /** Represents the S Tetrimino. */
    S(Color.GREEN, new String[]{
        ".XX",
        "XX.",
        "..."
    }),
    /** Represents the Z Tetrimino. */
    Z(Color.RED, new String[]{
        "XX.",
        ".XX",
        "..."
    }),
    /** Represents the J Tetrimino. */
    J(Color.BLUE, new String[]{
        "X..",
        "XXX",
        "..."
    }),
    /** Represents the L Tetrimino. */
    L(Color.ORANGE, new String[]{
        "..X",
        "XXX",
        "..."
    });

    public final Color color;
    private final String[] shapeLines;
    private final boolean[][] shape;

    /**
     * Constructs a Tetrimino with the given color and shape definition.
     * @param color The color of the piece
     * @param shapeLines The string array representing the shape
     */
    Tetrimino(Color color, String[] shapeLines) {
        this.color = color;
        this.shapeLines = shapeLines;
        this.shape = convertToBooleanGrid(shapeLines);
    }

    /**
     * Converts a string array to a boolean grid for shape representation.
     * 'X' characters in the string array are converted to true in the boolean grid.
     * All other characters are converted to false.
     * @param lines The string array representing the shape
     * @return The boolean grid representing the shape
     */
    private boolean[][] convertToBooleanGrid(String[] lines) {
        int rows = lines.length;
        int cols = lines[0].length();
        boolean[][] grid = new boolean[rows][cols];
        // Iterate over each row
        for (int i = 0; i < rows; i++) {
            String row = lines[i];
            // Iterate over each column in the row
            for (int j = 0; j < cols; j++) {
                grid[i][j] = row.charAt(j) == 'X';
            }
        }
        return grid;
    }

    /**
     * Returns a visual string representation of the piece.
     */
    public String toVisualString() {
        StringBuilder sb = new StringBuilder();
        for (String line : shapeLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns a deep copy of the shape grid to avoid mutation.
     */
    public boolean[][] getShape() {
        int rows = shape.length;
        int cols = shape[0].length;
        boolean[][] copy = new boolean[rows][cols];
        // Iterate over each row
        for (int i = 0; i < rows; i++)
            System.arraycopy(shape[i], 0, copy[i], 0, cols);
        return copy;
    }

    /**
     * Returns the shape grid rotated by the given number of 90-degree steps.
     * @param rotation Number of 90-degree rotations
     * @return Rotated shape grid
     */
    public boolean[][] getShape(int rotation) {
        boolean[][] result = getShape();
        for (int i = 0; i < rotation % 4; i++) {
            result = rotate90(result);
        }
        return result;
    }

    /**
     * Rotates a boolean grid 90 degrees clockwise.
     * @param input The boolean grid to rotate
     * @return The rotated boolean grid
     */
    private boolean[][] rotate90(boolean[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        boolean[][] rotated = new boolean[cols][rows];
        // Iterate over each row
        for (int i = 0; i < rows; i++) {
            // Iterate over each column in the row
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = input[i][j];
            }
        }
        return rotated;
    }
}

