package com.cuatrifasico.battleshi.controller;

import com.cuatrifasico.battleshi.view.board.BoardGridView;
import com.cuatrifasico.battleshi.view.board.ShipTrayView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Controller for {@code game-view.fxml}. At this stage it is only
 * responsible for wiring the procedurally-built board and fleet tray
 * views into their FXML placeholders, and toggling the instructions
 * overlay. Turn logic, shot handling, and ship placement will be
 * added incrementally on top of this scaffolding.
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

        instructionsOkButton.setOnAction(event -> hideInstructionsOverlay());
    }

    private void hideInstructionsOverlay() {
        instructionsOverlay.setVisible(false);
        instructionsOverlay.setManaged(false);
    }
}