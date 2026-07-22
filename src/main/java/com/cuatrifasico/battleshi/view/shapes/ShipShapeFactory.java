package com.cuatrifasico.battleshi.view.shapes;

import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import com.cuatrifasico.battleshi.view.board.BoardTheme;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

/**
 * Builds the JavaFX {@link Group} representing a ship's visual body.
 * <p>
 * Every ship is composed as a sequence of individual, cell-sized
 * pieces (one {@link Shape} per occupied cell) laid out at exact
 * multiples of {@link BoardTheme#CELL_SIZE}, rather than as a single
 * stretched rectangle. This guarantees the ship aligns perfectly
 * with the board grid regardless of length, and lets each end piece
 * be rounded only on its outward-facing corners while staying sharp
 * on the corners that connect to the next segment:
 * <ul>
 *     <li>Frigate (1 cell): a single square, fully rounded on all
 *     four corners using {@link BoardTheme#CELL_ARC} (same radius as
 *     board cells).</li>
 *     <li>Destroyer (2 cells) / Submarine (3 cells): two end caps
 *     rounded with {@link BoardTheme#SHIP_CAPSULE_ARC} (a full
 *     semicircle), plus a plain square body between them for the
 *     submarine.</li>
 *     <li>Aircraft carrier (4 cells): two end caps rounded with the
 *     smaller {@link BoardTheme#CELL_ARC}, plus two plain square body
 *     segments between them.</li>
 * </ul>
 * Because ship sprites are symmetric along their own axis, rotating a
 * ship never requires an actual 90-degree transform: the caller only
 * needs to swap which axis (row vs. column) segments are laid out
 * along, which is exactly what the {@code orientation} parameter
 * controls here.
 */
public final class ShipShapeFactory {

    private ShipShapeFactory() {
        // Utility class, not meant to be instantiated.
    }

    private static final double TAPER_START_RATIO = 1.2;

    /**
     * Creates the visual body of a ship, including the darker hit-point
     * dots marking where each segment will show a hit marker later.
     * Each piece is tagged with {@code fillStyleClass} so its color
     * comes entirely from the stylesheet (e.g. the normal navy body,
     * or a dark red "sunk & revealed" variant).
     *
     * @param shipType       The ship category, defines size and corner style.
     * @param orientation    The orientation to render the ship in.
     * @param fillStyleClass The CSS class controlling this ship's fill color.
     * @return A {@link Group} sized to exactly cover the ship's footprint
     *         ({@code size * CELL_SIZE} along its length), top-left at local (0,0).
     */
    public static Group createShipNode(ShipType shipType, Orientation orientation, String fillStyleClass) {
        int size = shipType.getSize();
        Group shipGroup = new Group();

        for (int segment = 0; segment < size; segment++) {
            Shape piece = createSegmentPiece(size, segment, orientation);
            piece.getStyleClass().add(fillStyleClass);
            positionSegment(piece, segment, orientation);
            shipGroup.getChildren().add(piece);

            Circle hitDot = createHitDot(segment, orientation);
            shipGroup.getChildren().add(hitDot);
        }

        return shipGroup;
    }

    /**
     * Convenience overload using the default "ship-body" style class.
     *
     * @param shipType    The ship category.
     * @param orientation The orientation to render the ship in.
     * @return The ship's visual {@link Group}.
     */
    public static Group createShipNode(ShipType shipType, Orientation orientation) {
        return createShipNode(shipType, orientation, BoardTheme.CLASS_SHIP_BODY);
    }

    private static Shape createSegmentPiece(int size, int segment, Orientation orientation) {
        if (size == 1) {
            return createUniformRoundedSquare(BoardTheme.CELL_ARC * 1.6);
        }

        boolean isFirst = segment == 0;
        boolean isLast = segment == size - 1;

        if (!isFirst && !isLast) {
            return new Rectangle(BoardTheme.CELL_SIZE, BoardTheme.CELL_SIZE);
        }

        boolean outwardIsLeadingSide = isFirst;
        if (size <= 3) {
            return createPointedEndCap(outwardIsLeadingSide, orientation); // antes: createEndCap(SHIP_CAPSULE_ARC, ...)
        }
        return createEndCap(BoardTheme.CELL_ARC * 1.6, outwardIsLeadingSide, orientation);
    }

