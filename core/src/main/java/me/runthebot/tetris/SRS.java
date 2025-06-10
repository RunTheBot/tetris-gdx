package me.runthebot.tetris;

/**
 * Super Rotation System (SRS) implementation for Tetris.
 * Contains kick tables and logic for piece rotation with wall kicks.
 * Based on "TGM-ACE SRS study" (http://www.the-shell.net/img/srs_study.html)
 * Suggested By Classmate
 */
public class SRS {

    // Block position data for each piece type, rotation state, and block
    // Format: [piece_type][rotation_state][block_index]
    private static final int[][][] SRS_BLOCK_DATA_X = {
        // I piece (index 0)
        {
            {0, 1, 2, 3},   // rotation 0
            {2, 2, 2, 2},   // rotation 1
            {3, 2, 1, 0},   // rotation 2
            {1, 1, 1, 1}    // rotation 3
        },
        // O piece (index 1)
        {
            {2, 2, 1, 0},   // rotation 0
            {2, 1, 1, 1},   // rotation 1
            {0, 0, 1, 2},   // rotation 2
            {0, 1, 1, 1}    // rotation 3
        },
        // T piece (index 2)
        {
            {1, 2, 2, 1},   // rotation 0
            {2, 2, 1, 1},   // rotation 1
            {2, 1, 1, 2},   // rotation 2
            {1, 1, 2, 2}    // rotation 3
        },
        // S piece (index 3)
        {
            {0, 1, 1, 2},   // rotation 0
            {2, 2, 1, 1},   // rotation 1
            {2, 1, 1, 0},   // rotation 2
            {0, 0, 1, 1}    // rotation 3
        },
        // Z piece (index 4)
        {
            {1, 0, 1, 2},   // rotation 0
            {2, 1, 1, 1},   // rotation 1
            {1, 2, 1, 0},   // rotation 2
            {0, 1, 1, 1}    // rotation 3
        },
        // J piece (index 5)
        {
            {0, 0, 1, 2},   // rotation 0
            {2, 1, 1, 1},   // rotation 1
            {2, 2, 1, 0},   // rotation 2
            {0, 1, 1, 1}    // rotation 3
        },
        // L piece (index 6)
        {
            {2, 1, 1, 0},   // rotation 0
            {2, 2, 1, 1},   // rotation 1
            {0, 1, 1, 2},   // rotation 2
            {0, 0, 1, 1}    // rotation 3
        }
    };

    private static final int[][][] SRS_BLOCK_DATA_Y = {
        // I piece (index 0)
        {
            {1, 1, 1, 1},   // rotation 0
            {0, 1, 2, 3},   // rotation 1
            {2, 2, 2, 2},   // rotation 2
            {3, 2, 1, 0}    // rotation 3
        },
        // O piece (index 1)
        {
            {0, 1, 1, 1},   // rotation 0
            {2, 2, 1, 0},   // rotation 1
            {2, 1, 1, 1},   // rotation 2
            {0, 0, 1, 2}    // rotation 3
        },
        // T piece (index 2)
        {
            {0, 0, 1, 1},   // rotation 0
            {0, 1, 1, 0},   // rotation 1
            {1, 1, 0, 0},   // rotation 2
            {1, 0, 0, 1}    // rotation 3
        },
        // S piece (index 3)
        {
            {0, 0, 1, 1},   // rotation 0
            {0, 1, 1, 2},   // rotation 1
            {2, 2, 1, 1},   // rotation 2
            {2, 1, 1, 0}    // rotation 3
        },
        // Z piece (index 4)
        {
            {0, 1, 1, 1},   // rotation 0
            {1, 0, 1, 2},   // rotation 1
            {2, 1, 1, 1},   // rotation 2
            {1, 2, 1, 0}    // rotation 3
        },
        // J piece (index 5)
        {
            {0, 1, 1, 1},   // rotation 0
            {0, 0, 1, 2},   // rotation 1
            {2, 1, 1, 1},   // rotation 2
            {2, 2, 1, 0}    // rotation 3
        },
        // L piece (index 6)
        {
            {0, 0, 1, 1},   // rotation 0
            {2, 1, 1, 0},   // rotation 1
            {2, 2, 1, 1},   // rotation 2
            {0, 1, 1, 2}    // rotation 3
        }
    };

