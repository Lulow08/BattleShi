package com.cuatrifasico.battleshi;

import com.cuatrifasico.battleshi.model.AppInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application entry point for the runtime framework compilation layer.
 */
public class Main extends Application {

    /**
     * System execution vector executing programmatic process instructions.
     *
     * @param args Array of linear execution input parameters.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Framework callback signaling runtime startup completion structures.
     *
     * @param mainStage Root display window initialization component context.
     * @throws IOException If filesystem asset paths fail resolution checks.
     */
    @Override
    public void start(Stage mainStage) throws IOException {
        new AppInitializer().start(mainStage);
    }
}
