package com.cuatrifasico.battleshi.model.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable representation of a single board position.
 * <p>
 * Rows are exposed to the user as numbers 1 to 10, and columns are
 * exposed as letters A to J, matching the coordinate scheme used
 * throughout the user interface (e.g. "B4"). Internally both values
 * are stored as zero-based indices (0..9) to simplify array access.
 */
public final class Coordinate implements Serializable, Comparable<Coordinate> {

    private static final long serialVersionUID = 1L;

    /** Number of rows/columns on any board. */
    public static final int BOARD_SIZE = 10;

    private final int rowIndex;    // 0..9  (displayed as 1..10)
    private final int columnIndex; // 0..9  (displayed as A..J)

    /**
     * Creates a coordinate from zero-based row and column indices.
     *
     * @param rowIndex    Zero-based row index (0..9).
     * @param columnIndex Zero-based column index (0..9).
     * @throws IndexOutOfBoundsException If either index is outside the 0..9 range.
     */
    public Coordinate(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= BOARD_SIZE || columnIndex < 0 || columnIndex >= BOARD_SIZE) {
            throw new IndexOutOfBoundsException(
                    "Coordinate out of bounds: row=" + rowIndex + ", column=" + columnIndex);
        }
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    /**
     * Parses a user-facing coordinate string such as "A5" or "J10" into a Coordinate.
     *
     * @param text The coordinate text, column letter first, followed by the row number.
     * @return The parsed Coordinate.
     * @throws IllegalArgumentException If the text does not match a valid coordinate.
     */
    public static Coordinate parse(String text) {
        if (text == null || text.length() < 2 || text.length() > 3) {
            throw new IllegalArgumentException("Invalid coordinate text: " + text);
        }
        char columnLetter = Character.toUpperCase(text.charAt(0));
        int columnIndex = columnLetter - 'A';
        int rowNumber;
        try {
            rowNumber = Integer.parseInt(text.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate text: " + text, e);
        }
        return new Coordinate(rowNumber - 1, columnIndex);
    }

    /**
     * @return The zero-based row index (0..9).
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * @return The zero-based column index (0..9).
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * @return The user-facing row number (1..10).
     */
    public int getDisplayRow() {
        return rowIndex + 1;
    }

    /**
     * @return The user-facing column letter (A..J).
     */
    public char getDisplayColumn() {
        return (char) ('A' + columnIndex);
    }

    /**
     * Attempts to build the coordinate located one step away from this one,
     * in the given row/column deltas. Returns {@code null} instead of throwing
     * when the resulting position would fall outside the board, which allows
     * callers (e.g. the machine's hunt strategy) to safely discard it.
     *
     * @param rowDelta    Row displacement, may be negative.
     * @param columnDelta Column displacement, may be negative.
     * @return The neighboring Coordinate, or {@code null} if out of bounds.
     */
    public Coordinate neighbor(int rowDelta, int columnDelta) {
        int newRow = rowIndex + rowDelta;
        int newColumn = columnIndex + columnDelta;
        if (newRow < 0 || newRow >= BOARD_SIZE || newColumn < 0 || newColumn >= BOARD_SIZE) {
            return null;
        }
        return new Coordinate(newRow, newColumn);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) other;
        return rowIndex == that.rowIndex && columnIndex == that.columnIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowIndex, columnIndex);
    }

    @Override
    public int compareTo(Coordinate other) {
        int rowCompare = Integer.compare(this.rowIndex, other.rowIndex);
        return rowCompare != 0 ? rowCompare : Integer.compare(this.columnIndex, other.columnIndex);
    }

    @Override
    public String toString() {
        return "" + getDisplayColumn() + getDisplayRow();
    }
}