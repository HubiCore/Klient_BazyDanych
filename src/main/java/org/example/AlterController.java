package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class AlterController {

    @FXML
    private ChoiceBox<String> tableChoiceBox;

    @FXML
    private ChoiceBox<String> operationChoiceBox;

    @FXML
    private ChoiceBox<String> dataTypeChoiceBox;

    @FXML
    private ChoiceBox<String> existingColumnChoiceBox;

    @FXML
    private ChoiceBox<String> constraintTypeChoiceBox;

    @FXML
    private ChoiceBox<String> foreignTableChoiceBox;

    @FXML
    private ChoiceBox<String> foreignColumnChoiceBox;

    @FXML
    private ChoiceBox<String> constraintNameChoiceBox;

    @FXML
    private TextField secondTextField;

    @FXML
    private TextField constraintNameTextField;

    @FXML
    private TextField columnSizeTextField;

    @FXML
    private Button confirmButton;

    @FXML
    private Button executeButton;

    @FXML
    private Text instructionText;

    private ZooController zooController;

    @FXML
    private void initialize() {
        zooController = new ZooController();
        loadTables();
        setupEventHandlers();
        updateUIForOperation();
    }
    private void loadTables() {
        try {
            ChoiceBox<String> tables = zooController.get_table_names();
            tableChoiceBox.getItems().clear();
            tableChoiceBox.getItems().addAll(tables.getItems());
            if (!tableChoiceBox.getItems().isEmpty()) {
                tableChoiceBox.setValue(tableChoiceBox.getItems().get(0));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania tabel: " + e.getMessage());
        }
    }
    private void setupEventHandlers() {
        // Aktualizuj UI po wyborze operacji
        operationChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateUIForOperation()
        );
        tableChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadColumnsForTable();
                    }
                }
        );
        confirmButton.setOnAction(event -> handleConfirmButton());
        executeButton.setOnAction(event -> handleExecuteButton());
        foreignTableChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadForeignColumns();
                    }
                }
        );
    }

    private void updateUIForOperation() {
        String operation = operationChoiceBox.getValue();
        if (operation == null) return;

        secondTextField.setVisible(true);
        secondTextField.setPromptText("");
        dataTypeChoiceBox.setVisible(false);
        existingColumnChoiceBox.setVisible(false);
        constraintTypeChoiceBox.setVisible(false);
        foreignTableChoiceBox.setVisible(false);
        foreignColumnChoiceBox.setVisible(false);
        constraintNameChoiceBox.setVisible(false);
        constraintNameTextField.setVisible(false);
        columnSizeTextField.setVisible(false);
        instructionText.setText("");

        switch (operation) {
            case "Dodaj kolumne":
                secondTextField.setPromptText("Nazwa nowej kolumny");
                dataTypeChoiceBox.setVisible(true);
                columnSizeTextField.setVisible(true);
                columnSizeTextField.setPromptText("Rozmiar (opcjonalnie)");
                instructionText.setText("Wybierz tabelę, podaj nazwę kolumny i typ danych");
                break;

            case "Usuń kolumne":
                existingColumnChoiceBox.setVisible(true);
                instructionText.setText("Wybierz tabelę i kolumnę do usunięcia");
                break;

            case "Zmień typ danych kolumny":
                existingColumnChoiceBox.setVisible(true);
                dataTypeChoiceBox.setVisible(true);
                columnSizeTextField.setVisible(true);
                columnSizeTextField.setPromptText("Nowy rozmiar (opcjonalnie)");
                instructionText.setText("Wybierz tabelę, kolumnę i nowy typ danych");
                break;

            case "Zmień nazwe kolumny":
                existingColumnChoiceBox.setVisible(true);
                secondTextField.setVisible(true);
                secondTextField.setPromptText("Nowa nazwa kolumny");
                instructionText.setText("Wybierz tabelę, kolumnę i podaj nową nazwę");
                break;

            case "Dodaj ograniczenie":
                constraintTypeChoiceBox.setVisible(true);
                constraintNameTextField.setVisible(true);
                constraintNameTextField.setPromptText("Nazwa ograniczenia (opcjonalnie)");
                existingColumnChoiceBox.setVisible(true);
                instructionText.setText("Wybierz tabelę i typ ograniczenia");

                constraintTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal != null && newVal.contains("FOREIGN KEY")) {
                                foreignTableChoiceBox.setVisible(true);
                                foreignColumnChoiceBox.setVisible(true);
                                loadForeignTables();
                            } else {
                                foreignTableChoiceBox.setVisible(false);
                                foreignColumnChoiceBox.setVisible(false);
                            }
                        }
                );
                break;

            case "Usuń ograniczenie":
                constraintNameChoiceBox.setVisible(true);
                instructionText.setText("Wybierz tabelę i ograniczenie do usunięcia");
                loadConstraintsForTable();
                break;

            case "Zmień nazwe tabeli":
                secondTextField.setVisible(true);
                secondTextField.setPromptText("Nowa nazwa tabeli");
                instructionText.setText("Wybierz tabelę i podaj nową nazwę");
                break;
        }
    }

    private void loadColumnsForTable() {
        String tableName = tableChoiceBox.getValue();
        if (tableName == null) return;

        try {
            ChoiceBox<String> columnChoiceBox = zooController.get_column_names(tableName);
            existingColumnChoiceBox.getItems().setAll(columnChoiceBox.getItems());
            if (!existingColumnChoiceBox.getItems().isEmpty()) {
                existingColumnChoiceBox.setValue(existingColumnChoiceBox.getItems().get(0));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania kolumn: " + e.getMessage());
        }
    }

    private void loadForeignTables() {
        foreignTableChoiceBox.getItems().setAll(tableChoiceBox.getItems());
    }

    private void loadForeignColumns() {
        String tableName = foreignTableChoiceBox.getValue();
        if (tableName == null) return;

        try {
            ChoiceBox<String> columnChoiceBox = zooController.get_column_names(tableName);
            foreignColumnChoiceBox.getItems().setAll(columnChoiceBox.getItems());
            if (!foreignColumnChoiceBox.getItems().isEmpty()) {
                foreignColumnChoiceBox.setValue(foreignColumnChoiceBox.getItems().get(0));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania kolumn obcych: " + e.getMessage());
        }
    }

    private void loadConstraintsForTable() {
        String tableName = tableChoiceBox.getValue();
        if (tableName == null) return;

        try {
            List<String> constraints = zooController.getConstraintNames(tableName);
            constraintNameChoiceBox.getItems().setAll(constraints);
            if (!constraints.isEmpty()) {
                constraintNameChoiceBox.setValue(constraints.get(0));
            }
        } catch (SQLException e) {
            showError("Błąd ładowania ograniczeń: " + e.getMessage());
        }
    }

    private void handleConfirmButton() {
        String operation = operationChoiceBox.getValue();
        String tableName = tableChoiceBox.getValue();

        if (tableName == null || operation == null) {
            showError("Proszę wybrać tabelę i operację");
            return;
        }

        switch (operation) {
            case "Dodaj kolumne":
            case "Zmień typ danych kolumny":
            case "Zmień nazwe kolumny":
            case "Usuń kolumne":
            case "Dodaj ograniczenie":
                loadColumnsForTable();
                break;
            case "Usuń ograniczenie":
                loadConstraintsForTable();
                break;
        }
    }

    private void handleExecuteButton() {
        String operation = operationChoiceBox.getValue();
        String tableName = tableChoiceBox.getValue();

        if (tableName == null || operation == null) {
            showError("Proszę wybrać tabelę i operację");
            return;
        }

        try {
            switch (operation) {
                case "Dodaj kolumne":
                    addColumn(tableName);
                    break;
                case "Usuń kolumne":
                    dropColumn(tableName);
                    break;
                case "Zmień typ danych kolumny":
                    modifyColumnType(tableName);
                    break;
                case "Zmień nazwe kolumny":
                    renameColumn(tableName);
                    break;
                case "Dodaj ograniczenie":
                    addConstraint(tableName);
                    break;
                case "Usuń ograniczenie":
                    dropConstraint(tableName);
                    break;
                case "Zmień nazwe tabeli":
                    renameTable(tableName);
                    break;
                default:
                    showError("Nieobsługiwana operacja");
            }
            showSuccess("Operacja wykonana pomyślnie!");
            clearFields();

            // Odśwież listę kolumn po wykonaniu operacji
            if (!operation.equals("Zmień nazwe tabeli")) {
                loadColumnsForTable();
            }
        } catch (SQLException e) {
            showError("Błąd wykonania operacji: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Błąd: " + e.getMessage());
        }
    }

    private void addColumn(String tableName) throws SQLException {
        String columnName = secondTextField.getText();
        String dataType = dataTypeChoiceBox.getValue();
        String size = columnSizeTextField.getText();

        if (columnName == null || columnName.isEmpty()) {
            throw new IllegalArgumentException("Proszę podać nazwę kolumny");
        }

        if (dataType == null) {
            throw new IllegalArgumentException("Proszę wybrać typ danych");
        }

        String columnDefinition = dataType;
        if (!size.isEmpty()) {
            try {
                Integer.parseInt(size);
                columnDefinition += "(" + size + ")";
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Rozmiar musi być liczbą całkowitą");
            }
        }

        zooController.addColumn(tableName, columnName, columnDefinition);
    }

    private void dropColumn(String tableName) throws SQLException {
        String columnName = existingColumnChoiceBox.getValue();
        if (columnName == null) {
            throw new IllegalArgumentException("Proszę wybrać kolumnę do usunięcia");
        }
        zooController.dropColumn(tableName, columnName);
    }

    private void modifyColumnType(String tableName) throws SQLException {
        String columnName = existingColumnChoiceBox.getValue();
        String dataType = dataTypeChoiceBox.getValue();
        String size = columnSizeTextField.getText();

        if (columnName == null) {
            throw new IllegalArgumentException("Proszę wybrać kolumnę");
        }

        if (dataType == null) {
            throw new IllegalArgumentException("Proszę wybrać typ danych");
        }

        String columnDefinition = dataType;
        if (!size.isEmpty()) {
            try {
                Integer.parseInt(size);
                columnDefinition += "(" + size + ")";
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Rozmiar musi być liczbą całkowitą");
            }
        }

        zooController.modifyColumnType(tableName, columnName, columnDefinition);
    }

    private void renameColumn(String tableName) throws SQLException {
        String oldColumnName = existingColumnChoiceBox.getValue();
        String newColumnName = secondTextField.getText();

        if (oldColumnName == null) {
            throw new IllegalArgumentException("Proszę wybrać kolumnę");
        }

        if (newColumnName == null || newColumnName.isEmpty()) {
            throw new IllegalArgumentException("Proszę podać nową nazwę kolumny");
        }

        zooController.renameColumn(tableName, oldColumnName, newColumnName);
    }

    private void addConstraint(String tableName) throws SQLException {
        String constraintType = constraintTypeChoiceBox.getValue();
        String constraintName = constraintNameTextField.getText();
        String columnName = existingColumnChoiceBox.getValue();

        if (constraintType == null) {
            throw new IllegalArgumentException("Proszę wybrać typ ograniczenia");
        }

        if (columnName == null) {
            throw new IllegalArgumentException("Proszę wybrać kolumnę");
        }

        String constraintDefinition;
        switch (constraintType.trim()) {
            case "NOT NULL":
                constraintDefinition = "MODIFY " + columnName + " NOT NULL";
                break;
            case "PRIMARY KEY":
                constraintDefinition = "ADD PRIMARY KEY (" + columnName + ")";
                break;
            case "UNIQUE":
                constraintDefinition = "ADD UNIQUE (" + columnName + ")";
                break;
            case "FOREIGN KEY":
                String foreignTable = foreignTableChoiceBox.getValue();
                String foreignColumn = foreignColumnChoiceBox.getValue();
                if (foreignTable == null || foreignColumn == null) {
                    throw new IllegalArgumentException("Proszę wybrać tabelę i kolumnę obcą");
                }
                constraintDefinition = "ADD FOREIGN KEY (" + columnName +
                        ") REFERENCES " + foreignTable + "(" + foreignColumn + ")";
                break;
            default:
                throw new IllegalArgumentException("Nieobsługiwany typ ograniczenia: " + constraintType);
        }

        zooController.addConstraint(tableName, constraintDefinition, constraintName);
    }

    private void dropConstraint(String tableName) throws SQLException {
        String constraintName = constraintNameChoiceBox.getValue();
        if (constraintName == null) {
            throw new IllegalArgumentException("Proszę wybrać ograniczenie do usunięcia");
        }
        zooController.dropConstraint(tableName, constraintName);
    }

    private void renameTable(String tableName) throws SQLException {
        String newTableName = secondTextField.getText();
        if (newTableName == null || newTableName.isEmpty()) {
            throw new IllegalArgumentException("Proszę podać nową nazwę tabeli");
        }
        zooController.renameTable(tableName, newTableName);
        int index = tableChoiceBox.getItems().indexOf(tableName);
        if (index >= 0) {
            tableChoiceBox.getItems().set(index, newTableName);
        }
        tableChoiceBox.setValue(newTableName);
    }

    private void clearFields() {
        secondTextField.clear();
        constraintNameTextField.clear();
        columnSizeTextField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void switch_to_menu(ActionEvent event) throws IOException {
        Stage stage;
        Scene scene;
        Parent root;
        root = FXMLLoader.load(getClass().getResource("/Menu.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}