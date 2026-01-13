package org.example;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import java.sql.ResultSet;

public class TableController {
    @FXML
    private ChoiceBox<String> tableChoiceBox;

    @FXML
    private TableView<ObservableList<Object>> dataTableView;

    private ZooController zooController;

    @FXML
    private void handleRefresh() {
        String selectedTable = tableChoiceBox.getValue();
        if (selectedTable != null) {
            displayTableData(selectedTable);
        }
    }
    @FXML
    public void initialize() {
        try {
            zooController = new ZooController();

            tableChoiceBox.getItems().addAll(
                    "Pracownicy", "Bilet", "Klienci", "Wybiegi", "Klatki", "Karmienia"
            );
            tableChoiceBox.setValue("Pracownicy");

            tableChoiceBox.setOnAction(event -> {
                String selectedTable = tableChoiceBox.getValue();
                displayTableData(selectedTable);
            });

            // Wyświetlenie danych domyślnej tabeli
            displayTableData("Pracownicy");

        } catch (Exception e) {
            showError("Błąd inicjalizacji", "Nie można zainicjalizować kontrolera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayTableData(String tableName) {
        try {
            ResultSet resultSet = null;

            switch (tableName) {
                case "Pracownicy":
                    resultSet = zooController.getPracownicy("*");
                    break;
                case "Bilet":
                    resultSet = zooController.getBilety("*");
                    break;
                case "Klienci":
                    resultSet = zooController.getKlienci("*");
                    break;
                case "Wybiegi":
                    resultSet = zooController.getWybiegi("*");
                    break;
                case "Klatki":
                    resultSet = zooController.getKlatki("*");
                    break;
                case "Karmienia":
                    resultSet = zooController.getKarmienia("*");
                    break;
            }

            if (resultSet != null) {
                TableViewHelper.populateTableView(dataTableView, resultSet);
            }

        } catch (Exception e) {
            showError("Błąd bazy danych", "Nie można załadować danych z tabeli " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}