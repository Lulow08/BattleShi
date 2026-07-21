package com.cuatrifasico.battleshi.model.concurrency;

import com.cuatrifasico.battleshi.model.entities.MachinePlayer;
import com.cuatrifasico.battleshi.model.entities.RandomFleetPlacer;
import javafx.application.Platform;

/**
 * Places the machine player's fleet on a background thread so the
 * JavaFX Application Thread — and therefore the UI — is never blocked
 * while the random placement algorithm runs.
 * <p>
 * <b>Concurrency contract</b> (Thread 2 of 2):
 * <ul>
 *     <li>The {@link RandomFleetPlacer} operates exclusively on
 *     {@code machinePlayer.getOwnBoard()}, which is not yet visible to
 *     any other thread at this point (the human is still in the
 *     placement phase and has no reference to the machine's board).
 *     No external synchronization is therefore needed for the
 *     placement work itself.</li>
 *     <li>Once placement is complete the result is announced via
 *     {@link Platform#runLater}, guaranteeing that the callback runs
 *     on the JavaFX Application Thread and can safely mutate the
 *     scene graph (e.g. enabling the "Play" button once all ships
 *     are placed).</li>
 * </ul>
 * <p>
 * The {@link PlacementReadyCallback} interface decouples this thread
 * from the concrete controller class, following the Dependency
 * Inversion Principle and satisfying the project's requirement for
 * multiple non-domain interfaces.
 */
public final class FleetPlacementThread extends Thread {

    /**
     * Callback invoked on the JavaFX Application Thread once the
     * machine fleet has been placed (successfully or not).
     */
    public interface PlacementReadyCallback {

        /**
         * Called after the background placement attempt completes.
         *
         * @param success {@code true} if the fleet was placed without
         *                errors; {@code false} if an unexpected error
         *                occurred (the controller should show an error
         *                message and abort the game setup in that case).
         */
        void onPlacementReady(boolean success);
    }

    private final MachinePlayer machinePlayer;
    private final PlacementReadyCallback callback;

    /**
     * Creates a new fleet-placement thread.
     *
     * @param machinePlayer The machine player whose board will be
     *                      populated by {@link RandomFleetPlacer}.
     * @param callback      Invoked on the JavaFX thread when done.
     */
    public FleetPlacementThread(MachinePlayer machinePlayer,
                                PlacementReadyCallback callback) {
        super("FleetPlacementThread");
        this.machinePlayer = machinePlayer;
        this.callback = callback;
        setDaemon(true);
    }

    /**
     * Runs {@link RandomFleetPlacer#place(com.cuatrifasico.battleshi.model.entities.Board)}
     * on this background thread and then dispatches the result via
     * {@link Platform#runLater}.
     */
    @Override
    public void run() {
        boolean success;
        try {
            new RandomFleetPlacer().place(machinePlayer.getOwnBoard());
            success = true;
        } catch (Exception e) {
            success = false;
        }

        final boolean finalSuccess = success;
        Platform.runLater(() -> callback.onPlacementReady(finalSuccess));
    }
}