    // Kick tables for non-I pieces
    // Format: [rotation_from][kick_test][x_offset, y_offset]
    private static final int[][][] OTHER_BLOCK_KICK_TABLE = {
        // From rotation 0
        {
            {0, 0},   // Test 1: no offset
            {-1, 0},  // Test 2: left 1
            {-1, 1},  // Test 3: left 1, up 1
            {0, -2},  // Test 4: down 2
            {-1, -2}  // Test 5: left 1, down 2
        },
        // From rotation 1
        {
            {0, 0},   // Test 1: no offset
            {1, 0},   // Test 2: right 1
            {1, -1},  // Test 3: right 1, down 1
            {0, 2},   // Test 4: up 2
            {1, 2}    // Test 5: right 1, up 2
        },
        // From rotation 2
        {
            {0, 0},   // Test 1: no offset
            {1, 0},   // Test 2: right 1
            {1, 1},   // Test 3: right 1, up 1
            {0, -2},  // Test 4: down 2
            {1, -2}   // Test 5: right 1, down 2
        },
        // From rotation 3
        {
            {0, 0},   // Test 1: no offset
            {-1, 0},  // Test 2: left 1
            {-1, -1}, // Test 3: left 1, down 1
            {0, 2},   // Test 4: up 2
            {-1, 2}   // Test 5: left 1, up 2
        }
    };

    // Kick tables for I pieces
    private static final int[][][] I_BLOCK_KICK_TABLE = {
        // From rotation 0
        {
            {0, 0},   // Test 1: no offset
            {-2, 0},  // Test 2: left 2
            {1, 0},   // Test 3: right 1
            {-2, -1}, // Test 4: left 2, down 1
            {1, 2}    // Test 5: right 1, up 2
        },
        // From rotation 1
        {
            {0, 0},   // Test 1: no offset
            {-1, 0},  // Test 2: left 1
            {2, 0},   // Test 3: right 2
            {2, 1},   // Test 4: right 2, up 1
            {-1, -2}  // Test 5: left 1, down 2
        },
        // From rotation 2
        {
            {0, 0},   // Test 1: no offset
            {2, 0},   // Test 2: right 2
            {-1, 0},  // Test 3: left 1
            {-1, 1},  // Test 4: left 1, up 1
            {2, -1}   // Test 5: right 2, down 1
        },
        // From rotation 3
        {
            {0, 0},   // Test 1: no offset
            {1, 0},   // Test 2: right 1
            {-2, 0},  // Test 3: left 2
            {1, -2},  // Test 4: right 1, down 2
            {-2, 1}   // Test 5: left 2, up 1
        }
    };

    // 180-degree rotation kick tables for non-I pieces
    private static final int[][][] OTHER_BLOCK_180_KICK_TABLE = {
        // From rotation 0 to 2
        {
            {0, 0}, {1, 0}, {2, 0}, {1, 1}, {2, 1}, {-1, 0}, {-2, 0},
            {-1, 1}, {-2, 1}, {0, -1}, {3, 0}, {-3, 0}
        },
        // From rotation 1 to 3
        {
            {0, 0}, {0, 1}, {0, 2}, {-1, 1}, {-1, 2}, {0, -1}, {0, -2},
            {-1, -1}, {-1, -2}, {1, 0}, {0, 3}, {0, -3}
        },
        // From rotation 2 to 0
        {
            {0, 0}, {-1, 0}, {-2, 0}, {-1, -1}, {-2, -1}, {1, 0}, {2, 0},
            {1, -1}, {2, -1}, {0, 1}, {-3, 0}, {3, 0}
        },
        // From rotation 3 to 1
        {
            {0, 0}, {0, 1}, {0, 2}, {1, 1}, {1, 2}, {0, -1}, {0, -2},
            {1, -1}, {1, -2}, {-1, 0}, {0, 3}, {0, -3}
        }
    };

