package com.cuatrifasico.battleshi.model.enums;

/**
 * Represents the orientation a ship can have on the board.
 * Provides the row/column deltas used to project a ship's
 * occupied cells starting from its head coordinate.
 */
public enum Orientation {

    HORIZONTAL(0, 1),
    VERTICAL(1, 0);

    private final int rowDelta;
    private final int columnDelta;

    Orientation(int rowDelta, int columnDelta) {
        this.rowDelta = rowDelta;
        this.columnDelta = columnDelta;
    }

    /**
     * @return The row increment applied per cell when projecting a ship.
     */
    public int getRowDelta() {
        return rowDelta;
    }

    /**
     * @return The column increment applied per cell when projecting a ship.
     */
    public int getColumnDelta() {
        return columnDelta;
    }

    /**
     * @return The opposite orientation (used when rotating a ship).
     */
    public Orientation opposite() {
        return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }
}