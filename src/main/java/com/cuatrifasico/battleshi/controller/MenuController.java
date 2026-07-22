package com.cuatrifasico.battleshi.controller;

import com.cuatrifasico.battleshi.model.entities.GameSession;
import com.cuatrifasico.battleshi.model.exceptions.PersistenceException;
import com.cuatrifasico.battleshi.model.persistence.GamePersistenceManager;
import com.cuatrifasico.battleshi.view.SceneManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

/**
 * Controller for {@code menu-view.fxml}, the application's true entry
 * screen (see {@link com.cuatrifasico.battleshi.model.AppInitializer}).
 * Wires the three top-level actions and decides whether "Continue Game"
 * is available by asking {@link GamePersistenceManager} if a save
 * file already exists on disk.
 */
public class MenuController {

    /** Fallback nickname used when no player file has been saved yet. */
    private static final String DEFAULT_NICKNAME = "PLAYER";

    @FXML
    private Button newGameButton;

    @FXML
    private Button continueGameButton;

    @FXML
    private Button exitButton;

    @FXML
    private void initialize() {
        continueGameButton.setDisable(!GamePersistenceManager.hasSave());

        newGameButton.setOnAction(event -> startNewGame());
        continueGameButton.setOnAction(event -> continueGame());
        exitButton.setOnAction(event -> Platform.exit());
    }

    /**
     * Loads {@code game-view.fxml} fresh (a brand-new {@code Board} and
     * {@link PlacementController} come with it, since {@code GameController}
     * builds them in its own {@code initialize()}), and hands it the
     * player's nickname.
     * <p>
     * Note: {@code menu-view.fxml} has no nickname field of its own yet,
     * so this reuses whatever nickname was last saved (or a default).
     * If a nickname-entry screen gets added later, swap this method's
     * body for whatever value that screen collects.
     */
    private void startNewGame() {
        String nickname = loadNicknameOrDefault();
        GameController gameController = loadGameScene();
        if (gameController != null) {
            gameController.setNickname(nickname);
        }
    }

    private void continueGame() {
        GameSession session = loadSessionOrNull();
        if (session == null) {
            // Save vanished or was corrupted since the menu opened; keep
            // the user on the menu instead of loading an empty match.
            continueGameButton.setDisable(true);
            return;
        }
        GameController gameController = loadGameScene();
        if (gameController != null) {
            gameController.resumeFromSession(session);
        }
    }

    private GameController loadGameScene() {
        try {
            return SceneManager.getInstance().switchScene("game-view.fxml");
        } catch (IOException e) {
            throw new IllegalStateException("Could not load game-view.fxml", e);
        }
    }

    private String loadNicknameOrDefault() {
        try {
            String stored = GamePersistenceManager.loadNickname();
            return stored != null ? stored : DEFAULT_NICKNAME;
        } catch (PersistenceException e) {
            return DEFAULT_NICKNAME;
        }
    }

    private GameSession loadSessionOrNull() {
        try {
            return GamePersistenceManager.loadSession();
        } catch (PersistenceException e) {
            return null;
        }
    }
}
