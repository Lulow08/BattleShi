package com.cuatrifasico.battleshi.model.entities;

import com.cuatrifasico.battleshi.model.enums.CellState;
import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import com.cuatrifasico.battleshi.model.exceptions.AlreadyShotException;
import com.cuatrifasico.battleshi.model.exceptions.InvalidPlacementException;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a 10x10 board: the grid of {@link Cell} objects and the
 * fleet of {@link Ship} instances placed on it. Encapsulates every
 * placement and shooting rule so that neither the controller nor the
 * view need to know how the grid is internally organized.
 */
public final class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Cell[][] grid;
    private final Set<Ship> fleet;

    /**
     * Creates an empty 10x10 board with every cell set to water.
     */
    public Board() {
        this.grid = new Cell[Coordinate.BOARD_SIZE][Coordinate.BOARD_SIZE];
        for (int row = 0; row < Coordinate.BOARD_SIZE; row++) {
            for (int column = 0; column < Coordinate.BOARD_SIZE; column++) {
                grid[row][column] = new Cell(new Coordinate(row, column));
            }
        }
        this.fleet = new LinkedHashSet<>();
    }

    /**
     * @param coordinate The position to look up.
     * @return The cell located at the given coordinate.
     */
    public Cell getCell(Coordinate coordinate) {
        return grid[coordinate.getRowIndex()][coordinate.getColumnIndex()];
    }

    /**
     * @return An unmodifiable-in-spirit view of every ship placed on this board.
     *         Callers should not mutate the returned collection directly.
     */
    public Set<Ship> getFleet() {
        return fleet;
    }

    /**
     * Validates and places a ship of the given type on the board.
     *
     * @param shipType    The type of ship to place.
     * @param head        The coordinate of the ship's first cell.
     * @param orientation The direction the ship extends towards from {@code head}.
     * @return The newly created and placed {@link Ship}.
     * @throws InvalidPlacementException If the ship would go out of bounds,
     *                                    overlap another ship, or touch an
     *                                    adjacent ship (including diagonally).
     */
    public Ship placeShip(ShipType shipType, Coordinate head, Orientation orientation)
            throws InvalidPlacementException {

        List<Coordinate> projectedCells = projectCells(shipType, head, orientation);
        validatePlacement(projectedCells);

        Set<Coordinate> occupied = new LinkedHashSet<>(projectedCells);
        Ship ship = new Ship(shipType, orientation, occupied);

        for (Coordinate coordinate : occupied) {
            getCell(coordinate).assignShip(ship);
        }
        fleet.add(ship);
        return ship;
    }

    /**
     * Removes a previously placed ship from the board: clears every cell
     * it occupied back to water and drops it from the fleet. Used by the
     * placement phase to "pick up" a ship before moving it elsewhere,
     * and to fully reset the board.
     *
     * @param ship The ship to remove; must currently belong to this board's fleet.
     */
    public void removeShip(Ship ship) {
        for (Coordinate coordinate : ship.getOccupiedCells()) {
            getCell(coordinate).clear();
        }
        fleet.remove(ship);
    }

    /**
     * Non-mutating check for whether a ship of the given type could be
     * placed at the given head/orientation right now, without actually
     * placing it. Used by the placement phase to preview valid/invalid
     * drop targets as the player hovers the board.
     *
     * @param shipType    The ship type to test.
     * @param head        The candidate head coordinate.
     * @param orientation The candidate orientation.
     * @return {@code true} if {@link #placeShip} would currently succeed
     *         with these exact arguments.
     */
    public boolean canPlace(ShipType shipType, Coordinate head, Orientation orientation) {
        try {
            validatePlacement(projectCells(shipType, head, orientation));
            return true;
        } catch (InvalidPlacementException e) {
            return false;
        }
    }

    /**
     * Projects the list of coordinates a ship of the given type and
     * orientation would occupy, starting from its head cell.
     *
     * @param shipType    The ship type (defines how many cells to project).
     * @param head        The starting coordinate.
     * @param orientation The direction to project towards.
     * @return The ordered list of coordinates the ship would occupy.
     * @throws InvalidPlacementException If any projected cell falls outside the board.
     */
    private List<Coordinate> projectCells(ShipType shipType, Coordinate head, Orientation orientation)
            throws InvalidPlacementException {

        List<Coordinate> cells = new java.util.ArrayList<>();
        for (int i = 0; i < shipType.getSize(); i++) {
            int rowDelta = orientation.getRowDelta() * i;
            int columnDelta = orientation.getColumnDelta() * i;
            Coordinate next = head.neighbor(rowDelta, columnDelta);
            if (next == null) {
                throw new InvalidPlacementException(
                        "Ship " + shipType.getDisplayName() + " does not fit on the board from " + head);
            }
            cells.add(next);
        }
        return cells;
    }

    /**
     * Ensures none of the projected cells overlap an existing ship and
     * that no projected cell is orthogonally or diagonally adjacent to
     * an already-placed ship.
     *
     * @param projectedCells The coordinates the candidate ship would occupy.
     * @throws InvalidPlacementException If the placement violates any rule.
     */
    private void validatePlacement(List<Coordinate> projectedCells) throws InvalidPlacementException {
        for (Coordinate coordinate : projectedCells) {
            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                    Coordinate adjacent = coordinate.neighbor(rowOffset, columnOffset);
                    if (adjacent != null && getCell(adjacent).getOccupyingShip() != null) {
                        throw new InvalidPlacementException(
                                "Placement at " + coordinate + " overlaps or touches another ship");
                    }
                }
            }
        }
    }

    /**
     * Fires a shot at the given coordinate, updating the cell's state
     * and, if applicable, the affected ship's hit registry.
     *
     * @param coordinate The target coordinate.
     * @return The resulting {@link CellState}: {@link CellState#MISS},
     *         {@link CellState#HIT} or {@link CellState#SUNK}.
     * @throws AlreadyShotException If the given coordinate was already fired upon.
     */
    public CellState receiveShot(Coordinate coordinate) {
        Cell cell = getCell(coordinate);
        if (cell.isAlreadyShot()) {
            throw new AlreadyShotException("Cell " + coordinate + " was already shot at");
        }

        Ship ship = cell.getOccupyingShip();
        if (ship == null) {
            cell.setState(CellState.MISS);
            return CellState.MISS;
        }

        boolean sunk = ship.registerHit(coordinate);
        if (sunk) {
            for (Coordinate shipCell : ship.getOccupiedCells()) {
                getCell(shipCell).setState(CellState.SUNK);
            }
            return CellState.SUNK;
        }

        cell.setState(CellState.HIT);
        return CellState.HIT;
    }

    /**
     * @return {@code true} if every ship in this board's fleet is sunk.
     */
    public boolean isFleetDestroyed() {
        for (Ship ship : fleet) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return !fleet.isEmpty();
    }
}
