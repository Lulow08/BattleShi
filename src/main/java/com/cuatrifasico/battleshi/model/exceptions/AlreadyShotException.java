package com.cuatrifasico.battleshi.model.exceptions;

/**
 * Thrown when a shot is attempted on a cell that has already been
 * fired upon. This is a programming/UI-guard error rather than a
 * recoverable game rule violation (the view is expected to prevent
 * this from ever reaching the model), so it is unchecked.
 */
public class AlreadyShotException extends RuntimeException {

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message Human-readable explanation of the failure.
     */
    public AlreadyShotException(String message) {
        super(message);
    }
}