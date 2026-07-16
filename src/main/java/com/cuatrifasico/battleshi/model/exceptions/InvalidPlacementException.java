package com.cuatrifasico.battleshi.model.exceptions;

/**
 * Thrown when an attempt is made to place a ship in a position that
 * violates placement rules: overlapping another ship, touching an
 * adjacent ship, or falling outside the board boundaries.
 * <p>
 * This is a checked exception because callers (placement controller)
 * are expected to recover from it by asking the player to choose a
 * different position, rather than letting the program crash.
 */
public class InvalidPlacementException extends BattleShiException {

    /**
     * Creates a new exception describing why the placement was rejected.
     *
     * @param message Human-readable explanation of the rule that was violated.
     */
    public InvalidPlacementException(String message) {
        super(message);
    }
}