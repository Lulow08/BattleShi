package com.cuatrifasico.battleshi.model.entities;

/**
 * Represents the human player. Shots are triggered directly by mouse
 * events on the controller/view layer, so this class adds no
 * automatic shooting behavior of its own beyond what {@link Player}
 * already provides.
 */
public final class HumanPlayer extends Player {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the human player with the given nickname.
     *
     * @param nickname The display name chosen by the user.
     */
    public HumanPlayer(String nickname) {
        super(nickname);
    }
}