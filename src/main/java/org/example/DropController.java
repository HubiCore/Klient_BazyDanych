package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

import java.sql.Connection;
import java.sql.SQLException;

public class DropController {
    private ZooController zooController;
    @FXML
    private ChoiceBox<String> tableChoiceBox;
    @FXML
    public void initialize() {
        try {
            zooController = new ZooController();
            tableChoiceBox.getItems().addAll("Pracownicy", "Bilet", "Klienci", "Wybiegi", "Klatki", "Karmienia");
            tableChoiceBox.setValue("Pracownicy");
            tableChoiceBox.setOnAction(event -> {String selectedTable = tableChoiceBox.getValue();});
        } catch (Exception e) {
            System.out.println("Błąd inicjalizacji Nie można zainicjalizować kontrolera:\n" + e.getMessage());
            e.printStackTrace();
        }
    }
    public void drop_table () {
        String selectedTable = tableChoiceBox.getValue();

        try {
            zooController.Drop_Table(selectedTable);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
