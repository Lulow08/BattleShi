package com.cuatrifasico.battleshi.model.entities;

import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import com.cuatrifasico.battleshi.model.exceptions.InvalidPlacementException;

import java.util.Random;

/**
 * Populates a {@link Board} with a complete, valid fleet using a
 * random placement algorithm.
 * <p>
 * Ships are placed one type at a time, in fleet-count order as defined
 * by {@link ShipType}.  For each ship, the placer draws a random
 * {@link Orientation} and a random {@link Coordinate} head position and
 * delegates validation entirely to {@link Board#placeShip}.  If that
 * call raises {@link InvalidPlacementException} (out-of-bounds or
 * adjacency/overlap conflict) the attempt is simply retried until a
 * valid position is found.
 * <p>
 * This class follows the <em>Builder</em> pattern in spirit: it
 * constructs (fills) a complex object step by step, keeping the
 * construction logic separate from the {@link Board} class itself
 * (Single Responsibility).  It is intentionally stateless between
 * calls — each {@link #place(Board)} invocation operates independently.
 * <p>
 * The placement loop is guaranteed to terminate in practice because
 * a standard 10×10 Battleship fleet always fits on a 10×10 grid with
 * the enforced spacing rules.  A safety counter prevents infinite loops
 * during unit testing or if future rule changes make the fleet too
 * large.
 */
public final class RandomFleetPlacer {

    /** Maximum placement attempts per individual ship before giving up. */
    private static final int MAX_ATTEMPTS_PER_SHIP = 1_000;

    private final Random random;

    /**
     * Creates a placer with an internally managed {@link Random} source.
     */
    public RandomFleetPlacer() {
        this.random = new Random();
    }

    /**
     * Creates a placer with the given {@link Random} source, useful for
     * seeding deterministic tests.
     *
     * @param random The random number generator to use.
     */
    public RandomFleetPlacer(Random random) {
        this.random = random;
    }

    /**
     * Places one ship of every type in the fleet onto {@code board},
     * choosing random orientations and positions while respecting all
     * of {@link Board#placeShip}'s validation rules.
     *
     * @param board The board to populate.  Must be empty (no prior ships).
     * @throws IllegalStateException If the maximum attempt count is exceeded
     *                               for any single ship, indicating a
     *                               configuration that cannot be satisfied.
     */
    public void place(Board board) {
        for (ShipType type : ShipType.values()) {
            for (int count = 0; count < type.getFleetCount(); count++) {
                placeOneShip(board, type);
            }
        }
    }

    /**
     * Attempts to place a single ship of {@code type} on {@code board}
     * by trying random positions until {@link Board#placeShip} accepts one.
     *
     * @param board The board to place the ship on.
     * @param type  The ship type to place.
     * @throws IllegalStateException If {@link #MAX_ATTEMPTS_PER_SHIP} is
     *                               exceeded without finding a valid placement.
     */
    private void placeOneShip(Board board, ShipType type) {
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS_PER_SHIP) {
            Orientation orientation = randomOrientation();
            Coordinate head = randomCoordinate();
            try {
                board.placeShip(type, head, orientation);
                return; // success
            } catch (InvalidPlacementException ignored) {
                // Position was invalid; try again
            }
            attempts++;
        }
        throw new IllegalStateException(
                "Could not place " + type.getDisplayName()
                        + " after " + MAX_ATTEMPTS_PER_SHIP + " attempts.");
    }

    /**
     * @return A uniformly random {@link Orientation}.
     */
    private Orientation randomOrientation() {
        return random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    }

    /**
     * @return A uniformly random {@link Coordinate} anywhere on the board.
     */
    private Coordinate randomCoordinate() {
        int row = random.nextInt(Coordinate.BOARD_SIZE);
        int col = random.nextInt(Coordinate.BOARD_SIZE);
        return new Coordinate(row, col);
    }
}
