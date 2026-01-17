package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class SceneController {

    public void switch_Tabela(ActionEvent event) throws IOException {
        System.out.println("Przechodzenie do tabel");
        Parent root = FXMLLoader.load(getClass().getResource("/Tabela_Wyswietlanie.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
    public void switch_Updachuj(ActionEvent event) throws IOException {
        System.out.println("Przechodzenie do tabel");
        Parent root = FXMLLoader.load(getClass().getResource("/Updatuj.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
    public void switch_Drop(ActionEvent event) throws IOException {
        System.out.println("Przechodzenie do tabel");
        Parent root = FXMLLoader.load(getClass().getResource("/Usuwanie_tabeli.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
    public void switch_Delete(ActionEvent event) throws IOException {
        System.out.println("Przechodzenie do tabel");
        Parent root = FXMLLoader.load(getClass().getResource("/Delete.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
    public void switch_Alter_natywka(ActionEvent event) throws IOException {
        System.out.println("Przechodzenie do tabel");
        Parent root = FXMLLoader.load(getClass().getResource("/Alternatywka.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
    public void switch_create(ActionEvent event) throws IOException {
        System.out.println("Przechodzenie do tabel");
        Parent root = FXMLLoader.load(getClass().getResource("/Create.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
    public void switch_insert(ActionEvent event) throws IOException {
        System.out.println("Przechodzenie do tabel");
        Parent root = FXMLLoader.load(getClass().getResource("/Insert.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Nie znaleziono pliku CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.show();
    }
}