package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class DeleteController {
    private ZooController zooController;

    @FXML
    private ChoiceBox<String> tableChoiceBox;

    @FXML
    private ChoiceBox<String> kolumnyChoiceBox;

    @FXML
    private ChoiceBox<String> porownanieChoiceBox;

    @FXML
    private TextField warunekWartoscTextField;

    @FXML
    private CheckBox czyWarunekCheckBox;

    @FXML
    private Button odswiezKolumnyButton;

    @FXML
    private Button deleteButton;

    @FXML
    public void initialize() {
        try {
            zooController = new ZooController();

            // Pobieranie nazw tabel z bazy danych
            refreshTables();

            // Sprawdź czy porownanieChoiceBox nie jest null przed użyciem
            if (porownanieChoiceBox != null) {
                // Inicjalizacja operatorów porównania
                porownanieChoiceBox.getItems().addAll("większe od", "mniejsze od", "równe", "różne");
                porownanieChoiceBox.setValue("równe");
            } else {
                System.out.println("Warning: porownanieChoiceBox is null");
            }

            // Odśwież kolumny przy starcie (jeśli tabela została wybrana)
            if (tableChoiceBox.getValue() != null && !tableChoiceBox.getValue().isEmpty()) {
                odswiezKolumny();
            }

        } catch (Exception e) {
            System.out.println("Błąd inicjalizacji: Nie można zainicjalizować kontrolera:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshTables() {
        try {
            // Pobieranie nazw tabel z bazy danych
            ChoiceBox<String> tablesFromDB = zooController.get_table_names();
            tableChoiceBox.getItems().clear();

            // Kopiowanie nazw tabel z zwróconego ChoiceBox
            if (tablesFromDB != null && tablesFromDB.getItems() != null) {
                tableChoiceBox.getItems().addAll(tablesFromDB.getItems());

                // Ustawienie domyślnej wartości, jeśli dostępne są tabele
                if (!tableChoiceBox.getItems().isEmpty()) {
                    tableChoiceBox.setValue(tableChoiceBox.getItems().get(0));
                } else {
                    System.out.println("Warning: No tables found in the database.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania nazw tabel: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Nieoczekiwany błąd podczas odświeżania tabel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void odswiezKolumny() {
        try {
            String selectedTable = tableChoiceBox.getValue();
            if (selectedTable != null && !selectedTable.isEmpty()) {
                ChoiceBox<String> columnChoiceBox = zooController.get_column_names(selectedTable);
                kolumnyChoiceBox.getItems().clear();

                if (columnChoiceBox != null && columnChoiceBox.getItems() != null) {
                    kolumnyChoiceBox.getItems().addAll(columnChoiceBox.getItems());

                    if (!columnChoiceBox.getItems().isEmpty()) {
                        kolumnyChoiceBox.setValue(columnChoiceBox.getItems().get(0));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania nazw kolumn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void deleteTable() {
        String selectedTable = tableChoiceBox.getValue();
        if (czyWarunekCheckBox == null) {
            System.out.println("Error: czyWarunekCheckBox is null!");
            return;
        }
        boolean czyWarunek = czyWarunekCheckBox.isSelected();

        if (selectedTable == null || selectedTable.isEmpty()) {
            System.out.println("Proszę wybrać tabelę!");
            return;
        }

        try {
            if (czyWarunek) {
                if (kolumnyChoiceBox == null || porownanieChoiceBox == null || warunekWartoscTextField == null) {
                    System.out.println("Error: Some condition controls are null!");
                    return;
                }

                String warunekKolumna = kolumnyChoiceBox.getValue();
                String operator = porownanieChoiceBox.getValue();
                String warunekWartosc = warunekWartoscTextField.getText();

                if (warunekKolumna == null || warunekKolumna.isEmpty() ||
                        operator == null || operator.isEmpty() ||
                        warunekWartosc == null || warunekWartosc.trim().isEmpty()) {
                    System.out.println("Proszę wypełnić wszystkie pola warunku!");
                    return;
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
                        sqlOperator = "<>";
                        break;
                    default:
                        sqlOperator = "=";
                }

                String warunek = warunekKolumna + " " + sqlOperator + " ";
                if (isNumeric(warunekWartosc)) {
                    warunek += warunekWartosc;
                } else {
                    warunek += "'" + warunekWartosc + "'";
                }

                zooController.delete_table_where(selectedTable, warunek);
                System.out.println("Usunięto rekordy z tabeli " + selectedTable + " z warunkiem: " + warunek);

            } else {
                zooController.delete_table(selectedTable);
                System.out.println("Usunięto wszystkie rekordy z tabeli " + selectedTable);
            }

        } catch (Exception e) {
            System.out.println("Błąd podczas usuwania: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}