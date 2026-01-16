package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

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
            tableChoiceBox.getItems().addAll("Pracownicy", "Bilet", "Klienci", "Wybiegi", "Klatki", "Karmienia");
            tableChoiceBox.setValue("Pracownicy");
            porownanieChoiceBox.getItems().addAll("większe od", "mniejsze od", "równe", "różne");
            porownanieChoiceBox.setValue("równe");


        } catch (Exception e) {
            System.out.println("Błąd inicjalizacji: Nie można zainicjalizować kontrolera:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void delete_wybierz_kolumnyS() {
        try {
            String selectedTable = tableChoiceBox.getValue();
            if (selectedTable != null && !selectedTable.isEmpty()) {
                ChoiceBox<String> columnChoiceBox = zooController.get_column_names(selectedTable);
                kolumnyChoiceBox.getItems().clear();
                kolumnyChoiceBox.getItems().addAll(columnChoiceBox.getItems());

                if (!columnChoiceBox.getItems().isEmpty()) {
                    kolumnyChoiceBox.setValue(columnChoiceBox.getItems().get(0));
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
        boolean czyWarunek = czyWarunekCheckBox.isSelected();

        if (selectedTable == null || selectedTable.isEmpty()) {
            System.out.println("Proszę wybrać tabelę!");
            return;
        }

        try {
            if (czyWarunek) {
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