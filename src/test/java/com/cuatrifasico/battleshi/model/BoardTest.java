package com.cuatrifasico.battleshi.model;

import com.cuatrifasico.battleshi.model.entities.Board;
import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.model.entities.Ship;
import com.cuatrifasico.battleshi.model.enums.CellState;
import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import com.cuatrifasico.battleshi.model.exceptions.AlreadyShotException;
import com.cuatrifasico.battleshi.model.exceptions.InvalidPlacementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Board}.
 * Covers initial state, ship placement rules, shot processing,
 * and fleet-destroyed detection.
 */
@DisplayName("Board")
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    // ------------------------------------------------------------------ //
    //  Initial state                                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("All cells start as WATER on a new board")
    void allCellsStartAsWater() {
        for (int row = 0; row < Coordinate.BOARD_SIZE; row++) {
            for (int col = 0; col < Coordinate.BOARD_SIZE; col++) {
                Coordinate c = new Coordinate(row, col);
                assertEquals(CellState.WATER, board.getCell(c).getState(),
                        "Cell " + c + " should be WATER initially");
            }
        }
    }

    @Test
    @DisplayName("Fleet is empty on a new board")
    void fleetIsEmptyOnNewBoard() {
        assertTrue(board.getFleet().isEmpty());
    }

    @Test
    @DisplayName("isFleetDestroyed returns false when no ships are placed")
    void isFleetDestroyedFalseWhenEmpty() {
        assertFalse(board.isFleetDestroyed());
    }

    // ------------------------------------------------------------------ //
    //  Ship placement – happy path                                         //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("placeShip adds the ship to the fleet")
    void placeShipAddsToFleet() throws InvalidPlacementException {
        board.placeShip(ShipType.FRIGATE, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertEquals(1, board.getFleet().size());
    }

    @Test
    @DisplayName("placeShip marks all occupied cells as SHIP (horizontal)")
    void placeShipMarksCellsHorizontal() throws InvalidPlacementException {
        // DESTROYER size=2 → occupies (2,2) and (2,3)
        board.placeShip(ShipType.DESTROYER, new Coordinate(2, 2), Orientation.HORIZONTAL);
        assertEquals(CellState.SHIP, board.getCell(new Coordinate(2, 2)).getState());
        assertEquals(CellState.SHIP, board.getCell(new Coordinate(2, 3)).getState());
        assertEquals(CellState.WATER, board.getCell(new Coordinate(2, 4)).getState());
    }

    @Test
    @DisplayName("placeShip marks all occupied cells as SHIP (vertical)")
    void placeShipMarksCellsVertical() throws InvalidPlacementException {
        // SUBMARINE size=3 → occupies (0,0), (1,0), (2,0)
        board.placeShip(ShipType.SUBMARINE, new Coordinate(0, 0), Orientation.VERTICAL);
        assertEquals(CellState.SHIP, board.getCell(new Coordinate(0, 0)).getState());
        assertEquals(CellState.SHIP, board.getCell(new Coordinate(1, 0)).getState());
        assertEquals(CellState.SHIP, board.getCell(new Coordinate(2, 0)).getState());
        assertEquals(CellState.WATER, board.getCell(new Coordinate(3, 0)).getState());
    }

    @Test
    @DisplayName("placeShip returns a Ship whose occupied cells match the type size")
    void placeShipReturnsShipWithCorrectSize() throws InvalidPlacementException {
        Ship ship = board.placeShip(ShipType.AIRCRAFT_CARRIER, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertEquals(ShipType.AIRCRAFT_CARRIER.getSize(), ship.getOccupiedCells().size());
    }

    @Test
    @DisplayName("canPlace returns true for a valid position")
    void canPlaceReturnsTrueForValidPosition() {
        assertTrue(board.canPlace(ShipType.AIRCRAFT_CARRIER, new Coordinate(0, 0), Orientation.HORIZONTAL));
    }

    // ------------------------------------------------------------------ //
    //  Ship placement – invalid cases                                      //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("placeShip throws when ship goes out of bounds horizontally")
    void placeShipThrowsOutOfBoundsHorizontal() {
        // AIRCRAFT_CARRIER size=4 at column 8 → cols 8,9,10,11 → out of bounds
        assertThrows(InvalidPlacementException.class, () ->
                board.placeShip(ShipType.AIRCRAFT_CARRIER, new Coordinate(0, 8), Orientation.HORIZONTAL));
    }

    @Test
    @DisplayName("placeShip throws when ship goes out of bounds vertically")
    void placeShipThrowsOutOfBoundsVertical() {
        // SUBMARINE size=3 at row 9 → rows 9,10,11 → out of bounds
        assertThrows(InvalidPlacementException.class, () ->
                board.placeShip(ShipType.SUBMARINE, new Coordinate(9, 0), Orientation.VERTICAL));
    }

    @Test
    @DisplayName("placeShip throws when ships overlap")
    void placeShipThrowsOnOverlap() throws InvalidPlacementException {
        board.placeShip(ShipType.DESTROYER, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertThrows(InvalidPlacementException.class, () ->
                board.placeShip(ShipType.DESTROYER, new Coordinate(0, 0), Orientation.HORIZONTAL));
    }

    @Test
    @DisplayName("placeShip throws when ships are directly adjacent")
    void placeShipThrowsWhenDirectlyAdjacent() throws InvalidPlacementException {
        board.placeShip(ShipType.FRIGATE, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertThrows(InvalidPlacementException.class, () ->
                board.placeShip(ShipType.FRIGATE, new Coordinate(0, 1), Orientation.HORIZONTAL));
    }

    @Test
    @DisplayName("placeShip throws when ships are diagonally adjacent")
    void placeShipThrowsWhenDiagonallyAdjacent() throws InvalidPlacementException {
        board.placeShip(ShipType.FRIGATE, new Coordinate(0, 0), Orientation.HORIZONTAL);
        assertThrows(InvalidPlacementException.class, () ->
                board.placeShip(ShipType.FRIGATE, new Coordinate(1, 1), Orientation.HORIZONTAL));
    }

    @Test
    @DisplayName("canPlace returns false when position is occupied")
    void canPlaceReturnsFalseWhenOccupied() throws InvalidPlacementException {
        board.placeShip(ShipType.FRIGATE, new Coordinate(5, 5), Orientation.HORIZONTAL);
        assertFalse(board.canPlace(ShipType.FRIGATE, new Coordinate(5, 5), Orientation.HORIZONTAL));
    }

    // ------------------------------------------------------------------ //
    //  Remove ship                                                         //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("removeShip clears its cells back to WATER")
    void removeShipClearsCells() throws InvalidPlacementException {
        Ship ship = board.placeShip(ShipType.FRIGATE, new Coordinate(3, 3), Orientation.HORIZONTAL);
        board.removeShip(ship);
        assertEquals(CellState.WATER, board.getCell(new Coordinate(3, 3)).getState());
    }

    @Test
    @DisplayName("removeShip removes the ship from the fleet")
    void removeShipRemovesFromFleet() throws InvalidPlacementException {
        Ship ship = board.placeShip(ShipType.FRIGATE, new Coordinate(3, 3), Orientation.HORIZONTAL);
        board.removeShip(ship);
        assertTrue(board.getFleet().isEmpty());
    }

    @Test
    @DisplayName("canPlace succeeds in the same spot after removing the ship that was there")
    void canPlaceSucceedsAfterRemoval() throws InvalidPlacementException {
        Ship ship = board.placeShip(ShipType.FRIGATE, new Coordinate(3, 3), Orientation.HORIZONTAL);
        board.removeShip(ship);
        assertTrue(board.canPlace(ShipType.FRIGATE, new Coordinate(3, 3), Orientation.HORIZONTAL));
    }

    // ------------------------------------------------------------------ //
    //  Shot processing                                                     //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("receiveShot on empty water returns MISS and sets cell state")
    void receiveShotOnWaterReturnsMiss() {
        CellState result = board.receiveShot(new Coordinate(0, 0));
        assertEquals(CellState.MISS, result);
        assertEquals(CellState.MISS, board.getCell(new Coordinate(0, 0)).getState());
    }

    @Test
    @DisplayName("receiveShot on a multi-cell ship returns HIT when ship not fully sunk")
    void receiveShotOnShipReturnsHit() throws InvalidPlacementException {
        board.placeShip(ShipType.DESTROYER, new Coordinate(0, 0), Orientation.HORIZONTAL);
        CellState result = board.receiveShot(new Coordinate(0, 0)); // only first cell of 2-cell ship
        assertEquals(CellState.HIT, result);
    }

    @Test
    @DisplayName("receiveShot returns SUNK when the last cell of a ship is hit")
    void receiveShotReturnsSunkWhenLastCellHit() throws InvalidPlacementException {
        board.placeShip(ShipType.FRIGATE, new Coordinate(0, 0), Orientation.HORIZONTAL);
        CellState result = board.receiveShot(new Coordinate(0, 0)); // FRIGATE has size 1
        assertEquals(CellState.SUNK, result);
    }

    @Test
    @DisplayName("receiveShot sets all ship cells to SUNK after sinking")
    void receiveShotSetsAllCellsToSunkAfterSinking() throws InvalidPlacementException {
        board.placeShip(ShipType.DESTROYER, new Coordinate(0, 0), Orientation.HORIZONTAL);
        board.receiveShot(new Coordinate(0, 0));
        board.receiveShot(new Coordinate(0, 1));
        assertEquals(CellState.SUNK, board.getCell(new Coordinate(0, 0)).getState());
        assertEquals(CellState.SUNK, board.getCell(new Coordinate(0, 1)).getState());
    }

    @Test
    @DisplayName("receiveShot on an already-shot cell throws AlreadyShotException")
    void receiveShotOnAlreadyShotCellThrows() {
        board.receiveShot(new Coordinate(0, 0));
        assertThrows(AlreadyShotException.class,
                () -> board.receiveShot(new Coordinate(0, 0)));
    }

    // ------------------------------------------------------------------ //
    //  Fleet destroyed                                                     //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("isFleetDestroyed returns false when some ships are still afloat")
    void isFleetDestroyedFalseWithSurvivors() throws InvalidPlacementException {
        board.placeShip(ShipType.FRIGATE, new Coordinate(0, 0), Orientation.HORIZONTAL);
        board.placeShip(ShipType.FRIGATE, new Coordinate(2, 0), Orientation.HORIZONTAL);
        board.receiveShot(new Coordinate(0, 0)); // sink only first frigate
        assertFalse(board.isFleetDestroyed());
    }

    @Test
    @DisplayName("isFleetDestroyed returns true only after all ships are sunk")
    void isFleetDestroyedTrueWhenAllSunk() throws InvalidPlacementException {
        board.placeShip(ShipType.FRIGATE, new Coordinate(0, 0), Orientation.HORIZONTAL);
        board.placeShip(ShipType.FRIGATE, new Coordinate(2, 0), Orientation.HORIZONTAL);
        board.receiveShot(new Coordinate(0, 0));
        board.receiveShot(new Coordinate(2, 0));
        assertTrue(board.isFleetDestroyed());
    }
}