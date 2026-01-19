package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.IOException;
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
            refreshTables();
            if (porownanieChoiceBox != null) {
                porownanieChoiceBox.getItems().addAll("większe od", "mniejsze od", "równe", "różne");
                porownanieChoiceBox.setValue("równe");
            } else {
                System.out.println("Warning: porownanieChoiceBox is null");
            }

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
            ChoiceBox<String> tablesFromDB = zooController.get_table_names();
            tableChoiceBox.getItems().clear();

            if (tablesFromDB != null && tablesFromDB.getItems() != null) {
                tableChoiceBox.getItems().addAll(tablesFromDB.getItems());

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
            showError("Błąd: Pole wyboru warunku jest nieprawidłowe!");
            return;
        }
        boolean czyWarunek = czyWarunekCheckBox.isSelected();

        if (selectedTable == null || selectedTable.isEmpty()) {
            showError("Proszę wybrać tabelę!");
            return;
        }

        try {
            if (czyWarunek) {
                if (kolumnyChoiceBox == null || porownanieChoiceBox == null || warunekWartoscTextField == null) {
                    showError("Błąd: Niektóre kontrolki warunku są nieprawidłowe!");
                    return;
                }

                String warunekKolumna = kolumnyChoiceBox.getValue();
                String operator = porownanieChoiceBox.getValue();
                String warunekWartosc = warunekWartoscTextField.getText();

                if (warunekKolumna == null || warunekKolumna.isEmpty() ||
                        operator == null || operator.isEmpty() ||
                        warunekWartosc == null || warunekWartosc.trim().isEmpty()) {
                    showError("Proszę wypełnić wszystkie pola warunku!");
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
                showSuccess("Usunięto rekordy z tabeli " + selectedTable + " z warunkiem: " + warunek);

            } else {
                zooController.delete_table(selectedTable);
                showSuccess("Usunięto wszystkie rekordy z tabeli " + selectedTable);
            }

        } catch (Exception e) {
            showError("Błąd podczas usuwania: " + e.getMessage());
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