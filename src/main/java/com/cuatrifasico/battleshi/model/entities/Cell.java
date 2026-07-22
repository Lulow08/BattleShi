package com.cuatrifasico.battleshi.model.entities;

import com.cuatrifasico.battleshi.model.enums.CellState;

import java.io.Serializable;

/**
 * Represents a single cell on a board: its coordinate, its current
 * visual/logical state, and (if any) the ship occupying it.
 */
public final class Cell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Coordinate coordinate;
    private CellState state;
    private Ship occupyingShip;

    /**
     * Creates a new, empty water cell at the given coordinate.
     *
     * @param coordinate The board position this cell represents.
     */
    public Cell(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.state = CellState.WATER;
        this.occupyingShip = null;
    }

    /**
     * @return This cell's coordinate.
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * @return This cell's current state.
     */
    public CellState getState() {
        return state;
    }

    /**
     * Updates this cell's state.
     *
     * @param state The new state to assign.
     */
    public void setState(CellState state) {
        this.state = state;
    }

    /**
     * @return The ship occupying this cell, or {@code null} if it is empty water.
     */
    public Ship getOccupyingShip() {
        return occupyingShip;
    }

    /**
     * Assigns a ship to this cell and marks it as {@link CellState#SHIP}.
     *
     * @param ship The ship that occupies this cell.
     */
    public void assignShip(Ship ship) {
        this.occupyingShip = ship;
        this.state = CellState.SHIP;
    }

    /**
     * Clears this cell back to empty, unshot water. Used by the
     * placement phase when picking up an already-placed ship to move
     * it elsewhere, and by the "restart placement" action.
     */
    public void clear() {
        this.occupyingShip = null;
        this.state = CellState.WATER;
    }

    /**
     * @return {@code true} if this cell has already been fired upon
     *         (i.e. its state is neither {@link CellState#WATER} nor {@link CellState#SHIP}).
     */
    public boolean isAlreadyShot() {
        return state == CellState.MISS || state == CellState.HIT || state == CellState.SUNK;
    }
}
