package me.runthebot.tetris;

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
}
