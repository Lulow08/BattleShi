package com.cuatrifasico.battleshi.model.entities;

import com.cuatrifasico.battleshi.model.enums.CellState;
import com.cuatrifasico.battleshi.model.strategy.IMachineShotStrategy;

/**
 * Represents the computer-controlled opponent. Delegates the decision
 * of where to shoot to an injected {@link IMachineShotStrategy}
 * (Strategy pattern), so the targeting algorithm (random shots,
 * hunt-and-target after a hit, etc.) can be swapped or extended
 * without modifying this class (Open/Closed Principle).
 */
public final class MachinePlayer extends Player {

    private static final long serialVersionUID = 1L;

    private final IMachineShotStrategy shotStrategy;

    /**
     * Creates the machine player with the given nickname and targeting strategy.
     *
     * @param nickname     The display name for the machine player.
     * @param shotStrategy The strategy used to pick and refine shot targets.
     */
    public MachinePlayer(String nickname, IMachineShotStrategy shotStrategy) {
        super(nickname);
        this.shotStrategy = shotStrategy;
    }

    /**
     * Fires a shot on the given target board, choosing the coordinate
     * via this player's strategy and reporting the outcome back to it.
     *
     * @param targetBoard The human player's board to fire upon.
     * @return The coordinate that was fired at.
     */
    public Coordinate takeShot(Board targetBoard) {
        Coordinate target = shotStrategy.selectNextShot(targetBoard);
        CellState result = targetBoard.receiveShot(target);
        shotStrategy.registerResult(target, result);
        return target;
    }
}