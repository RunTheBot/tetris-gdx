package me.runthebot.tetris;

import com.badlogic.gdx.Input;

/**
 * Stores user-configurable game settings such as movement delays and UI options.
 * DAS_DELAY: Delayed Auto Shift (ms before piece starts moving when holding left/right)
 * ARR_DELAY: Auto Repeat Rate (ms between repeated moves when holding left/right)
 * showGhostPiece: Whether to render a preview of where the current piece will land
 */
public class GameConfig {
    public float DAS_DELAY = 170; // Delayed Auto Shift
    public float ARR_DELAY = 30; // Auto Repeat Rate
    public boolean showGhostPiece = true; // Renders the place where the piece would be if dropped

    // Key bindings with default values
    public int KEY_MOVE_LEFT = Input.Keys.LEFT;
    public int KEY_MOVE_RIGHT = Input.Keys.RIGHT;
    public int KEY_MOVE_DOWN = Input.Keys.DOWN;
    public int KEY_ROTATE_CW = Input.Keys.UP;
    public int KEY_ROTATE_CCW = Input.Keys.Z;
    public int KEY_ROTATE_180 = Input.Keys.A;
    public int KEY_HARD_DROP = Input.Keys.SPACE;
    public int KEY_HOLD = Input.Keys.C;
    public int KEY_HOLD_ALT = Input.Keys.SHIFT_LEFT; // Alternative key for hold (shift)
}
