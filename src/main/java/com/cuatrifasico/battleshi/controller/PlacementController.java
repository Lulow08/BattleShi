package com.cuatrifasico.battleshi.controller;

import com.cuatrifasico.battleshi.model.entities.Board;
import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.model.entities.Ship;
import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import com.cuatrifasico.battleshi.model.exceptions.InvalidPlacementException;
import com.cuatrifasico.battleshi.view.board.BoardGridView;
import com.cuatrifasico.battleshi.view.board.ShipTrayView;
import com.cuatrifasico.battleshi.view.shapes.ShipShapeFactory;

import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Drives the ship-placement phase of a match on a single {@link Board}
 * / {@link BoardGridView} pair.
 * <p>
 * Implements the rubric's "select and move" mechanic (point 4): the
 * player either
 * <ol>
 *     <li>clicks a not-yet-used entry on the {@link ShipTrayView} to
 *     pick up a brand-new ship, or</li>
 *     <li>clicks an already-placed ship directly on the board to pick
 *     it back up,</li>
 * </ol>
 * and in both cases the ship then follows the mouse conceptually
 * (previewed with a green/red cell highlight) until the player clicks
 * a destination cell to drop it there. Pressing <b>R</b> rotates the
 * ship currently held; <b>ESC</b> cancels the hold and returns a
 * picked-up ship to its previous spot.
 * <p>
 * This class is built and wired programmatically by {@link GameController}
 * once the board/tray views exist; it is deliberately independent of
 * FXML so it can be unit-tested without a loaded scene.
 */
public final class PlacementController {

    private final Board board;
    private final BoardGridView boardView;
    private final Button restartButton;
    private final Button playButton;

    private static final int TOTAL_FLEET_SIZE = totalFleetSize();

    /** Visual node currently on screen for each placed ship, so it can be moved or removed. */
    private final Map<Ship, Group> shipNodes = new HashMap<>();

    /** Flattened tray entries, in the same order as {@code ShipType.values()} with repeats. */
    private final List<Node> trayEntries = new ArrayList<>();

    /** Tray entries that have already been dropped onto the board. */
    private final Set<Node> usedTrayEntries = new HashSet<>();

    private Node highlightedTrayEntry;
    private Coordinate lastHoveredCoordinate;
    private List<Coordinate> previewedCells;

    /** Currently held ship, or {@code null} if nothing is selected. */
    private Selection selection;

    public PlacementController(Board board,
                                BoardGridView boardView,
                                ShipTrayView trayView,
                                Button restartButton,
                                Button playButton) {
        this.board = board;
        this.boardView = boardView;
        this.restartButton = restartButton;
        this.playButton = playButton;

        wireTray(trayView);
        wireBoard();
        restartButton.setOnAction(event -> resetPlacement());
        playButton.setDisable(true);
    }

    private static int totalFleetSize() {
        int total = 0;
        for (ShipType type : ShipType.values()) {
            total += type.getFleetCount();
        }
        return total;
    }

    // ------------------------------------------------------------------ //
    //  Wiring                                                              //
    // ------------------------------------------------------------------ //

    private void wireTray(ShipTrayView trayView) {
        for (Node rowNode : trayView.getRootNode().getChildren()) {
            if (rowNode instanceof HBox row) {
                trayEntries.addAll(row.getChildren());
            }
        }
        // ShipTrayView lays its rows out by iterating ShipType.values() in
        // fleet-count order (carrier, then submarines in row 1; destroyers,
        // then frigates in row 2), so zipping that same iteration against
        // the flattened entry list recovers which ShipType each node is.
        int index = 0;
        for (ShipType type : ShipType.values()) {
            for (int i = 0; i < type.getFleetCount(); i++) {
                Node entry = trayEntries.get(index++);
                entry.setOnMouseClicked(new TraySelectAdapter(entry, type));
            }
        }
    }

