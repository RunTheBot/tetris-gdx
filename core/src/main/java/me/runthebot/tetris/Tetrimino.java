package me.runthebot.tetris;
import lombok.Getter;
import com.badlogic.gdx.graphics.Color;

@Getter
public enum Tetrimino {
    I(Color.CYAN, new String[]{
        "....",
        "XXXX",
        "....",
        "...."
    }),
    O(Color.YELLOW, new String[]{
        "XX",
        "XX"
    }),
    T(Color.MAGENTA, new String[]{
        ".X.",
        "XXX",
        "..."
    }),
    S(Color.GREEN, new String[]{
        ".XX",
        "XX.",
        "..."
    }),
    Z(Color.RED, new String[]{
        "XX.",
        ".XX",
        "..."
    }),
    J(Color.BLUE, new String[]{
        "X..",
        "XXX",
        "..."
    }),
    L(Color.ORANGE, new String[]{
        "..X",
        "XXX",
        "..."
    });

    public final Color color;
    private final String[] shapeLines;
    private final boolean[][] shape;

    Tetrimino(Color color, String[] shapeLines) {
        this.color = color;
        this.shapeLines = shapeLines;
        this.shape = convertToBooleanGrid(shapeLines);
    }

    // grid of booleans to represent the grid blocks
    private boolean[][] convertToBooleanGrid(String[] lines) {
        int rows = lines.length;
        int cols = lines[0].length();
        boolean[][] grid = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            String row = lines[i];
            for (int j = 0; j < cols; j++) {
                grid[i][j] = row.charAt(j) == 'X';
            }
        }
        return grid;
    }

    // visually prints piece as a string
    public String toVisualString() {
        StringBuilder sb = new StringBuilder();
        for (String line : shapeLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}

