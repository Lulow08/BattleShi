package com.cuatrifasico.battleshi.model;

import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.model.entities.Ship;
import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Ship}.
 * Covers construction, hit registration, sunk detection, and cell occupancy.
 */
@DisplayName("Ship")
class ShipTest {

    // A DESTROYER occupies 2 cells: (0,0) and (0,1)
    private Ship destroyer;
    private Coordinate cell0;
    private Coordinate cell1;

    // A FRIGATE occupies 1 cell: (5,5)
    private Ship frigate;
    private Coordinate frigateCell;

    @BeforeEach
    void setUp() {
        cell0 = new Coordinate(0, 0);
        cell1 = new Coordinate(0, 1);
        Set<Coordinate> destroyerCells = new LinkedHashSet<>();
        destroyerCells.add(cell0);
        destroyerCells.add(cell1);
        destroyer = new Ship(ShipType.DESTROYER, Orientation.HORIZONTAL, destroyerCells);

        frigateCell = new Coordinate(5, 5);
        Set<Coordinate> frigateCells = new LinkedHashSet<>();
        frigateCells.add(frigateCell);
        frigate = new Ship(ShipType.FRIGATE, Orientation.HORIZONTAL, frigateCells);
    }

    // ------------------------------------------------------------------ //
    //  Construction                                                        //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getShipType returns the correct type")
    void getShipTypeReturnsCorrectType() {
        assertEquals(ShipType.DESTROYER, destroyer.getShipType());
        assertEquals(ShipType.FRIGATE, frigate.getShipType());
    }

    @Test
    @DisplayName("getOrientation returns the orientation given at construction")
    void getOrientationReturnsCorrectOrientation() {
        assertEquals(Orientation.HORIZONTAL, destroyer.getOrientation());
    }

    @Test
    @DisplayName("getOccupiedCells returns all cells given at construction")
    void getOccupiedCellsReturnsAllCells() {
        Set<Coordinate> cells = destroyer.getOccupiedCells();
        assertEquals(2, cells.size());
        assertTrue(cells.contains(cell0));
        assertTrue(cells.contains(cell1));
    }

    @Test
    @DisplayName("getOccupiedCells returns an unmodifiable view")
    void getOccupiedCellsIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> destroyer.getOccupiedCells().add(new Coordinate(9, 9)));
    }

    // ------------------------------------------------------------------ //
    //  occupies()                                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("occupies returns true for a cell the ship is on")
    void occupiesReturnsTrueForOwnCell() {
        assertTrue(destroyer.occupies(cell0));
        assertTrue(destroyer.occupies(cell1));
    }

    @Test
    @DisplayName("occupies returns false for a cell the ship is not on")
    void occupiesReturnsFalseForOtherCell() {
        assertFalse(destroyer.occupies(new Coordinate(9, 9)));
    }

    // ------------------------------------------------------------------ //
    //  isSunk() before any hits                                            //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("isSunk returns false when no hits have been registered")
    void isSunkFalseWithNoHits() {
        assertFalse(destroyer.isSunk());
    }

    // ------------------------------------------------------------------ //
    //  registerHit() – partial hits                                        //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("registerHit returns false when ship is not yet sunk")
    void registerHitReturnsFalseWhenNotSunk() {
        boolean sunk = destroyer.registerHit(cell0);
        assertFalse(sunk);
    }

    @Test
    @DisplayName("isSunk returns false after a partial hit on a multi-cell ship")
    void isSunkFalseAfterPartialHit() {
        destroyer.registerHit(cell0);
        assertFalse(destroyer.isSunk());
    }

    // ------------------------------------------------------------------ //
    //  registerHit() – sinking                                             //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("registerHit returns true when the last cell is hit")
    void registerHitReturnsTrueWhenSunk() {
        destroyer.registerHit(cell0);
        boolean sunk = destroyer.registerHit(cell1);
        assertTrue(sunk);
    }

    @Test
    @DisplayName("isSunk returns true after all cells are hit")
    void isSunkTrueAfterAllCellsHit() {
        destroyer.registerHit(cell0);
        destroyer.registerHit(cell1);
        assertTrue(destroyer.isSunk());
    }

    @Test
    @DisplayName("A single-cell ship (FRIGATE) sinks on its first hit")
    void frigatesinksOnFirstHit() {
        boolean sunk = frigate.registerHit(frigateCell);
        assertTrue(sunk);
        assertTrue(frigate.isSunk());
    }

    // ------------------------------------------------------------------ //
    //  registerHit() – idempotency                                         //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Hitting the same cell twice does not change the sunk status incorrectly")
    void duplicateHitOnSameCell() {
        // Register the same cell twice on a 2-cell ship — it should not be sunk yet
        destroyer.registerHit(cell0);
        destroyer.registerHit(cell0); // duplicate
        assertFalse(destroyer.isSunk(), "Hitting the same cell twice should not sink a 2-cell ship");
    }
}