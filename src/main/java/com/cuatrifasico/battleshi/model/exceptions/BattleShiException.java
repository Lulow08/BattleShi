package com.cuatrifasico.battleshi.model.exceptions;

/**
 * Base checked exception for every domain-level error that the
 * Battle Shi game can raise. Concrete subclasses describe more
 * specific failure situations (invalid ship placement, persistence
 * failures, etc.).
 */
public class BattleShiException extends Exception {

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message Human-readable explanation of the failure.
     */
    public BattleShiException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a descriptive message and an underlying cause.
     *
     * @param message Human-readable explanation of the failure.
     * @param cause   The original exception that triggered this one.
     */
    public BattleShiException(String message, Throwable cause) {
        super(message, cause);
    }
}