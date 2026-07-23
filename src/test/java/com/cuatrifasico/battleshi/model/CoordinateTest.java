package com.cuatrifasico.battleshi.model;

import com.cuatrifasico.battleshi.model.entities.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Coordinate}.
 * Covers construction, parsing, neighbor computation,
 * display values, equality, and compareTo.
 */
@DisplayName("Coordinate")
class CoordinateTest {

    // ------------------------------------------------------------------ //
    //  Construction – valid                                                //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Constructor stores row and column indices correctly")
    void constructorStoresIndices() {
        Coordinate c = new Coordinate(3, 5);
        assertEquals(3, c.getRowIndex());
        assertEquals(5, c.getColumnIndex());
    }

    @Test
    @DisplayName("Constructor accepts boundary values (0,0) and (9,9)")
    void constructorAcceptsBoundaryValues() {
        assertDoesNotThrow(() -> new Coordinate(0, 0));
        assertDoesNotThrow(() -> new Coordinate(9, 9));
    }

    // ------------------------------------------------------------------ //
    //  Construction – invalid                                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Constructor throws for negative row index")
    void constructorThrowsForNegativeRow() {
        assertThrows(IndexOutOfBoundsException.class, () -> new Coordinate(-1, 0));
    }

    @Test
    @DisplayName("Constructor throws for row index equal to BOARD_SIZE")
    void constructorThrowsForRowEqualToBoardSize() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Coordinate(Coordinate.BOARD_SIZE, 0));
    }

    @Test
    @DisplayName("Constructor throws for negative column index")
    void constructorThrowsForNegativeColumn() {
        assertThrows(IndexOutOfBoundsException.class, () -> new Coordinate(0, -1));
    }

    @Test
    @DisplayName("Constructor throws for column index equal to BOARD_SIZE")
    void constructorThrowsForColumnEqualToBoardSize() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> new Coordinate(0, Coordinate.BOARD_SIZE));
    }

    // ------------------------------------------------------------------ //
    //  Display values                                                      //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getDisplayRow returns 1-based row number")
    void getDisplayRowIsOneBased() {
        assertEquals(1, new Coordinate(0, 0).getDisplayRow());
        assertEquals(10, new Coordinate(9, 0).getDisplayRow());
    }

    @Test
    @DisplayName("getDisplayColumn returns correct letter (A=0, J=9)")
    void getDisplayColumnReturnsCorrectLetter() {
        assertEquals('A', new Coordinate(0, 0).getDisplayColumn());
        assertEquals('J', new Coordinate(0, 9).getDisplayColumn());
        assertEquals('E', new Coordinate(0, 4).getDisplayColumn());
    }

    @Test
    @DisplayName("toString returns column letter followed by row number")
    void toStringReturnsColumnThenRow() {
        assertEquals("A1", new Coordinate(0, 0).toString());
        assertEquals("J10", new Coordinate(9, 9).toString());
        assertEquals("B4", new Coordinate(3, 1).toString());
    }

    // ------------------------------------------------------------------ //
    //  Parsing                                                             //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("parse converts 'A1' to row=0 col=0")
    void parseA1() {
        Coordinate c = Coordinate.parse("A1");
        assertEquals(0, c.getRowIndex());
        assertEquals(0, c.getColumnIndex());
    }

    @Test
    @DisplayName("parse converts 'J10' to row=9 col=9")
    void parseJ10() {
        Coordinate c = Coordinate.parse("J10");
        assertEquals(9, c.getRowIndex());
        assertEquals(9, c.getColumnIndex());
    }

    @Test
    @DisplayName("parse is case-insensitive for the column letter")
    void parseLowercaseLetter() {
        Coordinate c = Coordinate.parse("b3");
        assertEquals(2, c.getRowIndex());
        assertEquals(1, c.getColumnIndex());
    }

    @Test
    @DisplayName("parse throws IllegalArgumentException for null input")
    void parseThrowsForNull() {
        assertThrows(IllegalArgumentException.class, () -> Coordinate.parse(null));
    }

    @Test
    @DisplayName("parse throws for a string that is too short (empty)")
    void parseThrowsForEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> Coordinate.parse(""));
    }

    @Test
    @DisplayName("parse throws for a string with an out-of-range row number")
    void parseThrowsForOutOfRangeRow() {
        // "A99" → valid format but row 99 is out of bounds
        assertThrows(IndexOutOfBoundsException.class, () -> Coordinate.parse("A99"));
    }

    // ------------------------------------------------------------------ //
    //  neighbor()                                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("neighbor returns correct coordinate for valid delta")
    void neighborReturnsCorrectCoordinate() {
        Coordinate c = new Coordinate(3, 3);
        Coordinate right = c.neighbor(0, 1);
        assertNotNull(right);
        assertEquals(3, right.getRowIndex());
        assertEquals(4, right.getColumnIndex());
    }

    @Test
    @DisplayName("neighbor returns null when the result is out of bounds")
    void neighborReturnsNullWhenOutOfBounds() {
        Coordinate topLeft = new Coordinate(0, 0);
        assertNull(topLeft.neighbor(-1, 0)); // above top edge
        assertNull(topLeft.neighbor(0, -1)); // left of left edge
    }

    @Test
    @DisplayName("neighbor with zero delta returns equivalent coordinate")
    void neighborZeroDeltaReturnsSamePosition() {
        Coordinate c = new Coordinate(4, 4);
        Coordinate same = c.neighbor(0, 0);
        assertNotNull(same);
        assertEquals(c, same);
    }

    // ------------------------------------------------------------------ //
    //  equals() and hashCode()                                             //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Two coordinates with same indices are equal")
    void equalCoordinates() {
        assertEquals(new Coordinate(2, 3), new Coordinate(2, 3));
    }

    @Test
    @DisplayName("Two coordinates with different indices are not equal")
    void unequalCoordinates() {
        assertNotEquals(new Coordinate(2, 3), new Coordinate(2, 4));
        assertNotEquals(new Coordinate(2, 3), new Coordinate(3, 3));
    }

    @Test
    @DisplayName("Equal coordinates have the same hashCode")
    void equalCoordinatesHaveSameHashCode() {
        assertEquals(
                new Coordinate(5, 7).hashCode(),
                new Coordinate(5, 7).hashCode());
    }

    // ------------------------------------------------------------------ //
    //  compareTo()                                                         //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("compareTo orders by row first, then by column")
    void compareToOrdersByRowThenColumn() {
        Coordinate a = new Coordinate(0, 5);
        Coordinate b = new Coordinate(1, 0);
        Coordinate c = new Coordinate(1, 3);

        assertTrue(a.compareTo(b) < 0, "row 0 should be before row 1");
        assertTrue(b.compareTo(c) < 0, "same row, col 0 before col 3");
        assertEquals(0, a.compareTo(new Coordinate(0, 5)));
    }
}