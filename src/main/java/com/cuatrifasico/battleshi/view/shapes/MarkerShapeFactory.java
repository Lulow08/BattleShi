package com.cuatrifasico.battleshi.view.shapes;

import com.cuatrifasico.battleshi.view.board.BoardTheme;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * Builds the small overlay shapes drawn on top of a single cell to
 * represent a shot's outcome: a miss (thick rounded white X) or a
 * hit (solid red circle). Both are centered on a {@code CELL_SIZE} x
 * {@code CELL_SIZE} local coordinate box so callers only need to
 * translate the returned node to the target cell's top-left corner.
 * Colors come entirely from {@code styles.css} via style classes.
 */
public final class MarkerShapeFactory {

    private MarkerShapeFactory() {
        // Utility class, not meant to be instantiated.
    }

    /**
     * Creates the "water" / miss marker: a thick, rounded white X
     * centered in the cell. The overall footprint stays centered on
     * the cell like before; only the arm length was shortened so the
     * strokes read as a tighter mark rather than spanning the cell.
     *
     * @return A {@link Group} containing the two crossed lines.
     */
    public static Group createMissMarker() {
        double center = BoardTheme.CELL_SIZE / 2.0;
        double arm = BoardTheme.MISS_ARM_LENGTH;

        Line diagonalDown = new Line(center - arm, center - arm, center + arm, center + arm);
        Line diagonalUp = new Line(center - arm, center + arm, center + arm, center - arm);

        for (Line line : new Line[]{diagonalDown, diagonalUp}) {
            line.setStrokeWidth(BoardTheme.MISS_STROKE_WIDTH);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.getStyleClass().add(BoardTheme.CLASS_MARKER_MISS);
        }

        return new Group(diagonalDown, diagonalUp);
    }

    /**
     * Creates the "hit" marker: a solid red circle centered in the
     * cell, sized to fully cover the darker hit-point dot already
     * drawn on the underlying ship body.
     *
     * @return A {@link Circle} representing the hit marker.
     */
    public static Circle createHitMarker() {
        double center = BoardTheme.CELL_SIZE / 2.0;
        Circle marker = new Circle(center, center, BoardTheme.HIT_MARKER_RADIUS);
        marker.getStyleClass().add(BoardTheme.CLASS_MARKER_HIT);
        return marker;
    }
}