package com.cuatrifasico.battleshi.view;

import com.cuatrifasico.battleshi.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Singleton class responsible for managing the primary JavaFX {@link Stage}
 * and handling scene transitions throughout the application.
 */
public final class SceneManager {

    private static SceneManager instance;
    private Stage mainStage;

    private SceneManager() {
    }

    /**
     * Retrieves the singleton instance of the SceneManager.
     *
     * @return The active {@link SceneManager} instance.
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Assigns the primary stage for the application.
     *
     * @param stage The primary JavaFX {@link Stage}.
     */
    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }

    /**
     * Loads application-specific custom fonts from the classpath.
     */
    public void loadFonts() {
        // Font.loadFont(getClass().getResourceAsStream("/fonts/FontName.otf"), 14);
    }

    /**
     * Switches the current scene on the main stage to the specified FXML view.
     *
     * @param fxmlFile The name of the FXML file to load (e.g., "menu-view.fxml").
     * @param <SceneController> The expected controller type for the loaded FXML.
     * @return The controller instance associated with the loaded FXML view.
     * @throws IOException If the FXML file cannot be found or parsed.
     */
    public <SceneController> SceneController switchScene(String fxmlFile) throws IOException {
        URL fxmlUrl = Main.class.getResource("/fxml/" + fxmlFile);
        if (fxmlUrl == null) {
            throw new IOException("Resource not found: " + fxmlFile);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        URL cssUrl = Main.class.getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        mainStage.setScene(scene);
        mainStage.show();
        root.requestFocus();

        return loader.getController();
    }
}
