package com.cuatrifasico.battleshi.model.enums;

/**
 * Represents the overall phase the current match is in.
 * Used to control which actions are valid at a given moment
 * and whether a saved game can be resumed.
 */
public enum GameState {

    /** The human player is placing ships on their own board. */
    PLACEMENT,

    /** Both fleets are placed and shots are being exchanged. */
    IN_PROGRESS,

    /** The human player sank the entire enemy fleet. */
    PLAYER_WON,

    /** The machine player sank the entire human fleet. */
    MACHINE_WON
}