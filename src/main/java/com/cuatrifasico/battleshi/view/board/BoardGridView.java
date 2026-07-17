package com.cuatrifasico.battleshi.view.board;

import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.view.board.BoardTheme;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * Builds and owns the visual grid for a single 10x10 board: the
 * column letters (A-J) and row numbers (1-10), the water cells
 * themselves, and a transparent overlay layer where ships and shot
 * markers are placed.
 * <p>
 * The logical grid treats every cell as a perfect {@code CELL_SIZE} x
 * {@code CELL_SIZE} square glued directly to its neighbors â€” cell
 * {@code n} starts exactly at {@code n * CELL_SIZE}, with no gap
 * baked into that math. The visual "spacing" between water cells is
 * purely cosmetic: each light-blue cell square is drawn inset by half
 * of {@link BoardTheme#CELL_GAP} on every side, over the shared dark
 * board background, so two adjacent insets add up to the full visual
 * gap. Ships and shot markers ignore that inset entirely and occupy
 * the full {@code CELL_SIZE} square, which is what makes them read as
 * "glued together" pieces with no seams between segments.
 * <p>
 * This class only builds and exposes the grid; it deliberately knows
 * nothing about game rules or turns, keeping view and controller
 * concerns separate (Single Responsibility / MVC).
 */
public final class BoardGridView {

    private static final double HEADER_SIZE = 20;

    private final Pane rootPane;
    private final Pane overlayLayer;
    private final Rectangle[][] cellRectangles;
    private Rectangle turnIndicator;

    /**
     * Builds a new board grid view.
     */
    public BoardGridView() {
        this.cellRectangles = new Rectangle[Coordinate.BOARD_SIZE][Coordinate.BOARD_SIZE];
        this.rootPane = new Pane();
        this.overlayLayer = new Pane();
        this.overlayLayer.setMouseTransparent(true);
        this.overlayLayer.setLayoutX(HEADER_SIZE);
        this.overlayLayer.setLayoutY(HEADER_SIZE);

        buildBackground();
        buildHeaders();
        buildCells();
        buildTurnIndicator();

        rootPane.getChildren().add(overlayLayer);
    }

    /**
     * @return The root node to be embedded in the FXML/scene graph.
     */
    public Pane getRootNode() {
        return rootPane;
    }

    /**
     * @return The transparent layer where ship and shot marker nodes
     *         should be added, already aligned with the cell grid.
     */
    public Pane getOverlayLayer() {
        return overlayLayer;
    }

    /**
     * @param coordinate The cell to look up.
     * @return The background {@link Rectangle} representing that cell
     *         (the inset light-blue square), useful to attach mouse
     *         handlers and hover feedback.
     */
    public Rectangle getCellRectangle(Coordinate coordinate) {
        return cellRectangles[coordinate.getRowIndex()][coordinate.getColumnIndex()];
    }

    /**
     * Computes the top-left position of a cell's full logical square
     * (ignoring the cosmetic inset), relative to the overlay layer.
     * This is where ship/marker nodes should be translated to, so
     * they occupy the entire cell rather than the inset water sprite.
     *
     * @param coordinate The target cell.
     * @return The (x, y) position, in overlay-layer-local coordinates.
     */
    public Point2D getCellOrigin(Coordinate coordinate) {
        double x = coordinate.getColumnIndex() * BoardTheme.CELL_SIZE;
        double y = coordinate.getRowIndex() * BoardTheme.CELL_SIZE;
        return new Point2D(x, y);
    }

    /**
     * Attaches a click handler to every cell in the grid. The handler
     * receives the coordinate of the clicked cell via the rectangle's
     * user data, keeping this view decoupled from how the controller
     * reacts to the click (the controller is expected to wrap this in
     * its own mouse event adapter).
     *
     * @param handler The click handler to attach to each cell.
     */
    public void setOnCellClicked(EventHandler<MouseEvent> handler) {
        for (int row = 0; row < Coordinate.BOARD_SIZE; row++) {
            for (int column = 0; column < Coordinate.BOARD_SIZE; column++) {
                cellRectangles[row][column].setOnMouseClicked(handler);
            }
        }
    }

    private void buildBackground() {
        double gridSize = Coordinate.BOARD_SIZE * BoardTheme.CELL_SIZE;
        Rectangle background = new Rectangle(gridSize, gridSize);
        background.getStyleClass().add(BoardTheme.CLASS_BOARD_BACKGROUND);
        background.setArcWidth(BoardTheme.CELL_ARC * 2);
        background.setArcHeight(BoardTheme.CELL_ARC * 2);
        background.setLayoutX(HEADER_SIZE);
        background.setLayoutY(HEADER_SIZE);
        rootPane.getChildren().add(background);
    }

    private void buildHeaders() {
        for (int column = 0; column < Coordinate.BOARD_SIZE; column++) {
            char letter = (char) ('A' + column);
            Label label = createHeaderLabel(String.valueOf(letter));
            label.setLayoutX(HEADER_SIZE + column * BoardTheme.CELL_SIZE);
            label.setLayoutY(0);
            label.setPrefWidth(BoardTheme.CELL_SIZE);
            label.setAlignment(Pos.CENTER);
            rootPane.getChildren().add(label);
        }
        for (int row = 0; row < Coordinate.BOARD_SIZE; row++) {
            Label label = createHeaderLabel(String.valueOf(row + 1));
            label.setLayoutX(0);
            label.setLayoutY(HEADER_SIZE + row * BoardTheme.CELL_SIZE);
            label.setPrefHeight(BoardTheme.CELL_SIZE);
            label.setAlignment(Pos.CENTER);
            rootPane.getChildren().add(label);
        }
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("board-header-label");
        return label;
    }

    private void buildCells() {
        for (int row = 0; row < Coordinate.BOARD_SIZE; row++) {
            for (int column = 0; column < Coordinate.BOARD_SIZE; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                Rectangle cell = createCellRectangle();
                cell.setUserData(coordinate);

                Point2D origin = getCellOrigin(coordinate);
                double inset = BoardTheme.CELL_GAP / 2.0;
                cell.setLayoutX(HEADER_SIZE + origin.getX() + inset);
                cell.setLayoutY(HEADER_SIZE + origin.getY() + inset);

                cellRectangles[row][column] = cell;
                rootPane.getChildren().add(cell);
            }
        }
    }

    private Rectangle createCellRectangle() {
        double insetSize = BoardTheme.CELL_SIZE - BoardTheme.CELL_GAP;
        Rectangle cell = new Rectangle(insetSize, insetSize);
        cell.setArcWidth(BoardTheme.CELL_ARC);
        cell.setArcHeight(BoardTheme.CELL_ARC);
        cell.getStyleClass().add(BoardTheme.CLASS_CELL_WATER);
        return cell;
    }

    /**
     * Builds the (initially inactive) border drawn around the 10x10
     * cell block to signal whose turn it is. It sits outside the grid
     * by {@link BoardTheme#TURN_INDICATOR_MARGIN} on every side, and
     * stops short of the coordinate headers rather than surrounding
     * them, per the reference design.
     */
    private void buildTurnIndicator() {
        double margin = BoardTheme.TURN_INDICATOR_MARGIN;
        double size = Coordinate.BOARD_SIZE * BoardTheme.CELL_SIZE + 2 * margin;

        turnIndicator = new Rectangle(size, size);
        turnIndicator.setArcWidth(BoardTheme.CELL_ARC * 2);
        turnIndicator.setArcHeight(BoardTheme.CELL_ARC * 2);
        turnIndicator.setLayoutX(HEADER_SIZE - margin);
        turnIndicator.setLayoutY(HEADER_SIZE - margin);
        turnIndicator.setMouseTransparent(true);
        turnIndicator.getStyleClass().add(BoardTheme.CLASS_TURN_INDICATOR);

        rootPane.getChildren().add(turnIndicator);
    }

    /**
     * Toggles this board's turn-indicator border on or off. Intended
     * to be called by the controller whenever the active player
     * changes, so it can freely switch the highlight between the
     * player's and the opponent's {@link BoardGridView} instances.
     *
     * @param active {@code true} to show the border, {@code false} to hide it.
     */
    public void setTurnIndicatorActive(boolean active) {
        turnIndicator.getStyleClass().removeAll(BoardTheme.CLASS_TURN_INDICATOR_ACTIVE);
        if (active) {
            turnIndicator.getStyleClass().add(BoardTheme.CLASS_TURN_INDICATOR_ACTIVE);
        }
    }

    /**
     * Marks a cell as a valid or invalid placement target while the
     * player is dragging a ship, by toggling a CSS class rather than
     * setting an explicit fill, so the actual color stays defined in
     * the stylesheet.
     *
     * @param coordinate The cell to highlight.
     * @param valid      {@code true} to mark it as a valid placement spot.
     */
    public void setCellHighlight(Coordinate coordinate, boolean valid) {
        Rectangle cell = getCellRectangle(coordinate);
        cell.getStyleClass().removeAll(BoardTheme.CLASS_CELL_HIGHLIGHT_VALID, BoardTheme.CLASS_CELL_HIGHLIGHT_INVALID);
        cell.getStyleClass().add(valid ? BoardTheme.CLASS_CELL_HIGHLIGHT_VALID : BoardTheme.CLASS_CELL_HIGHLIGHT_INVALID);
    }

    /**
     * Resets a cell's highlight back to the default water look.
     *
     * @param coordinate The cell to reset.
     */
    public void resetCellHighlight(Coordinate coordinate) {
        Rectangle cell = getCellRectangle(coordinate);
        cell.getStyleClass().removeAll(BoardTheme.CLASS_CELL_HIGHLIGHT_VALID, BoardTheme.CLASS_CELL_HIGHLIGHT_INVALID);
    }
}