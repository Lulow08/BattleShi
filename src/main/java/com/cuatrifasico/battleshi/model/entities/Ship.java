package com.cuatrifasico.battleshi.model.entities;

import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a single ship placed on a board: its type, orientation,
 * the coordinates it occupies and which of those coordinates have
 * already been hit. A ship is considered sunk once every occupied
 * coordinate has been hit.
 */
public final class Ship implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ShipType shipType;
    private final Set<Coordinate> occupiedCells;
    private final Set<Coordinate> hitCells;
    private Orientation orientation;

    /**
     * Creates a ship of the given type occupying the given coordinates.
     *
     * @param shipType      The ship category (defines its size).
     * @param orientation   The orientation the ship was placed with.
     * @param occupiedCells The ordered set of coordinates this ship occupies,
     *                      must match {@code shipType.getSize()} in count.
     */
    public Ship(ShipType shipType, Orientation orientation, Set<Coordinate> occupiedCells) {
        this.shipType = shipType;
        this.orientation = orientation;
        this.occupiedCells = new LinkedHashSet<>(occupiedCells);
        this.hitCells = new LinkedHashSet<>();
    }

    /**
     * @return The ship's category.
     */
    public ShipType getShipType() {
        return shipType;
    }

    /**
     * @return The ship's current orientation.
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * @return An unmodifiable view of every coordinate occupied by this ship.
     */
    public Set<Coordinate> getOccupiedCells() {
        return Collections.unmodifiableSet(occupiedCells);
    }

    /**
     * Registers a hit on the given coordinate.
     *
     * @param coordinate The coordinate that was fired upon, must belong to this ship.
     * @return {@code true} if this hit caused the ship to become fully sunk.
     */
    public boolean registerHit(Coordinate coordinate) {
        hitCells.add(coordinate);
        return isSunk();
    }

    /**
     * @param coordinate The coordinate to check.
     * @return {@code true} if this ship occupies the given coordinate.
     */
    public boolean occupies(Coordinate coordinate) {
        return occupiedCells.contains(coordinate);
    }

    /**
     * @return {@code true} if every occupied coordinate has been hit.
     */
    public boolean isSunk() {
        return hitCells.containsAll(occupiedCells);
    }
}