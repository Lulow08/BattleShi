package com.cuatrifasico.battleshi.model.exceptions;

/**
 * Thrown when reading or writing persistence files fails, whether it
 * is the serialized save game or the plain-text player data file.
 * <p>
 * Declared checked so persistence callers are forced to handle I/O
 * failures explicitly (e.g. falling back to a fresh game instead of
 * crashing the application).
 */
public class PersistenceException extends BattleShiException {

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message Human-readable explanation of the failure.
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Creates a new exception wrapping a lower-level I/O cause.
     *
     * @param message Human-readable explanation of the failure.
     * @param cause   The original exception (typically an {@link java.io.IOException}).
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}