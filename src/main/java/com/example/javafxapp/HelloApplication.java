package com.example.javafxapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.javafxapp.util.Logger;

public class HelloApplication extends Application {
    private static final String FXML_PATH = "/com/example/javafxapp/main-view.fxml";
    private static final String CSS_PATH = "/com/example/javafxapp/styles.css";
    private static final String TITLE = "Aplikacja do obróbki obrazów - PWr";
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger.getInstance().log("INFO", "Uruchamianie aplikacji");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(FXML_PATH));
        Parent root = loader.load();

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setOnCloseRequest(e -> {
            Logger.getInstance().log("INFO", "Zamykanie aplikacji");
            System.exit(0);
        });
        primaryStage.show();

        Logger.getInstance().log("INFO", "Aplikacja uruchomiona pomyślnie");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
