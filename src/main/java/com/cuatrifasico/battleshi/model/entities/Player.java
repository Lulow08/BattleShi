package com.cuatrifasico.battleshi.model.entities;

import java.io.Serial;
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

    @Serial
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
     * Creates a player with the given nickname wrapped around an
     * already-existing board. Used when the board was built and
     * populated before the player object itself needed to exist (e.g.
     * the human's board is filled in during the placement phase,
     * before a {@code HumanPlayer} is constructed to enter combat).
     *
     * @param nickname The display name for this player.
     * @param ownBoard The board this player will use, as-is.
     */
    protected Player(String nickname, Board ownBoard) {
        this.nickname = nickname;
        this.ownBoard = ownBoard;
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