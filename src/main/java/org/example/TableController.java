package org.example;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableController {
    @FXML
    private ChoiceBox<String> tableChoiceBox;
    @FXML
    private CheckBox czySortowanieCheckBox;
    @FXML
    private ChoiceBox<String> kolejnoscChoiceBox;
    @FXML
    private CheckBox czyWarunekCheckBox;
    @FXML
    private ChoiceBox<String> kolumnyChoiceBox;
    @FXML
    private ChoiceBox<String> operatorChoiceBox;
    @FXML
    private TextField wartoscTextField;
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
            loadTableNamesFromDatabase();
            tableChoiceBox.setOnAction(event -> {
                String selectedTable = tableChoiceBox.getValue();
                if (selectedTable != null) {
                    loadColumnNames(selectedTable);
                    displayTableData(selectedTable);
                }
            });
            operatorChoiceBox.getItems().addAll("większe od", "mniejsze od", "równe", "różne");
            operatorChoiceBox.setValue("równe");

            kolejnoscChoiceBox.getItems().addAll("Rosnąco", "Malejąco");
            kolejnoscChoiceBox.setValue("Rosnąco");
            if (!tableChoiceBox.getItems().isEmpty()) {
                String firstTable = tableChoiceBox.getValue();
                if (firstTable != null) {
                    loadColumnNames(firstTable);
                    displayTableData(firstTable);
                }
            }

        } catch (Exception e) {
            System.out.println("Błąd inicjalizacji Nie można zainicjalizować kontrolera:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTableNamesFromDatabase() {
        try {
            ChoiceBox<String> tablesFromDb = zooController.get_table_names();

            ObservableList<String> tableNames = tablesFromDb.getItems();

            tableChoiceBox.getItems().clear();
            tableChoiceBox.getItems().addAll(tableNames);

            if (!tableNames.isEmpty() && tablesFromDb.getValue() != null) {
                tableChoiceBox.setValue(tablesFromDb.getValue());
            } else if (!tableNames.isEmpty()) {
                tableChoiceBox.setValue(tableNames.get(0));
            } else {
                System.out.println("Brak tabel w bazie danych.");
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania nazw tabel z bazy danych:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadColumnNames(String tableName) {
        try {
            ChoiceBox<String> columnChoiceBox = zooController.get_column_names(tableName);
            kolumnyChoiceBox.getItems().clear();
            kolumnyChoiceBox.getItems().addAll(columnChoiceBox.getItems());
            if (!kolumnyChoiceBox.getItems().isEmpty()) {
                kolumnyChoiceBox.setValue(kolumnyChoiceBox.getItems().get(0));
            }
        } catch (Exception e) {
            System.out.println("Błąd podczas ładowania nazw kolumn:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayTableData(String tableName) {
        try {
            ResultSet resultSet;
            boolean useCondition = czyWarunekCheckBox.isSelected();
            boolean useSorting = czySortowanieCheckBox.isSelected();

            if (useCondition && useSorting) {
                String column = kolumnyChoiceBox.getValue();
                String operator = operatorChoiceBox.getValue();
                String value = wartoscTextField.getText();
                String order = kolejnoscChoiceBox.getValue();

                String whereClause = buildWhereClause(column, operator, value);
                String orderClause = buildOrderClause(column, order);

                resultSet = zooController.Select_Table_where_order("*", tableName, whereClause, orderClause);

            } else if (useCondition) {
                String column = kolumnyChoiceBox.getValue();
                String operator = operatorChoiceBox.getValue();
                String value = wartoscTextField.getText();

                String whereClause = buildWhereClause(column, operator, value);
                resultSet = zooController.Select_Table_where("*", tableName, whereClause);

            } else if (useSorting) {
                String column = kolumnyChoiceBox.getValue();
                String order = kolejnoscChoiceBox.getValue();
                String orderClause = buildOrderClause(column, order);
                resultSet = zooController.Select_Table_order("*", tableName, orderClause);

            } else {
                resultSet = zooController.Select_Table("*", tableName);
            }

            if (resultSet != null) {
                TableViewHelper.populateTableView(dataTableView, resultSet);
            }

        } catch (Exception e) {
            System.out.println("Błąd bazy danych Nie można załadować danych z tabeli " + tableName + ":\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildWhereClause(String column, String operator, String value) {
        if (column == null || operator == null || value == null || value.trim().isEmpty()) {
            return "1=1";
        }

        String sqlOperator = "";

        switch (operator) {
            case "większe od":
                sqlOperator = ">";
                break;
            case "mniejsze od":
                sqlOperator = "<";
                break;
            case "równe":
                sqlOperator = "=";
                break;
            case "różne":
                sqlOperator = "!=";
                break;
            default:
                sqlOperator = "=";
        }

        if (isNumeric(value)) {
            return column + " " + sqlOperator + " " + value;
        } else {
            return column + " " + sqlOperator + " '" + value + "'";
        }
    }

    private String buildOrderClause(String column, String order) {
        if (column == null) {
            return "";
        }

        if ("Malejąco".equals(order)) {
            return column + " DESC";
        } else {
            return column + " ASC";
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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