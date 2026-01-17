package org.example;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

            // Użycie istniejącej metody get_table_names() która zwraca ChoiceBox<String>
            loadTableNamesFromDatabase();

            // Ustawienie obsługi zmiany tabeli
            tableChoiceBox.setOnAction(event -> {
                String selectedTable = tableChoiceBox.getValue();
                if (selectedTable != null) {
                    loadColumnNames(selectedTable);
                    displayTableData(selectedTable);
                }
            });

            // Ustaw początkowe wartości dla ChoiceBoxów
            operatorChoiceBox.getItems().addAll("większe od", "mniejsze od", "równe", "różne");
            operatorChoiceBox.setValue("równe");

            kolejnoscChoiceBox.getItems().addAll("Rosnąco", "Malejąco");
            kolejnoscChoiceBox.setValue("Rosnąco");

            // Załaduj kolumny dla początkowej tabeli
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
            // Użycie istniejącej metody get_table_names() która zwraca ChoiceBox<String>
            ChoiceBox<String> tablesFromDb = zooController.get_table_names();

            // Pobierz listę nazw tabel z zwróconego ChoiceBox
            ObservableList<String> tableNames = tablesFromDb.getItems();

            // Wyczyść i ustaw nazwy tabel w tableChoiceBox
            tableChoiceBox.getItems().clear();
            tableChoiceBox.getItems().addAll(tableNames);

            // Ustaw pierwszą tabelę jako wybraną (jeśli istnieje)
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
            // Użyj istniejącej metody get_column_names która zwraca ChoiceBox
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
                // Użyj warunku i sortowania
                String column = kolumnyChoiceBox.getValue();
                String operator = operatorChoiceBox.getValue();
                String value = wartoscTextField.getText();
                String order = kolejnoscChoiceBox.getValue();

                // Zbuduj warunek WHERE
                String whereClause = buildWhereClause(column, operator, value);
                // Zbuduj klauzulę ORDER BY
                String orderClause = buildOrderClause(column, order);

                resultSet = zooController.Select_Table_where_order("*", tableName, whereClause, orderClause);

            } else if (useCondition) {
                // Użyj tylko warunku
                String column = kolumnyChoiceBox.getValue();
                String operator = operatorChoiceBox.getValue();
                String value = wartoscTextField.getText();

                String whereClause = buildWhereClause(column, operator, value);
                resultSet = zooController.Select_Table_where("*", tableName, whereClause);

            } else if (useSorting) {
                // Użyj tylko sortowania
                String column = kolumnyChoiceBox.getValue();
                String order = kolejnoscChoiceBox.getValue();
                String orderClause = buildOrderClause(column, order);
                resultSet = zooController.Select_Table_order("*", tableName, orderClause);

            } else {
                // Użyj podstawowego zapytania bez warunków
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
            return "1=1"; // Zawsze prawdziwy warunek jeśli brak wartości
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

        // Jeśli wartość jest liczbą, nie dodawaj apostrofów
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
            return column + " ASC"; // Domyślnie rosnąco
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
}