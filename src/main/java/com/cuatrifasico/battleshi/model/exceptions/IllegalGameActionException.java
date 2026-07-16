package com.cuatrifasico.battleshi.model.exceptions;

/**
 * Thrown when an action that requires an active turn (e.g. firing a
 * shot) is attempted while the match has already finished, or while
 * it is not the acting player's turn. Represents a control-flow
 * misuse rather than a recoverable rule violation, hence unchecked.
 */
public class IllegalGameActionException extends RuntimeException {

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message Human-readable explanation of the failure.
     */
    public IllegalGameActionException(String message) {
        super(message);
    }
}