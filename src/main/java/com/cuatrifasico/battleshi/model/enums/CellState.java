package com.cuatrifasico.battleshi.model.enums;

/**
 * Represents the visual and logical state of a single board cell.
 */
public enum CellState {

    /** No ship occupies the cell and it has not been fired upon. */
    WATER,

    /** A ship occupies the cell and it has not been fired upon. */
    SHIP,

    /** A shot was fired at this cell and it contained no ship. */
    MISS,

    /** A shot hit a ship part, but the ship it belongs to is not fully sunk. */
    HIT,

    /** A shot hit a ship part, and the ship it belongs to is completely sunk. */
    SUNK
}