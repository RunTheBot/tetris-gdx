package me.runthebot.tetris;

import com.badlogic.gdx.Input;

/**
 * Stores user-configurable game settings such as movement delays and UI options.
 */
public class GameConfig {
    /**
     * Delayed Auto Shift (ms before piece starts moving when holding left/right)
     */
    public float DAS_DELAY = 170;
    /**
     * Auto Repeat Rate (ms between repeated moves when holding left/right)
     */
    public float ARR_DELAY = 30;
    /**
     * Whether to render a preview of where the current piece will land
     */
    public boolean showGhostPiece = true;

    /**
     * Key binding for moving the piece to the left. Default: Left Arrow.
     */
    public int KEY_MOVE_LEFT = Input.Keys.LEFT;
    /**
     * Key binding for moving the piece to the right. Default: Right Arrow.
     */
    public int KEY_MOVE_RIGHT = Input.Keys.RIGHT;
    /**
     * Key binding for moving the piece down (soft drop). Default: Down Arrow.
     */
    public int KEY_MOVE_DOWN = Input.Keys.DOWN;
    /**
     * Key binding for rotating the piece clockwise. Default: Up Arrow.
     */
    public int KEY_ROTATE_CW = Input.Keys.UP;
    /**
     * Key binding for rotating the piece counter-clockwise. Default: Z.
     */
    public int KEY_ROTATE_CCW = Input.Keys.Z;
    /**
     * Key binding for rotating the piece 180 degrees. Default: A.
     */
    public int KEY_ROTATE_180 = Input.Keys.A;
    /**
     * Key binding for hard dropping the piece. Default: Spacebar.
     */
    public int KEY_HARD_DROP = Input.Keys.SPACE;
    /**
     * Key binding for holding the current piece. Default: C.
     */
    public int KEY_HOLD = Input.Keys.C;
    /**
     * Alternative key for hold (shift). Default: Left Shift.
     */
    public int KEY_HOLD_ALT = Input.Keys.SHIFT_LEFT;
}
