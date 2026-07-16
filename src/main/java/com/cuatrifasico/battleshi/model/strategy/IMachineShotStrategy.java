package com.cuatrifasico.battleshi.model.strategy;

import com.cuatrifasico.battleshi.model.entities.Coordinate;
import com.cuatrifasico.battleshi.model.enums.CellState;

/**
 * Defines how the machine player decides where to shoot next.
 * Implementations may keep internal state between calls (e.g. a
 * queue of candidate cells to try after a hit), which is why the
 * strategy is notified of each shot's outcome via {@link #registerResult}.
 * <p>
 * Depending on {@link com.cuatrifasico.battleshi.model.entities.Board}
 * through this interface (rather than a concrete class) is what lets
 * {@link com.cuatrifasico.battleshi.model.entities.MachinePlayer} stay
 * decoupled from the exact targeting algorithm (Strategy pattern, and
 * Dependency Inversion in practice).
 */
public interface IMachineShotStrategy {

    /**
     * Chooses the next coordinate to fire at, on the given target board.
     *
     * @param targetBoard The human player's board, used only to know
     *                     which cells have already been shot at.
     * @return The coordinate the machine will fire at next.
     */
    Coordinate selectNextShot(com.cuatrifasico.battleshi.model.entities.Board targetBoard);

    /**
     * Notifies the strategy of the outcome of its last shot, so it can
     * update its internal targeting state (e.g. queue neighboring cells
     * after a hit, or clear them once a ship is sunk).
     *
     * @param shotCoordinate The coordinate that was just fired at.
     * @param result         The resulting state of that cell.
     */
    void registerResult(Coordinate shotCoordinate, CellState result);
}