package com.cuatrifasico.battleshi.model.entities;

import java.io.Serializable;

/**
 * Base class shared by every kind of player in the game. Holds the
 * player's own board (where their fleet is placed) and their nickname.
 * Concrete subclasses ({@link HumanPlayer}, {@link MachinePlayer})
 * only differ in how they decide where to shoot next, which is
 * delegated to a shooting strategy rather than implemented here
 * (see the {@code model.strategy} package), keeping this class
 * focused on player identity and board ownership (Single Responsibility).
 */
public abstract class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final Board ownBoard;

    /**
     * Creates a player with the given nickname and an empty board.
     *
     * @param nickname The display name for this player.
     */
    protected Player(String nickname) {
        this.nickname = nickname;
        this.ownBoard = new Board();
    }

    /**
     * @return This player's display name.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @return The board where this player's own fleet is placed.
     */
    public Board getOwnBoard() {
        return ownBoard;
    }
}