    // 180-degree rotation kick tables for I pieces
    private static final int[][][] I_BLOCK_180_KICK_TABLE = {
        // From rotation 0 to 2
        {
            {0, 0}, {-1, 0}, {-2, 0}, {1, 0}, {2, 0}, {0, 1}
        },
        // From rotation 1 to 3
        {
            {0, 0}, {0, 1}, {0, 2}, {0, -1}, {0, -2}, {-1, 0}
        },
        // From rotation 2 to 0
        {
            {0, 0}, {1, 0}, {2, 0}, {-1, 0}, {-2, 0}, {0, -1}
        },
        // From rotation 3 to 1
        {
            {0, 0}, {0, 1}, {0, 2}, {0, -1}, {0, -2}, {1, 0}
        }
    };

    /**
     * Gets the piece type index for kick table lookups.
     * @param type The Tetrimino type.
     * @return The index corresponding to the piece type.
     */
    private static int getPieceTypeIndex(Tetrimino type) {
        switch (type) {
            case I: return 0;
            case O: return 1;
            case T: return 2;
            case S: return 3;
            case Z: return 4;
            case J: return 5;
            case L: return 6;
            default: return 2; // Default to T piece
        }
    }

    /**
     * Attempts to rotate a piece using SRS kick system.
     * @param piece The piece to rotate.
     * @param grid The game grid to check collisions against.
     * @param clockwise True for clockwise rotation, false for counterclockwise.
     * @return True if rotation was successful, false otherwise.
     */
    public static boolean attemptRotation(Piece piece, Grid grid, boolean clockwise) {
        int currentRotation = piece.getRotation();
        int newRotation = clockwise ? (currentRotation + 1) % 4 : (currentRotation + 3) % 4;

        // Get the rotated shape
        boolean[][] rotatedShape = piece.getType().getShape(newRotation);

        // Determine which kick table to use
        boolean isIPiece = piece.getType() == Tetrimino.I;
        int[][][] kickTable = isIPiece ? I_BLOCK_KICK_TABLE : OTHER_BLOCK_KICK_TABLE;

        // Try each kick test
        int[][] kicks = kickTable[currentRotation];
        for (int[] kick : kicks) {
            int testX = piece.getX() + kick[0];
            int testY = piece.getY() + kick[1];

            if (!collides(testX, testY, rotatedShape, grid)) {
                // Successful kick - apply the rotation
                piece.setPosition(testX, testY);
                piece.setRotation(newRotation);
                piece.setGrid(rotatedShape);
                return true;
            }
        }

        return false; // All kick tests failed
    }

    /**
     * Attempts a 180-degree rotation using SRS kick system.
     * @param piece The piece to rotate.
     * @param grid The game grid to check collisions against.
     * @return True if rotation was successful, false otherwise.
     */
    public static boolean attempt180Rotation(Piece piece, Grid grid) {
        int currentRotation = piece.getRotation();
        int newRotation = (currentRotation + 2) % 4;

        // Get the rotated shape
        boolean[][] rotatedShape = piece.getType().getShape(newRotation);

        // Determine which kick table to use
        boolean isIPiece = piece.getType() == Tetrimino.I;
        int[][][] kickTable = isIPiece ? I_BLOCK_180_KICK_TABLE : OTHER_BLOCK_180_KICK_TABLE;

        // Try each kick test
        int[][] kicks = kickTable[currentRotation];
        for (int[] kick : kicks) {
            int testX = piece.getX() + kick[0];
            int testY = piece.getY() + kick[1];

            if (!collides(testX, testY, rotatedShape, grid)) {
                // Successful kick - apply the rotation
                piece.setPosition(testX, testY);
                piece.setRotation(newRotation);
                piece.setGrid(rotatedShape);
                return true;
            }
        }

        return false; // All kick tests failed
    }

    /**
     * Check if the block collides with the grid or other blocks.
     * @param tx Test X position.
     * @param ty Test Y position.
     * @param shape The shape to test.
     * @param field The grid to test against.
     * @return true if the block collides, else false.
     */
    private static boolean collides(int tx, int ty, boolean[][] shape, Grid field) {
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
}
