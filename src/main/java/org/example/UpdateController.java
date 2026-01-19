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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UpdateController {
    private ZooController zooController;

    @FXML
    private ChoiceBox<String> tableChoiceBox;
    @FXML
    private ChoiceBox<String> Wybierz_kolumne;
    @FXML
    private TextField Wartosc;
    @FXML
    private CheckBox czy_warunek;
    @FXML
    private Button odswiezButton;
    @FXML
    private ChoiceBox<String> kolumnyChoiceBox;
    @FXML
    private ChoiceBox<String> porownanieChoiceBox;
    @FXML
    private TextField warunekWartoscTextField;

    @FXML
    public void initialize() {
        try {
            zooController = new ZooController();
            refreshTableList();
        } catch (Exception e) {
            System.out.println("Błąd inicjalizacji Nie można zainicjalizować kontrolera:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshTableList() {
        try {
            ChoiceBox<String> tables = zooController.get_table_names();
            tableChoiceBox.setItems(tables.getItems());

            if (!tables.getItems().isEmpty()) {
                tableChoiceBox.setValue(tables.getItems().get(0));
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania listy tabel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update_wybierz_kolumne() {
        try {
            String selectedTable = tableChoiceBox.getValue();
            if (selectedTable != null && !selectedTable.isEmpty()) {
                ChoiceBox<String> columnChoiceBox = zooController.get_column_names(selectedTable);
                Wybierz_kolumne.getItems().clear();
                Wybierz_kolumne.getItems().addAll(columnChoiceBox.getItems());

                if (kolumnyChoiceBox != null) {
                    kolumnyChoiceBox.getItems().clear();
                    kolumnyChoiceBox.getItems().addAll(columnChoiceBox.getItems());
                }

                if (!columnChoiceBox.getItems().isEmpty()) {
                    Wybierz_kolumne.setValue(columnChoiceBox.getItems().get(0));
                    if (kolumnyChoiceBox != null) {
                        kolumnyChoiceBox.setValue(columnChoiceBox.getItems().get(0));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas pobierania nazw kolumn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void modify_table() {
        String selectedTable = tableChoiceBox.getValue();
        String selectedColumn = Wybierz_kolumne.getValue();
        String wartoscValue = Wartosc.getText();
        boolean czyWarunek = czy_warunek.isSelected();

        if (czyWarunek) {
            try {
                String warunekKolumna = kolumnyChoiceBox.getValue();
                String operator = porownanieChoiceBox.getValue();
                String warunekWartosc = warunekWartoscTextField.getText();
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

                zooController.update_table_where(selectedTable, selectedColumn, wartoscValue, warunek);
                showSuccess("Pomyślnie zaktualizowano dane z warunkiem!");
            } catch (Exception e) {
                String errorMessage = "Błąd przy aktualizacji z warunkiem: " + e.getMessage();
                System.out.println(errorMessage);
                e.printStackTrace();
                showError(errorMessage);
            }
        } else {
            try {
                zooController.update_table(selectedTable, selectedColumn, wartoscValue);
                showSuccess("Pomyślnie zaktualizowano dane!");
            } catch (Exception e) {
                String errorMessage = "Błąd przy aktualizacji: " + e.getMessage();
                System.out.println(errorMessage);
                e.printStackTrace();
                showError(errorMessage);
            }
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