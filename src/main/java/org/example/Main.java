package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean isConnected = DatabaseConnection.testConnection();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1920, 1080);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}