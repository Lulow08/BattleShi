package com.cuatrifasico.battleshi.view.board;

/**
 * Centralizes every geometric measurement used to build board and
 * ship shapes programmatically. Only geometry lives here: colors are
 * intentionally NOT defined as Java constants anymore. They live in
 * {@code styles.css} as regular JavaFX CSS classes, since fill colors
 * do not participate in any shape-construction math and are better
 * kept re-skinnable without recompiling.
 * <p>
 * Sizes/arcs stay in Java because they ARE consumed by arithmetic
 * when procedurally building shapes (cell positions, ship end-cap
 * unions). In particular, once an end cap is built via
 * {@link javafx.scene.shape.Shape#union}, the resulting node is a
 * generic {@link javafx.scene.shape.Shape} backed by a path — it no
 * longer exposes an {@code arcWidth}/{@code arcHeight} property, so
 * CSS could never have driven that rounding in the first place.
 */
public final class BoardTheme {

    private BoardTheme() {
        // Utility class, not meant to be instantiated.
    }

    // ---- Board geometry ----------------------------------------------
    /** Full logical size of one cell. Ships and the coordinate grid both use this directly (no gap). */
    public static final double CELL_SIZE = 42;

    /** Total visual gap perceived between two adjacent water cells (split as half-padding on each cell). */
    public static final double CELL_GAP = 6;

    /** Corner radius used for board cells, frigates, and aircraft carrier end caps. */
    public static final double CELL_ARC = 14;

    /** Corner radius used for destroyer/submarine end caps, producing a full semicircle ("capsule") look. */
    public static final double SHIP_CAPSULE_ARC = CELL_SIZE;

    /** Spacing (horizontal and vertical) between ship entries in the fleet tray. */
    public static final double TRAY_SHIP_SPACING = 10;

    /** Margin between the 10x10 grid block and the turn-indicator border around it. */
    public static final double TURN_INDICATOR_MARGIN = 4;

    // ---- Marker geometry ------------------------------------------------
    public static final double HIT_DOT_RADIUS = CELL_SIZE * 0.16;
    public static final double HIT_MARKER_RADIUS = CELL_SIZE * 0.27;
    public static final double MISS_ARM_LENGTH = CELL_SIZE * 0.16;
    public static final double MISS_STROKE_WIDTH = CELL_SIZE * 0.12;

    // ---- CSS style class names ------------------------------------------
    public static final String CLASS_BOARD_BACKGROUND = "board-background";
    public static final String CLASS_CELL_WATER = "cell-water";
    public static final String CLASS_CELL_HIGHLIGHT_VALID = "cell-highlight-valid";
    public static final String CLASS_CELL_HIGHLIGHT_INVALID = "cell-highlight-invalid";
    public static final String CLASS_SHIP_BODY = "ship-body";
    public static final String CLASS_SHIP_SUNK = "ship-sunk";
    public static final String CLASS_SHIP_SHADOW = "ship-shadow";
    public static final String CLASS_SHIP_HIT_DOT = "ship-hit-dot";
    public static final String CLASS_MARKER_HIT = "marker-hit";
    public static final String CLASS_MARKER_MISS = "marker-miss";
    public static final String CLASS_TURN_INDICATOR = "turn-indicator";
    public static final String CLASS_TURN_INDICATOR_ACTIVE = "turn-indicator-active";
}