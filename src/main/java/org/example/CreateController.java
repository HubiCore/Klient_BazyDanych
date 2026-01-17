package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateController {
    @FXML private TextField tableNameField;
    @FXML private TextField columnCountField;
    @FXML private VBox columnsContainer;
    @FXML private Button generateFormsButton;
    @FXML private Button createTableButton;

    private ZooController zooController;
    private List<ColumnForm> columnForms = new ArrayList<>();

    @FXML
    public void initialize() {
        zooController = new ZooController();
        generateFormsButton.setOnAction(e -> generateColumnForms());
        createTableButton.setOnAction(e -> createTable());
    }

    @FXML
    private void generateColumnForms() {
        try {
            int columnCount = Integer.parseInt(columnCountField.getText());
            columnsContainer.getChildren().clear();
            columnForms.clear();

            for (int i = 0; i < columnCount; i++) {
                ColumnForm columnForm = createColumnForm(i + 1);
                columnForms.add(columnForm);
                columnsContainer.getChildren().add(columnForm.getContainer());
            }
        } catch (NumberFormatException e) {
            showAlert("Błąd", "Nieprawidłowa liczba kolumn", "Wprowadź prawidłową liczbę całkowitą.");
        }
    }

    private ColumnForm createColumnForm(int columnNumber) {
        ColumnForm columnForm = new ColumnForm(columnNumber);
        columnForm.getForeignKeyTableChoice().getSelectionModel().selectedItemProperty().addListener((obs, oldTable, newTable) -> {
            if (newTable != null) {
                try {
                    ChoiceBox<String> columnChoiceBox = zooController.get_column_names(newTable);
                    columnForm.getForeignKeyColumnChoice().getItems().clear();
                    columnForm.getForeignKeyColumnChoice().getItems().addAll(columnChoiceBox.getItems());
                    if (!columnChoiceBox.getItems().isEmpty()) {
                        columnForm.getForeignKeyColumnChoice().setValue(columnChoiceBox.getItems().get(0));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Błąd", "Nie można załadować kolumn",
                            "Nie udało się załadować kolumn dla tabeli " + newTable + ": " + e.getMessage());
                }
            }
        });

        columnForm.getConstraintChoice().getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isForeignKey = "FOREIGN KEY".equals(newVal);
            columnForm.setForeignKeyFieldsVisible(isForeignKey);
        });

        return columnForm;
    }

    @FXML
    private void createTable() {
        String tableName = tableNameField.getText().trim();

        if (tableName.isEmpty()) {
            showAlert("Błąd", "Brak nazwy tabeli", "Wprowadź nazwę tabeli.");
            return;
        }

        if (columnForms.isEmpty()) {
            showAlert("Błąd", "Brak kolumn", "Wygeneruj najpierw formularze kolumn.");
            return;
        }

        List<ColumnDefinition> columnDefinitions = new ArrayList<>();

        for (ColumnForm columnForm : columnForms) {
            ColumnDefinition colDef = columnForm.getColumnDefinition();
            if (colDef != null) {
                columnDefinitions.add(colDef);
            }
        }

        if (columnDefinitions.isEmpty()) {
            showAlert("Błąd", "Brak danych kolumn", "Wypełnij wszystkie pola kolumn.");
            return;
        }

        try {
            zooController.createTable(tableName, columnDefinitions);
            showAlert("Sukces", "Tabela utworzona", "Tabela '" + tableName + "' została pomyślnie utworzona.");
            clearForm();
        } catch (SQLException e) {
            showAlert("Błąd SQL", "Nie udało się utworzyć tabeli", e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        tableNameField.clear();
        columnCountField.clear();
        columnsContainer.getChildren().clear();
        columnForms.clear();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private class ColumnForm {
        private final VBox container;
        private final TextField columnNameField;
        private final ChoiceBox<String> dataTypeChoice;
        private final ChoiceBox<String> constraintChoice;
        private final ChoiceBox<String> foreignKeyTableChoice;
        private final ChoiceBox<String> foreignKeyColumnChoice;

        public ColumnForm(int columnNumber) {
            container = new VBox(10);
            List<String> dataTypes = List.of(
                    "VARCHAR2(100)", "CHAR(10)", "NCHAR(10)", "NVARCHAR2(100)", "CLOB", "NCLOB", "LONG",
                    "NUMBER", "NUMBER(10)", "NUMBER(10,2)", "INTEGER", "FLOAT", "BINARY_FLOAT", "BINARY_DOUBLE",
                    "DATE", "TIMESTAMP", "TIMESTAMP WITH TIME ZONE", "TIMESTAMP WITH LOCAL TIME ZONE",
                    "INTERVAL YEAR TO MONTH", "INTERVAL DAY TO SECOND", "RAW(100)", "LONG RAW", "BLOB", "BFILE",
                    "ROWID", "UROWID"
            );

            List<String> constraints = List.of(
                    "NOT NULL", "PRIMARY KEY", "UNIQUE", "FOREIGN KEY"
            );

            List<String> tables = List.of(
                    "Pracownicy", "Bilet", "Klienci", "Wybiegi", "Klatki", "Karmienia"
            );

            Label nameLabel = new Label("Kolumna #" + columnNumber + " - Nazwa:");
            columnNameField = new TextField();
            columnNameField.setPromptText("Nazwa kolumny");

            Label typeLabel = new Label("Typ danych:");
            dataTypeChoice = new ChoiceBox<>(FXCollections.observableArrayList(dataTypes));
            dataTypeChoice.setValue("VARCHAR2(100)");

            Label constraintLabel = new Label("Constraint:");
            constraintChoice = new ChoiceBox<>(FXCollections.observableArrayList(constraints));

            Label fkTableLabel = new Label("Tabela (dla FOREIGN KEY):");
            foreignKeyTableChoice = new ChoiceBox<>(FXCollections.observableArrayList(tables));

            Label fkColumnLabel = new Label("Kolumna (dla FOREIGN KEY):");

            foreignKeyColumnChoice = new ChoiceBox<>();
            fkTableLabel.setVisible(false);
            foreignKeyTableChoice.setVisible(false);
            fkColumnLabel.setVisible(false);
            foreignKeyColumnChoice.setVisible(false);
            container.getChildren().addAll(
                    nameLabel, columnNameField,
                    typeLabel, dataTypeChoice,
                    constraintLabel, constraintChoice,
                    fkTableLabel, foreignKeyTableChoice,
                    fkColumnLabel, foreignKeyColumnChoice
            );
        }

        public VBox getContainer() {
            return container;
        }

        public TextField getColumnNameField() {
            return columnNameField;
        }

        public ChoiceBox<String> getDataTypeChoice() {
            return dataTypeChoice;
        }

        public ChoiceBox<String> getConstraintChoice() {
            return constraintChoice;
        }

        public ChoiceBox<String> getForeignKeyTableChoice() {
            return foreignKeyTableChoice;
        }

        public ChoiceBox<String> getForeignKeyColumnChoice() {
            return foreignKeyColumnChoice;
        }

        public void setForeignKeyFieldsVisible(boolean visible) {
            for (int i = 6; i < container.getChildren().size(); i++) {
                container.getChildren().get(i).setVisible(visible);
            }
        }

        public ColumnDefinition getColumnDefinition() {
            String columnName = columnNameField.getText().trim();
            String dataType = dataTypeChoice.getValue();
            String constraint = constraintChoice.getValue();
            String fkTable = foreignKeyTableChoice.getValue();
            String fkColumn = foreignKeyColumnChoice.getValue();

            if (columnName.isEmpty() || dataType == null) {
                return null;
            }

            return new ColumnDefinition(columnName, dataType, constraint, fkTable, fkColumn);
        }
    }

    public static class ColumnDefinition {
        private final String columnName;
        private final String dataType;
        private final String constraint;
        private final String foreignKeyTable;
        private final String foreignKeyColumn;

        public ColumnDefinition(String columnName, String dataType, String constraint,
                                String foreignKeyTable, String foreignKeyColumn) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.constraint = constraint;
            this.foreignKeyTable = foreignKeyTable;
            this.foreignKeyColumn = foreignKeyColumn;
        }

        public String getColumnName() { return columnName; }
        public String getDataType() { return dataType; }
        public String getConstraint() { return constraint; }
        public String getForeignKeyTable() { return foreignKeyTable; }
        public String getForeignKeyColumn() { return foreignKeyColumn; }
    }
}