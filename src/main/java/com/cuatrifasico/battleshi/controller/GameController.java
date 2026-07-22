package com.cuatrifasico.battleshi.controller;

import com.cuatrifasico.battleshi.model.entities.Board;
import com.cuatrifasico.battleshi.model.entities.GameSession;
import com.cuatrifasico.battleshi.view.board.BoardGridView;
import com.cuatrifasico.battleshi.view.board.ShipTrayView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Controller for {@code game-view.fxml}. Wires the procedurally-built
 * board and fleet tray views into their FXML placeholders, toggles the
 * instructions overlay, and drives the ship-placement phase through
 * {@link PlacementController}. Turn logic and shot handling for the
 * combat phase are still to be added on top of this scaffolding.
 */
public class GameController {

    @FXML
    private Label playerNicknameLabel;

    @FXML
    private Pane playerBoardContainer;

    @FXML
    private Pane opponentBoardContainer;

    @FXML
    private HBox fleetTrayContainer;

    @FXML
    private Button restartButton;

    @FXML
    private Button playButton;

    @FXML
    private Button viewEnemyButton;

    @FXML
    private Button exitButton;

    @FXML
    private StackPane instructionsOverlay;

    @FXML
    private Button instructionsOkButton;

    @FXML
    private StackPane resultOverlay;

    @FXML
    private Label resultTitleLabel;

    @FXML
    private Button resultMenuButton;

    @FXML
    private Button resultReplayButton;

    private BoardGridView playerBoardView;
    private BoardGridView opponentBoardView;
    private Board playerBoard;
    private PlacementController placementController;

    /**
     * Called automatically by the FXML loader once every {@code @FXML}
     * field has been injected.
     */
    @FXML
    private void initialize() {
        playerBoardView = new BoardGridView();
        playerBoardContainer.getChildren().add(playerBoardView.getRootNode());

        opponentBoardView = new BoardGridView();
        opponentBoardContainer.getChildren().add(opponentBoardView.getRootNode());

        ShipTrayView fleetTray = new ShipTrayView();
        fleetTrayContainer.getChildren().add(fleetTray.getRootNode());

        playerBoard = new Board();
        placementController = new PlacementController(
                playerBoard, playerBoardView, fleetTray, restartButton, playButton);

        instructionsOkButton.setOnAction(event -> hideInstructionsOverlay());
    }

    /**
     * Sets the nickname shown above the player's board. Called by
     * {@link MenuController} right after loading this view.
     *
     * @param nickname The nickname to display.
     */
    public void setNickname(String nickname) {
        playerNicknameLabel.setText(nickname);
    }

    /**
     * Entry point for resuming a previously saved match. For now this
     * only restores the player's nickname and keeps "Play" disabled,
     * since placement is already finished by definition in a saved
     * {@code IN_PROGRESS} session. Repainting the loaded fleet, the shot
     * history, and the active turn belongs to whichever teammate wires
     * up the combat/turn phase — left here as a clearly marked
     * extension point rather than guessed at.
     *
     * @param session The session loaded from disk.
     */
    public void resumeFromSession(GameSession session) {
        setNickname(session.getHumanPlayer().getNickname());
        playButton.setDisable(true);
        // TODO(combat phase owner): repaint session.getHumanPlayer().getOwnBoard()'s
        // fleet onto playerBoardView, replay session.getShotHistory() onto both
        // boards, and set the correct turn indicator before enabling input.
    }

    private void hideInstructionsOverlay() {
        instructionsOverlay.setVisible(false);
        instructionsOverlay.setManaged(false);
    }
}