    private void wireBoard() {
        boardView.setOnCellClicked(new CellClickAdapter());
        boardView.getRootNode().setFocusTraversable(true);
        boardView.getRootNode().setOnKeyPressed(new PlacementKeyAdapter());

        for (int row = 0; row < Coordinate.BOARD_SIZE; row++) {
            for (int column = 0; column < Coordinate.BOARD_SIZE; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                Rectangle cellRectangle = boardView.getCellRectangle(coordinate);
                cellRectangle.setOnMouseEntered(new CellHoverAdapter(coordinate));
                cellRectangle.setOnMouseExited(event -> clearHoverPreview());
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Adapters                                                            //
    // ------------------------------------------------------------------ //

    /** Reacts to a click on one of the ten tray entries: picks up that ship type. */
    private final class TraySelectAdapter implements EventHandler<MouseEvent> {
        private final Node entry;
        private final ShipType shipType;

        TraySelectAdapter(Node entry, ShipType shipType) {
            this.entry = entry;
            this.shipType = shipType;
        }

        @Override
        public void handle(MouseEvent event) {
            selectFromTray(entry, shipType);
        }
    }

    /** Reacts to a click on a board cell: either picks up a placed ship, or drops the held one. */
    private final class CellClickAdapter implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Rectangle source = (Rectangle) event.getSource();
            Coordinate coordinate = (Coordinate) source.getUserData();

            if (selection == null) {
                Ship existing = board.getCell(coordinate).getOccupyingShip();
                if (existing != null) {
                    selectPlacedShip(existing);
                }
            } else {
                confirmPlacement(coordinate);
            }
        }
    }

    /** Tracks which cell the mouse is over so rotation (R) can refresh the right preview. */
    private final class CellHoverAdapter implements EventHandler<MouseEvent> {
        private final Coordinate coordinate;

        CellHoverAdapter(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        @Override
        public void handle(MouseEvent event) {
            lastHoveredCoordinate = coordinate;
            refreshHoverPreview();
        }
    }

    /** Rotates or cancels the ship currently held, via the keyboard. */
    private final class PlacementKeyAdapter implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            if (selection == null) {
                return;
            }
            if (event.getCode() == KeyCode.R) {
                selection.orientation = selection.orientation.opposite();
                refreshHoverPreview();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelSelection();
                event.consume();
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Selection lifecycle                                                //
    // ------------------------------------------------------------------ //

    /** Immutable-enough record of what is currently "in hand" while placing. */
    private static final class Selection {
        final ShipType shipType;
        Orientation orientation;
        final Ship originalShip;
        final Coordinate originalHead;
        final Orientation originalOrientation;
        final Node sourceTrayEntry;

        Selection(ShipType shipType, Orientation orientation, Ship originalShip, Node sourceTrayEntry) {
            this.shipType = shipType;
            this.orientation = orientation;
            this.originalShip = originalShip;
            this.sourceTrayEntry = sourceTrayEntry;
            if (originalShip != null) {
                this.originalHead = originalShip.getOccupiedCells().iterator().next();
                this.originalOrientation = originalShip.getOrientation();
            } else {
                this.originalHead = null;
                this.originalOrientation = null;
            }
        }
    }

    private void selectFromTray(Node entry, ShipType shipType) {
        if (usedTrayEntries.contains(entry)) {
            return;
        }
        if (selection != null) {
            cancelSelection();
        }
        selection = new Selection(shipType, Orientation.HORIZONTAL, null, entry);
        highlightTrayEntry(entry);
        boardView.getRootNode().requestFocus();
    }

    private void selectPlacedShip(Ship ship) {
        if (selection != null) {
            cancelSelection();
        }
        Selection picked = new Selection(ship.getShipType(), ship.getOrientation(), ship, null);
        removeShipVisualAndModel(ship);
        selection = picked;
        checkFleetComplete();
        boardView.getRootNode().requestFocus();
    }

    private void confirmPlacement(Coordinate targetHead) {
        try {
            Ship placed = board.placeShip(selection.shipType, targetHead, selection.orientation);
            renderShip(placed);
            if (selection.sourceTrayEntry != null) {
                markTrayEntryUsed(selection.sourceTrayEntry);
            }
            clearSelection();
            checkFleetComplete();
        } catch (InvalidPlacementException invalidTarget) {
            // Keep the ship "in hand" so the player can just try another
            // cell instead of losing their selection on a misclick.
            flashInvalid(targetHead);
        }
    }

    private void cancelSelection() {
        if (selection == null) {
            return;
        }
        if (selection.originalShip != null) {
            restoreOriginalShip(selection);
        }
        clearSelection();
    }

    private void clearSelection() {
        clearHoverPreview();
        clearTrayHighlight();
        selection = null;
    }

    private void restoreOriginalShip(Selection heldSelection) {
        try {
            Ship restored = board.placeShip(
                    heldSelection.shipType, heldSelection.originalHead, heldSelection.originalOrientation);
            renderShip(restored);
            checkFleetComplete();
        } catch (InvalidPlacementException impossible) {
            throw new IllegalStateException(
                    "Could not restore a ship to a position it already validly occupied", impossible);
        }
    }

    private void resetPlacement() {
        cancelSelection();
        for (Ship ship : new ArrayList<>(board.getFleet())) {
            removeShipVisualAndModel(ship);
        }
        usedTrayEntries.clear();
        for (Node entry : trayEntries) {
            entry.setOpacity(1.0);
        }
        playButton.setDisable(true);
    }

    private void checkFleetComplete() {
        playButton.setDisable(board.getFleet().size() < TOTAL_FLEET_SIZE);
    }

    // ------------------------------------------------------------------ //
    //  Rendering helpers                                                   //
    // ------------------------------------------------------------------ //

    private void renderShip(Ship ship) {
        Group node = ShipShapeFactory.createShipNode(ship.getShipType(), ship.getOrientation());
        Coordinate head = ship.getOccupiedCells().iterator().next();
        Point2D origin = boardView.getCellOrigin(head);
        node.setLayoutX(origin.getX());
        node.setLayoutY(origin.getY());
        boardView.getOverlayLayer().getChildren().add(node);
        shipNodes.put(ship, node);
    }

    private void removeShipVisualAndModel(Ship ship) {
        Group node = shipNodes.remove(ship);
        if (node != null) {
            boardView.getOverlayLayer().getChildren().remove(node);
        }
        board.removeShip(ship);
    }

    private void highlightTrayEntry(Node entry) {
        clearTrayHighlight();
        entry.setOpacity(0.6);
        highlightedTrayEntry = entry;
    }

    private void clearTrayHighlight() {
        if (highlightedTrayEntry != null && !usedTrayEntries.contains(highlightedTrayEntry)) {
            highlightedTrayEntry.setOpacity(1.0);
        }
        highlightedTrayEntry = null;
    }

    private void markTrayEntryUsed(Node entry) {
        usedTrayEntries.add(entry);
        entry.setOpacity(0.25);
    }

    private void refreshHoverPreview() {
        clearHoverPreview();
        if (selection == null || lastHoveredCoordinate == null) {
            return;
        }
        List<Coordinate> footprint = projectFootprint(selection.shipType, lastHoveredCoordinate, selection.orientation);
        if (footprint == null) {
            return;
        }
        boolean valid = board.canPlace(selection.shipType, lastHoveredCoordinate, selection.orientation);
        for (Coordinate cell : footprint) {
            boardView.setCellHighlight(cell, valid);
        }
        previewedCells = footprint;
    }

    private void clearHoverPreview() {
        if (previewedCells != null) {
            for (Coordinate cell : previewedCells) {
                boardView.resetCellHighlight(cell);
            }
            previewedCells = null;
        }
    }

    private void flashInvalid(Coordinate attemptedHead) {
        List<Coordinate> footprint = projectFootprint(selection.shipType, attemptedHead, selection.orientation);
        if (footprint == null) {
            return;
        }
        for (Coordinate cell : footprint) {
            boardView.setCellHighlight(cell, false);
        }
        PauseTransition pause = new PauseTransition(Duration.millis(350));
        pause.setOnFinished(event -> {
            for (Coordinate cell : footprint) {
                boardView.resetCellHighlight(cell);
            }
            refreshHoverPreview();
        });
        pause.play();
    }

    /**
     * Projects the cells a ship would occupy from {@code head} without
     * touching the board model — used purely for preview/flash
     * rendering. Mirrors {@code Board}'s own (private) projection logic.
     *
     * @return The projected cells, or {@code null} if any of them would fall off the board.
     */
    private List<Coordinate> projectFootprint(ShipType shipType, Coordinate head, Orientation orientation) {
        List<Coordinate> cells = new ArrayList<>();
        for (int i = 0; i < shipType.getSize(); i++) {
            Coordinate next = head.neighbor(orientation.getRowDelta() * i, orientation.getColumnDelta() * i);
            if (next == null) {
                return null;
            }
            cells.add(next);
        }
        return cells;
    }
}
