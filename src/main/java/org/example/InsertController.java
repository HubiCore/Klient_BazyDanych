package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
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
    private List<ColumnInfo> columnInfos;

    public void initialize() {
        zooController = new ZooController();
        columnInfos = new ArrayList<>();
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
            showError("Nie można załadować listy tabel: " + e.getMessage());
        }
    }

    private void generateForms() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            showError("Wybierz tabelę");
            return;
        }

        try {
            columnInfos.clear();
            columnsContainer.getChildren().clear();

            List<ColumnInfo> columnDetails = zooController.getColumnDetails(selectedTable);
            columnInfos.addAll(columnDetails);

            for (ColumnInfo columnInfo : columnInfos) {
                HBox row = new HBox(10);
                row.setPrefWidth(600);

                Label label = new Label(columnInfo.getName() +
                        " (" + columnInfo.getType() + ")" +
                        (columnInfo.isNullable() ? "" : " *") + ":");
                label.setPrefWidth(300);

                TextField textField = new TextField();
                textField.setPrefWidth(300);
                textField.setUserData(columnInfo); // Przechowujemy ColumnInfo

                if (!columnInfo.isNullable()) {
                    textField.setStyle("-fx-border-color: #ffcccc; -fx-border-width: 1px;");
                    Tooltip tooltip = new Tooltip("Pole wymagane (NOT NULL)");
                    Tooltip.install(textField, tooltip);
                }

                row.getChildren().addAll(label, textField);
                columnsContainer.getChildren().add(row);
            }

        } catch (SQLException e) {
            System.err.println("Błąd podczas generowania formularzy: " + e.getMessage());
            showError("Nie można pobrać struktury tabeli: " + e.getMessage());
        }
    }

    private void insertData() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            showError("Wybierz tabelę");
            return;
        }

        try {
            List<Object> values = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            for (javafx.scene.Node node : columnsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;

                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            TextField textField = (TextField) child;
                            ColumnInfo columnInfo = (ColumnInfo) textField.getUserData();
                            String value = textField.getText().trim();

                            String validationError = validateValue(value, columnInfo);
                            if (validationError != null) {
                                showError("Błąd walidacji dla kolumny " + columnInfo.getName() +
                                        ":\n" + validationError);
                                return;
                            }

                            columnNames.add(columnInfo.getName());
                            values.add(value.isEmpty() ? null : value);
                        }
                    }
                }
            }
            zooController.insertIntoTable(selectedTable, columnNames, values);
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

            showSuccess("Dane zostały pomyślnie wstawione do tabeli");

        } catch (SQLException e) {
            System.err.println("Błąd podczas wstawiania danych: " + e.getMessage());
            showError("Nie można wstawić danych: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private String validateValue(String value, ColumnInfo columnInfo) {
        String columnType = columnInfo.getType().toUpperCase();

        if ((value == null || value.isEmpty()) && !columnInfo.isNullable()) {
            return "Pole nie może być puste (NOT NULL)";
        }

        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            if (columnType.contains("NUMBER") || columnType.contains("INT") ||
                    columnType.contains("FLOAT") || columnType.contains("DECIMAL")) {

                if (!isValidNumber(value, columnType)) {
                    return "Nieprawidłowy format liczby. Oczekiwano typu: " + columnType;
                }

                if (columnType.contains("INT") || columnType.startsWith("NUMBER") && !columnType.contains(",")) {
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return "Oczekiwano liczby całkowitej";
                    }
                }

            } else if (columnType.contains("DATE") || columnType.contains("TIMESTAMP")) {
                if (!isValidDate(value)) {
                    return "Nieprawidłowy format daty. Użyj formatu YYYY-MM-DD";
                }
            } else if (columnType.contains("CHAR") || columnType.contains("VARCHAR")) {
                int maxLength = extractMaxLength(columnType);
                if (maxLength > 0 && value.length() > maxLength) {
                    return "Przekroczono maksymalną długość (" + maxLength + " znaków)";
                }
            }
        } catch (Exception e) {
            return "Nieprawidłowy format danych dla typu: " + columnType;
        }

        return null;
    }

    private boolean isValidNumber(String value, String columnType) {
        try {
            value = value.trim();

            if (columnType.contains(",") || columnType.contains("FLOAT") || columnType.contains("DECIMAL")) {
                Double.parseDouble(value);
            } else {
                Long.parseLong(value);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDate(String value) {
        try {
            // Prosta walidacja formatu daty YYYY-MM-DD
            if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // Można dodać bardziej szczegółową walidację daty
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private int extractMaxLength(String columnType) {
        try {
            if (columnType.contains("(") && columnType.contains(")")) {
                int start = columnType.indexOf("(") + 1;
                int end = columnType.indexOf(")");
                String lengthStr = columnType.substring(start, end);
                return Integer.parseInt(lengthStr);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void switch_to_menu(ActionEvent event) throws IOException {
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
}