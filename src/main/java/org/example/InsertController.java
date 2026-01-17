// InsertController.java
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

    @FXML
    private VBox templateContainer;

    @FXML
    private Label templateColumnName;

    @FXML
    private TextField templateValueField;

    private ZooController zooController;
    private List<String> currentColumnNames;

    public void initialize() {
        zooController = new ZooController();

        generateFormsButton.setOnAction(event -> generateForms());
        insertButton.setOnAction(event -> insertData());
    }

    private void generateForms() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            System.out.println("Wybierz tabele");
            return;
        }

        try {
            // Pobierz nazwy kolumn dla wybranej tabeli
            currentColumnNames = getColumnNames(selectedTable);

            // Wyczyść kontener
            columnsContainer.getChildren().clear();

            // Dla każdej kolumny utwórz formularz
            for (String columnName : currentColumnNames) {
                // Utwórz kopię szablonu
                VBox columnTemplate = new VBox();
                columnTemplate.setSpacing(10);

                HBox row = new HBox(10);

                Label label = new Label(columnName + ":");
                label.setPrefWidth(150);

                TextField textField = new TextField();
                textField.setPrefWidth(200);
                textField.setUserData(columnName); // Przechowuj nazwę kolumny

                row.getChildren().addAll(label, textField);
                columnTemplate.getChildren().add(row);

                columnsContainer.getChildren().add(columnTemplate);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private List<String> getColumnNames(String tableName) throws SQLException {
        List<String> columnNames = new ArrayList<>();

        // Pobierz metadane tabeli
        String query = "SELECT * FROM " + tableName + " WHERE ROWNUM = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }
        }

        return columnNames;
    }

    private void insertData() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            System.out.println("Wybierz tabele");
            return;
        }

        try {
            // Pobierz wartości z formularzy
            List<Object> values = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            for (javafx.scene.Node node : columnsContainer.getChildren()) {
                if (node instanceof VBox) {
                    VBox columnBox = (VBox) node;
                    HBox row = (HBox) columnBox.getChildren().get(0);

                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            TextField textField = (TextField) child;
                            String columnName = (String) textField.getUserData();
                            String value = textField.getText();

                            columnNames.add(columnName);
                            values.add(value.isEmpty() ? null : value);
                        }
                    }
                }
            }

            // Wykonaj INSERT
            insertIntoTable(selectedTable, columnNames, values);

            // Wyczyść formularze
            for (javafx.scene.Node node : columnsContainer.getChildren()) {
                if (node instanceof VBox) {
                    VBox columnBox = (VBox) node;
                    HBox row = (HBox) columnBox.getChildren().get(0);

                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            TextField textField = (TextField) child;
                            textField.clear();
                        }
                    }
                }
            }

            System.out.println("Dane zostały pomyślnie wstawione do tabeli");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void insertIntoTable(String tableName, List<String> columnNames, List<Object> values) throws SQLException {
        if (columnNames.size() != values.size()) {
            throw new IllegalArgumentException("Liczba kolumn i wartości musi być taka sama");
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("INSERT INTO ").append(tableName).append(" (");

        // Dodaj nazwy kolumn
        for (int i = 0; i < columnNames.size(); i++) {
            queryBuilder.append(columnNames.get(i));
            if (i < columnNames.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(") VALUES (");

        // Dodaj placeholdery
        for (int i = 0; i < values.size(); i++) {
            queryBuilder.append("?");
            if (i < values.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(")");

        String query = queryBuilder.toString();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Ustaw wartości
            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                if (value == null) {
                    pstmt.setNull(i + 1, Types.VARCHAR);
                } else {
                    pstmt.setString(i + 1, value.toString());
                }
            }

            pstmt.executeUpdate();
        }
    }

}