    private static Rectangle createUniformRoundedSquare(double arc) {
        Rectangle square = new Rectangle(BoardTheme.CELL_SIZE, BoardTheme.CELL_SIZE);
        square.setArcWidth(arc);
        square.setArcHeight(arc);
        return square;
    }

    /**
     * Builds a single cell-sized end cap: a fully rounded square unioned
     * with a plain half-rectangle covering the inward half, which masks
     * the rounding away on the side facing the rest of the ship and
     * leaves it only on the outward-facing corners.
     *
     * @param arc                 The corner radius to use for the rounded half.
     * @param outwardIsLeadingSide {@code true} if the outward side is the
     *                             left (horizontal) / top (vertical) side.
     * @param orientation         The ship's orientation.
     * @return The composed {@link Shape} for this end cap.
     */
    private static Shape createEndCap(double arc, boolean outwardIsLeadingSide, Orientation orientation) {
        double size = BoardTheme.CELL_SIZE;
        double half = size / 2.0;

        Rectangle roundedBase = new Rectangle(size, size);
        roundedBase.setArcWidth(arc);
        roundedBase.setArcHeight(arc);

        Rectangle squareOffHalf;
        if (orientation == Orientation.HORIZONTAL) {
            squareOffHalf = new Rectangle(half, size);
            squareOffHalf.setX(outwardIsLeadingSide ? half : 0);
        } else {
            squareOffHalf = new Rectangle(size, half);
            squareOffHalf.setY(outwardIsLeadingSide ? half : 0);
        }

        return Shape.union(roundedBase, squareOffHalf);
    }

    private static void positionSegment(Shape piece, int segment, Orientation orientation) {
        double offset = segment * BoardTheme.CELL_SIZE;
        if (orientation == Orientation.HORIZONTAL) {
            piece.setTranslateX(offset);
        } else {
            piece.setTranslateY(offset);
        }
    }

    private static Circle createHitDot(int segment, Orientation orientation) {
        double centerAlongLength = segment * BoardTheme.CELL_SIZE + BoardTheme.CELL_SIZE / 2.0;
        double centerAcross = BoardTheme.CELL_SIZE / 2.0;

        double centerX = orientation == Orientation.HORIZONTAL ? centerAlongLength : centerAcross;
        double centerY = orientation == Orientation.HORIZONTAL ? centerAcross : centerAlongLength;

        Circle dot = new Circle(centerX, centerY, BoardTheme.HIT_DOT_RADIUS);
        dot.getStyleClass().add(BoardTheme.CLASS_SHIP_HIT_DOT);
        return dot;
    }

    private static Shape createPointedEndCap(boolean outwardIsLeadingSide, Orientation orientation) {
        double size = BoardTheme.CELL_SIZE;
        double taperStart = size * TAPER_START_RATIO;
        Path path = new Path();

        if (orientation == Orientation.HORIZONTAL) {
            if (outwardIsLeadingSide) {
                path.getElements().addAll(
                        new MoveTo(size, 0),
                        new LineTo(taperStart, 0),
                        new QuadCurveTo(0, 0, 0, size / 2.0),
                        new QuadCurveTo(0, size, taperStart, size),
                        new LineTo(size, size),
                        new ClosePath());
            } else {
                path.getElements().addAll(
                        new MoveTo(0, 0),
                        new LineTo(size - taperStart, 0),
                        new QuadCurveTo(size, 0, size, size / 2.0),
                        new QuadCurveTo(size, size, size - taperStart, size),
                        new LineTo(0, size),
                        new ClosePath());
            }
        } else {
            if (outwardIsLeadingSide) {
                path.getElements().addAll(
                        new MoveTo(0, size),
                        new LineTo(0, taperStart),
                        new QuadCurveTo(0, 0, size / 2.0, 0),
                        new QuadCurveTo(size, 0, size, taperStart),
                        new LineTo(size, size),
                        new ClosePath());
            } else {
                path.getElements().addAll(
                        new MoveTo(0, 0),
                        new LineTo(0, size - taperStart),
                        new QuadCurveTo(0, size, size / 2.0, size),
                        new QuadCurveTo(size, size, size, size - taperStart),
                        new LineTo(size, 0),
                        new ClosePath());
            }
        }

        path.setStroke(null);

        return path;
    }
}