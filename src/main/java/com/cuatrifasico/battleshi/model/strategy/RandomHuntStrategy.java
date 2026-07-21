package com.cuatrifasico.battleshi.model.strategy;

import com.cuatrifasico.battleshi.model.entities.Board;
import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.model.enums.CellState;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Machine shot strategy that combines a pure-random phase with a
 * "hunt &amp; target" phase:
 * <ol>
 *     <li><b>Random phase:</b> when there is no active hit being pursued,
 *     the strategy picks a random cell that has not been shot at yet.</li>
 *     <li><b>Hunt phase:</b> when the last shot was a {@link CellState#HIT},
 *     the strategy enqueues the four orthogonal neighbors of that hit
 *     (filtering out out-of-bounds and already-shot cells) and then fires
 *     randomly among those candidates.  If a candidate misses it is
 *     discarded and the next candidate is tried; if it hits, its own
 *     orthogonal neighbors are added to the queue as well.  When the
 *     ship is finally sunk the queue is cleared and the strategy returns
 *     to the random phase.</li>
 * </ol>
 * <p>
 * This class implements {@link Serializable} so the entire machine state
 * is preserved when the game is saved mid-match.
 */
public final class RandomHuntStrategy implements IMachineShotStrategy, Serializable {

    private static final long serialVersionUID = 1L;

    /** Orthogonal direction deltas: up, down, left, right. */
    private static final int[][] ORTHO_DELTAS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    /**
     * Candidate cells to try while in hunt mode.
     * Using an {@link ArrayDeque} gives O(1) add/poll at both ends and
     * counts as one of the four required non-array data structures.
     */
    private final Deque<Coordinate> huntQueue = new ArrayDeque<>();

    private final Random random = new Random();

    /**
     * Selects the next coordinate to fire at.
     * <p>
     * If {@link #huntQueue} is non-empty the strategy pops candidates
     * from it (skipping any that have already been shot) until it finds
     * one that is still valid, then fires there.  If the queue is empty
     * (or becomes empty after filtering) the strategy falls back to a
     * random unshot cell.
     *
     * @param targetBoard The human player's board, consulted only to
     *                     check which cells have already been shot at.
     * @return The coordinate to fire at next; never {@code null}.
     */
    @Override
    public Coordinate selectNextShot(Board targetBoard) {
        // --- Hunt phase: pop a valid candidate from the queue ----------
        while (!huntQueue.isEmpty()) {
            Coordinate candidate = huntQueue.poll();
            if (!targetBoard.getCell(candidate).isAlreadyShot()) {
                return candidate;
            }
        }

        // --- Random phase: pick a random unshot cell -------------------
        return pickRandomUnshot(targetBoard);
    }

    /**
     * Updates the hunt queue based on the outcome of the last shot.
     * <ul>
     *     <li>{@link CellState#HIT}: the four orthogonal neighbors of
     *     {@code shotCoordinate} that are in-bounds and not yet shot are
     *     added to the queue, giving the strategy new candidates to
     *     pursue in the next turns.</li>
     *     <li>{@link CellState#SUNK}: the hunt is over; the queue is
     *     cleared unconditionally so the strategy returns to random
     *     mode on the next call.</li>
     *     <li>{@link CellState#MISS}: nothing changes; if the queue
     *     still has entries the hunt continues, otherwise random mode
     *     resumes naturally.</li>
     * </ul>
     *
     * @param shotCoordinate The coordinate that was just fired at.
     * @param result         The resulting cell state.
     */
    @Override
    public void registerResult(Coordinate shotCoordinate, CellState result) {
        if (result == CellState.SUNK) {
            huntQueue.clear();
        } else if (result == CellState.HIT) {
            enqueueNeighbors(shotCoordinate);
        }
        // MISS: leave the queue as-is (keep hunting if queue is non-empty)
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    /**
     * Adds the orthogonal neighbors of {@code center} to the hunt queue.
     * Neighbors that fall outside the board are filtered via
     * {@link Coordinate#neighbor(int, int)}'s null-return contract.
     * Already-queued duplicates are accepted (they will be silently
     * skipped by the {@code isAlreadyShot} check in
     * {@link #selectNextShot}).
     *
     * @param center The coordinate whose neighbors should be enqueued.
     */
    private void enqueueNeighbors(Coordinate center) {
        for (int[] delta : ORTHO_DELTAS) {
            Coordinate neighbor = center.neighbor(delta[0], delta[1]);
            if (neighbor != null) {
                huntQueue.add(neighbor);
            }
        }
    }

    /**
     * Collects every cell on the target board that has not been shot at,
     * then returns one of them chosen uniformly at random.
     *
     * @param targetBoard The board to scan.
     * @return A random unshot coordinate.
     * @throws IllegalStateException If every cell on the board has already
     *                               been shot at (should never happen in a
     *                               valid game since the game ends when all
     *                               ships are sunk first).
     */
    private Coordinate pickRandomUnshot(Board targetBoard) {
        List<Coordinate> candidates = new ArrayList<>(Coordinate.BOARD_SIZE * Coordinate.BOARD_SIZE);
        for (int row = 0; row < Coordinate.BOARD_SIZE; row++) {
            for (int col = 0; col < Coordinate.BOARD_SIZE; col++) {
                Coordinate c = new Coordinate(row, col);
                if (!targetBoard.getCell(c).isAlreadyShot()) {
                    candidates.add(c);
                }
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No unshot cells remain — the game should have ended already.");
        }
        return candidates.get(random.nextInt(candidates.size()));
    }
}