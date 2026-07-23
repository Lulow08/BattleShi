package com.cuatrifasico.battleshi.view.board;

import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.model.entities.Ship;
import com.cuatrifasico.battleshi.view.shapes.SpriteOverlayFactory;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the PNG sprite {@link ImageView} overlays that sit on top of a
 * {@link BoardGridView}'s overlay layer. The underlying {@link javafx.scene.shape.Shape}
 * nodes remain in the scene graph but are made visually transparent via CSS,
 * so they continue to handle mouse hit-testing and cell highlights while the
 * PNGs provide the artwork.
 *
 * <p>This class is intentionally kept separate from {@link BoardGridView} to
 * respect the Single Responsibility Principle: {@code BoardGridView} owns grid
 * construction and cell interaction; {@code BoardSpriteLayer} owns the
 * decoration layer on top.</p>
 */
public final class BoardSpriteLayer {

    /** Maps a placed Ship to its currently displayed overlay ImageView. */
    private final Map<Ship, ImageView> shipOverlays = new HashMap<>();

    /** Maps a fired-upon coordinate to its marker ImageView, so it can be removed later. */
    private final Map<Coordinate, ImageView> markerOverlays = new HashMap<>();

    /** The overlay pane from the associated BoardGridView. */
    private final Pane overlayLayer;

    /** The associated grid view (used to resolve cell origins). */
    private final BoardGridView boardGridView;

    /**
     * Creates a sprite layer bound to the given board grid view.
     *
     * @param boardGridView The board whose overlay layer will receive the sprites.
     */
    public BoardSpriteLayer(BoardGridView boardGridView) {
        this.boardGridView = boardGridView;
        this.overlayLayer  = boardGridView.getOverlayLayer();
    }

    // -----------------------------------------------------------------------
    // Ship overlays
    // -----------------------------------------------------------------------

    /**
     * Places a live (non-sunk) ship sprite overlay at the ship's head coordinate.
     * If an overlay for this ship already exists it is replaced.
     *
     * @param ship      The ship to render.
     * @param headCoord The coordinate of the ship's first (head) cell.
     */
    public void addShipOverlay(Ship ship, Coordinate headCoord) {
        removeShipOverlay(ship);

        ImageView view = SpriteOverlayFactory.createShipOverlay(
                ship.getShipType(), ship.getOrientation(), false);

        positionAtCell(view, headCoord);
        overlayLayer.getChildren().add(view);
        shipOverlays.put(ship, view);
    }

    /**
     * Replaces a ship's overlay with the sunk/revealed variant. Call this after
     * {@link com.cuatrifasico.battleshi.model.entities.Board#receiveShot} returns
     * {@link com.cuatrifasico.battleshi.model.enums.CellState#SUNK}.
     *
     * @param ship      The ship that was just sunk.
     * @param headCoord The coordinate of the ship's head cell.
     */
    public void revealSunkShip(Ship ship, Coordinate headCoord) {
        removeShipOverlay(ship);

        ImageView view = SpriteOverlayFactory.createShipOverlay(
                ship.getShipType(), ship.getOrientation(), true);

        positionAtCell(view, headCoord);
        overlayLayer.getChildren().add(view);
        shipOverlays.put(ship, view);
    }

    /**
     * Removes the sprite overlay for the given ship, if one exists.
     *
     * @param ship The ship whose overlay should be removed.
     */
    public void removeShipOverlay(Ship ship) {
        ImageView old = shipOverlays.remove(ship);
        if (old != null) {
            overlayLayer.getChildren().remove(old);
        }
    }

    /**
     * Removes every ship overlay from the overlay layer.
     * Useful for a full board reset (restart / new game).
     */
    public void clearAllShipOverlays() {
        overlayLayer.getChildren().removeAll(shipOverlays.values());
        shipOverlays.clear();
    }

    // -----------------------------------------------------------------------
    // Marker overlays (hit / miss)
    // -----------------------------------------------------------------------

    /**
     * Places a hit marker sprite at the given coordinate.
     * Replaces any existing marker at that coordinate.
     *
     * @param coordinate The cell that was hit.
     */
    public void addHitMarker(Coordinate coordinate) {
        ImageView view = SpriteOverlayFactory.createHitOverlay();
        positionAtCell(view, coordinate);
        overlayLayer.getChildren().add(view);
        markerOverlays.put(coordinate, view);
    }

    /**
     * Places a miss marker sprite at the given coordinate.
     *
     * @param coordinate The cell that was missed.
     */
    public void addMissMarker(Coordinate coordinate) {
        ImageView view = SpriteOverlayFactory.createMissOverlay();
        positionAtCell(view, coordinate);
        overlayLayer.getChildren().add(view);
        markerOverlays.put(coordinate, view);
    }

    /**
     * Removes the marker overlay at the given coordinate, if one exists.
     * Used when a ship is sunk to clear the hit markers before revealing
     * the sunk sprite.
     *
     * @param coordinate The cell whose marker should be removed.
     */
    public void removeMarker(Coordinate coordinate) {
        ImageView old = markerOverlays.remove(coordinate);
        if (old != null) {
            overlayLayer.getChildren().remove(old);
        }
    }

    /**
     * Removes all marker overlays (hit and miss) from the overlay layer.
     * Ship overlays are NOT removed by this method.
     */
    public void clearAllMarkers() {
        overlayLayer.getChildren().removeAll(markerOverlays.values());
        markerOverlays.clear();
    }

    /**
     * Removes all overlays (ships and markers) from the overlay layer.
     */
    public void clearAll() {
        overlayLayer.getChildren().clear();
        shipOverlays.clear();
        markerOverlays.clear();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Translates an {@link ImageView} so its top-left corner aligns with the
     * logical cell origin in the overlay layer.
     *
     * @param view       The node to position.
     * @param coordinate The target cell.
     */
    private void positionAtCell(ImageView view, Coordinate coordinate) {
        Point2D origin = boardGridView.getCellOrigin(coordinate);
        view.setTranslateX(origin.getX());
        view.setTranslateY(origin.getY());
    }
}