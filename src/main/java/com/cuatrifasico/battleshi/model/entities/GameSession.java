package com.cuatrifasico.battleshi.model.entities;

import com.cuatrifasico.battleshi.model.enums.GameState;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Serializable snapshot of a complete, in-progress Battle Shi match.
 * <p>
 * This is the single object written to (and read from) the binary
 * {@code .dat} save file.  It groups every piece of mutable state
 * that must survive an application restart:
 * <ul>
 *     <li>Both players (with their {@link Board} instances embedded).</li>
 *     <li>The current {@link GameState}.</li>
 *     <li>A flag indicating whose turn it is.</li>
 *     <li>The full shot history as a {@link LinkedList} of {@link Shot}
 *         objects (one of the four required non-array data structures).</li>
 * </ul>
 * <p>
 * Only matches in state {@link GameState#IN_PROGRESS} are persisted;
 * {@link GameState#PLACEMENT}, {@link GameState#PLAYER_WON} and
 * {@link GameState#MACHINE_WON} are never written to disk (see the
 * save/load rules in the project specification).
 */
public final class GameSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HumanPlayer humanPlayer;
    private final MachinePlayer machinePlayer;
    private GameState gameState;
    private boolean humanTurn;

    /**
     * Ordered history of every shot fired in the match, used to
     * reconstruct the visual board state after loading a saved game.
     * {@link LinkedList} is chosen here to satisfy the project's
     * requirement for four distinct non-array data structures; it
     * gives O(1) append (the only operation needed during play) and
     * preserves insertion order for replay.
     */
    private final LinkedList<Shot> shotHistory;

    /**
     * Creates a new session from the two players, the starting state,
     * and the starting turn.
     *
     * @param humanPlayer   The human player (board already populated during placement).
     * @param machinePlayer The machine player (board pre-populated with random fleet).
     * @param gameState     The initial game state; must be
     *                      {@link GameState#IN_PROGRESS} before saving.
     * @param humanTurn     {@code true} if it is the human's turn first.
     */
    public GameSession(HumanPlayer humanPlayer,
                       MachinePlayer machinePlayer,
                       GameState gameState,
                       boolean humanTurn) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.gameState = gameState;
        this.humanTurn = humanTurn;
        this.shotHistory = new LinkedList<>();
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                           //
    // ------------------------------------------------------------------ //

    /**
     * @return The human player.
     */
    public HumanPlayer getHumanPlayer() {
        return humanPlayer;
    }

    /**
     * @return The machine player.
     */
    public MachinePlayer getMachinePlayer() {
        return machinePlayer;
    }

    /**
     * @return The current game state.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Updates the game state (e.g. to {@link GameState#PLAYER_WON} or
     * {@link GameState#MACHINE_WON} when the match ends).
     *
     * @param gameState The new state.
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * @return {@code true} if it is currently the human player's turn.
     */
    public boolean isHumanTurn() {
        return humanTurn;
    }

    /**
     * Switches the active turn between human and machine.
     */
    public void toggleTurn() {
        this.humanTurn = !this.humanTurn;
    }

    /**
     * Sets which player's turn it is explicitly (used when loading a saved session).
     *
     * @param humanTurn {@code true} to make it the human's turn.
     */
    public void setHumanTurn(boolean humanTurn) {
        this.humanTurn = humanTurn;
    }

    /**
     * Appends a shot to the history log.
     *
     * @param shot The shot to record.
     */
    public void recordShot(Shot shot) {
        shotHistory.add(shot);
    }

    /**
     * @return The full shot history in chronological order (unmodifiable view).
     */
    public LinkedList<Shot> getShotHistory() {
        return shotHistory;
    }
}