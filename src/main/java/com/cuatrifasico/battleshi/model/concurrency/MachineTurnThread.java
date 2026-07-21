package com.cuatrifasico.battleshi.model.concurrency;

import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.model.entities.GameSession;
import com.cuatrifasico.battleshi.model.entities.Shot;
import com.cuatrifasico.battleshi.model.enums.CellState;
import com.cuatrifasico.battleshi.model.enums.GameState;
import javafx.application.Platform;

/**
 * Executes the machine player's turn on a dedicated background thread so
 * the JavaFX Application Thread is never blocked during the artificial
 * "thinking" delay.
 * <p>
 * <b>Concurrency contract</b> (Thread 1 of 2):
 * <ul>
 *     <li>All mutations to the shared {@link GameSession} (firing the
 *     shot, recording it in the history, toggling the turn flag) are
 *     performed inside a {@code synchronized (session)} block, using
 *     the session itself as the monitor.  The human player's click
 *     handler in {@code GameController} must acquire the same monitor
 *     before reading or writing session state, eliminating the race
 *     between the two threads.</li>
 *     <li>UI updates are dispatched via {@link Platform#runLater} after
 *     the lock is released, honouring the JavaFX rule that scene-graph
 *     mutations must occur on the Application Thread.</li>
 *     <li>This thread is a daemon thread; it will not prevent the JVM
 *     from shutting down if the window is closed abruptly.</li>
 * </ul>
 * <p>
 * The inner {@link MachineActionCallback} interface decouples this
 * thread from the concrete controller, satisfying the Dependency
 * Inversion Principle and the project's requirement for multiple
 * non-domain interfaces beyond the model layer.
 */
public final class MachineTurnThread extends Thread {

    /** Simulated "thinking" pause before the machine fires, in milliseconds. */
    private static final long THINK_DELAY_MS = 900;

    /**
     * Callback invoked on the JavaFX Application Thread once the
     * machine has fired its shot and the model has been updated.
     */
    public interface MachineActionCallback {

        /**
         * Notifies the controller of the machine shot's outcome.
         *
         * @param coordinate The cell the machine fired at.
         * @param result     The resulting cell state
         *                   ({@link CellState#MISS}, {@link CellState#HIT},
         *                   or {@link CellState#SUNK}).
         * @param gameOver   {@code true} if the machine's fleet destroyed
         *                   the human fleet with this shot.
         */
        void onMachineShot(Coordinate coordinate, CellState result, boolean gameOver);
    }

    private final GameSession session;
    private final MachineActionCallback callback;

    /**
     * Creates a new machine-turn thread.
     *
     * @param session  The shared game session, also used as the
     *                 synchronization monitor.
     * @param callback Controller callback invoked on the JavaFX thread.
     */
    public MachineTurnThread(GameSession session, MachineActionCallback callback) {
        super("MachineTurnThread");
        this.session = session;
        this.callback = callback;
        setDaemon(true);
    }

    /**
     * Sleeps for {@link #THINK_DELAY_MS}, then acquires the session
     * monitor, fires the machine's shot, updates the model, and releases
     * the monitor before dispatching the UI update.
     * <p>
     * A defensive check inside the critical section verifies that the
     * game is still in progress and that it is still the machine's turn
     * before acting — guarding against edge cases where the user
     * navigates away while the thread is sleeping.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(THINK_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        Coordinate shotAt;
        CellState result;
        boolean gameOver;

        // ---- Critical section: all model mutations under session lock ----
        synchronized (session) {
            if (session.isHumanTurn()
                    || session.getGameState() != GameState.IN_PROGRESS) {
                return; // Turn changed while sleeping — abort silently.
            }

            shotAt = session.getMachinePlayer()
                    .takeShot(session.getHumanPlayer().getOwnBoard());

            result = session.getHumanPlayer()
                    .getOwnBoard()
                    .getCell(shotAt)
                    .getState();

            session.recordShot(new Shot(shotAt, result, Shot.Shooter.MACHINE));

            gameOver = session.getHumanPlayer().getOwnBoard().isFleetDestroyed();
            if (gameOver) {
                session.setGameState(GameState.MACHINE_WON);
            } else if (result == CellState.MISS) {
                // Miss: hand the turn back to the human.
                session.setHumanTurn(true);
            }
            // HIT or SUNK and not game over: machine shoots again.
            // The controller is responsible for spawning a new thread.
        }
        // ---- End critical section ----------------------------------------

        final Coordinate finalShotAt   = shotAt;
        final CellState  finalResult   = result;
        final boolean    finalGameOver = gameOver;

        Platform.runLater(() ->
                callback.onMachineShot(finalShotAt, finalResult, finalGameOver));
    }
}