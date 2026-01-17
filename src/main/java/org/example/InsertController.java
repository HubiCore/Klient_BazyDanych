package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsertController {

    @FXML
    private ChoiceBox<String> tableChoice;

    @FXML
    private VBox columnsContainer;

    @FXML
    private Button generateFormsButton;

    @FXML
    private Button insertButton;

    private ZooController zooController;

    public void initialize() {
        zooController = new ZooController();

        // Załaduj nazwy tabel do ChoiceBox
        loadTableNames();

        generateFormsButton.setOnAction(event -> generateForms());
        insertButton.setOnAction(event -> insertData());
    }

    private void loadTableNames() {
        try {
            ChoiceBox<String> tables = zooController.get_table_names();
            tableChoice.setItems(tables.getItems());
            if (!tables.getItems().isEmpty()) {
                tableChoice.setValue(tables.getItems().get(0));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania tabel: " + e.getMessage());
            showAlert("Błąd bazy danych", "Nie można załadować listy tabel: " + e.getMessage());
        }
    }

    private void generateForms() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            showAlert("Błąd", "Wybierz tabelę");
            return;
        }

        try {
            // Użyj istniejącej metody get_column_names z ZooController
            ChoiceBox<String> choiceBox = zooController.get_column_names(selectedTable);
            List<String> columnNames = new ArrayList<>(choiceBox.getItems());

            // Wyczyść kontener
            columnsContainer.getChildren().clear();

            // Dla każdej kolumny utwórz formularz
            for (String columnName : columnNames) {
                HBox row = new HBox(10);
                row.setPrefWidth(600);

                Label label = new Label(columnName + ":");
                label.setPrefWidth(200);

                TextField textField = new TextField();
                textField.setPrefWidth(300);
                textField.setUserData(columnName); // Przechowuj nazwę kolumny

                row.getChildren().addAll(label, textField);
                columnsContainer.getChildren().add(row);
            }

        } catch (SQLException e) {
            System.err.println("Błąd podczas generowania formularzy: " + e.getMessage());
            showAlert("Błąd bazy danych", "Nie można pobrać struktury tabeli: " + e.getMessage());
        }
    }

    private void insertData() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            showAlert("Błąd", "Wybierz tabelę");
            return;
        }

        try {
            // Pobierz wartości z formularzy
            List<Object> values = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            for (javafx.scene.Node node : columnsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;

                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            TextField textField = (TextField) child;
                            String columnName = (String) textField.getUserData();
                            String value = textField.getText();

                            columnNames.add(columnName);
                            // Przetwarzaj puste wartości jako null
                            values.add(value.isEmpty() ? null : value);
                        }
                    }
                }
            }

            // Wykonaj INSERT używając ZooController
            zooController.insertIntoTable(selectedTable, columnNames, values);

            // Wyczyść formularze
            for (javafx.scene.Node node : columnsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;
                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            ((TextField) child).clear();
                        }
                    }
                }
            }

            showAlert("Sukces", "Dane zostały pomyślnie wstawione do tabeli");

        } catch (SQLException e) {
            System.err.println("Błąd podczas wstawiania danych: " + e.getMessage());
            showAlert("Błąd bazy danych", "Nie można wstawić danych: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert("Błąd danych", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}