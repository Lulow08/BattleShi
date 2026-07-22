package com.cuatrifasico.battleshi.view.board;

import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import com.cuatrifasico.battleshi.view.shapes.ShipShapeFactory;
import com.cuatrifasico.battleshi.view.board.BoardTheme;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Builds the fleet tray shown below the player's board, listing every
 * ship in the fleet in the "shadow" style used for already-placed
 * ships. The layout is intentionally fixed into two rows (the window
 * is not resizable, per {@code AppInitializer}), so a single long
 * horizontal strip never pushes the opponent board sideways:
 * <ul>
 *     <li>Row 1: aircraft carrier + both submarines (10 cells wide).</li>
 *     <li>Row 2: the three destroyers + four frigates (10 cells wide).</li>
 * </ul>
 * Selection and drag-to-place behavior are intentionally NOT wired
 * here; this class only builds the static visual tray so a placement
 * controller can later attach mouse handlers directly to each entry.
 */
public final class ShipTrayView {

    private final VBox rootNode;

    /**
     * Builds the two-row tray with one shadow-styled entry per ship.
     */
    public ShipTrayView() {
        HBox firstRow = buildRow(ShipType.AIRCRAFT_CARRIER, ShipType.SUBMARINE);
        HBox secondRow = buildRow(ShipType.DESTROYER, ShipType.FRIGATE);

        this.rootNode = new VBox(BoardTheme.TRAY_SHIP_SPACING * 2, firstRow, secondRow);
        rootNode.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * @return The root node to be embedded in the game view.
     */
    public VBox getRootNode() {
        return rootNode;
    }

    private HBox buildRow(ShipType... shipTypes) {
        HBox row = new HBox(BoardTheme.TRAY_SHIP_SPACING);
        row.setAlignment(Pos.CENTER_LEFT);
        for (ShipType shipType : shipTypes) {
            for (int i = 0; i < shipType.getFleetCount(); i++) {
                row.getChildren().add(createTrayEntry(shipType));
            }
        }
        return row;
    }

    private Pane createTrayEntry(ShipType shipType) {
        Group ship = ShipShapeFactory.createShipNode(shipType, Orientation.HORIZONTAL);
        ship.getStyleClass().add(BoardTheme.CLASS_SHIP_BODY);

        double width = shipType.getSize() * BoardTheme.CELL_SIZE;
        Pane wrapper = new Pane(ship);
        wrapper.setPrefSize(width, BoardTheme.CELL_SIZE);
        return wrapper;
    }
}