package com.cuatrifasico.battleshi.model;

import com.cuatrifasico.battleshi.view.SceneManager;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Handles the application's bootstrapping sequence and initial stage preparation.
 */
public class AppInitializer {

    private static final String APP_TITLE   = "Battle Shi";
    private static final String ENTRY_SCENE = "game-view.fxml";

    /**
     * Configures the window parameters, registers the scene infrastructure, and sets the initial view.
     *
     * @param stage The primary stage provided by the runtime environment launcher.
     * @throws IOException If the default entry scene template file fails to map correctly.
     */
    public void start(Stage stage) throws IOException {
        stage.setTitle(APP_TITLE);
        stage.setResizable(false);

        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setMainStage(stage);
        sceneManager.loadFonts();
        sceneManager.switchScene(ENTRY_SCENE);
    }
}