package com.cuatrifasico.battleshi.model.entities;

import com.cuatrifasico.battleshi.model.enums.CellState;

import java.io.Serializable;
import java.util.Set;

/**
 * Immutable record of a single shot fired during the match.
 * Stored in a {@link java.util.LinkedList} as the game's shot history,
 * enabling full serialization and reconstruction of a saved game.
 * <p>
 * Note: this class is intentionally read-only after construction.
 * The history list is append-only; shots are never removed or modified
 * (there is no "undo" mechanic in this game).
 */
public final class Shot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identifies which side fired the shot.
     */
    public enum Shooter{
        /** The human player fired this shot. */
        HUMAN,
        /** The machine player fired this shot. */
        MACHINE
    }

    private final Coordinate coordinate;
    private final CellState result;
    private final Shooter shooter;

    /**
     * Creates a new shot record.
     *
     * @param coordinate The cell that was targeted.
     * @param result     The resulting cell state after the shot
     *                   ({@link CellState#MISS}, {@link CellState#HIT},
     *                   or {@link CellState#SUNK}).
     * @param shooter    Who fired this shot.
     */
    public Shot(Coordinate coordinate, CellState result, Shooter shooter) {
        this.coordinate = coordinate;
        this.result = result;
        this.shooter = shooter;
    }

    /**
     * @return The coordinate that was targeted.
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * @return The cell state that resulted from this shot.
     */
    public CellState getResult() {
        return result;
    }

    /**
     * @return Which side fired this shot.
     */
    public Shooter getShooter() {
        return shooter;
    }

    @Override
    public String toString() {
        return shooter + " shot " + coordinate + " → " + result;
    }
}
