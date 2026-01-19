package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ProcedureController {

    @FXML private ChoiceBox<String> zooChoiceBox;
    @FXML private ChoiceBox<Integer> monthChoiceBox;
    @FXML private ChoiceBox<Integer> yearChoiceBox;
    @FXML private TextArea raportTextArea;

    @FXML private TextField zwierzeNazwaField;
    @FXML private TextField zwierzeGatunekField;
    @FXML private TextField zwierzeKlatkaField;
    @FXML private TextField zwierzeOpiekunField;
    @FXML private Label zwierzeResultLabel;

    @FXML private TextField szukajZwierzeField;
    @FXML private TextArea szukajResultTextArea;

    @FXML private TabPane procedureTabPane;

    private ZooController zooController;

    public void initialize() {
        zooController = new ZooController();
        initializeChoiceBoxes();
    }

    private void initializeChoiceBoxes() {
        ObservableList<Integer> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            months.add(i);
        }
        monthChoiceBox.setItems(months);
        monthChoiceBox.setValue(1);
        ObservableList<Integer> years = FXCollections.observableArrayList();
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear - 5; i <= currentYear; i++) {
            years.add(i);
        }
        yearChoiceBox.setItems(years);
        yearChoiceBox.setValue(currentYear);
        try {
            ResultSet rs = zooController.getZooList();
            ObservableList<String> zooList = FXCollections.observableArrayList();
            while (rs.next()) {
                zooList.add(rs.getInt("Zoo_ID") + " - " + rs.getString("Nazwa"));
            }
            zooChoiceBox.setItems(zooList);
            if (!zooList.isEmpty()) {
                zooChoiceBox.setValue(zooList.get(0));
            }
        } catch (SQLException e) {
            showError("Nie udało się załadować listy zoo: " + e.getMessage());
        }
    }

    @FXML
    private void generujRaport() {
        try {
            String selectedZoo = zooChoiceBox.getValue();
            if (selectedZoo == null) {
                showError("Wybierz zoo!");
                return;
            }

            int zooId;
            try {
                zooId = Integer.parseInt(selectedZoo.split(" - ")[0]);
                if (zooId <= 0) {
                    showError("Nieprawidłowe ID zoo!");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Nieprawidłowy format ID zoo!");
                return;
            }

            Integer month = monthChoiceBox.getValue();
            Integer year = yearChoiceBox.getValue();

            if (month == null || year == null) {
                showError("Wybierz miesiąc i rok!");
                return;
            }

            if (month < 1 || month > 12) {
                showError("Miesiąc musi być w zakresie 1-12!");
                return;
            }

            int currentYear = java.time.Year.now().getValue();
            if (year < 1900 || year > currentYear) {
                showError("Rok musi być w rozsądnym zakresie!");
                return;
            }

            Map<String, Object> raport = zooController.generujRaportSprzedazyBiletow(zooId, month, year);

            StringBuilder sb = new StringBuilder();
            sb.append("Zoo: ").append(raport.get("nazwa_zoo")).append("\n");
            sb.append("Okres: ").append(raport.get("okres")).append("\n\n");
            sb.append("Podsumowanie:\n");
            sb.append("  Łączna sprzedaż: ").append(raport.get("laczna_sprzedaz")).append(" PLN\n");
            sb.append("  Liczba biletów: ").append(raport.get("liczba_biletow")).append("\n");
            sb.append("  Średnia cena: ").append(raport.get("srednia_cena")).append(" PLN\n\n");

            sb.append("Szczegóły sprzedaży:\n");
            sb.append("Data       | Cena   | Klient ID\n");
            sb.append("-----------|--------|-----------\n");

            List<Map<String, Object>> szczegoly = (List<Map<String, Object>>) raport.get("szczegoly");
            for (Map<String, Object> detal : szczegoly) {
                sb.append(String.format("%-11s| %-7s| %s\n",
                        detal.get("data_biletu"),
                        detal.get("cena"),
                        detal.get("klient_id")));
            }

            raportTextArea.setText(sb.toString());

        } catch (IllegalArgumentException e) {
            showError("Błąd walidacji: " + e.getMessage());
        } catch (SQLException e) {
            showError("Błąd bazy danych: " + e.getMessage());
        }
    }

    @FXML
    private void dodajZwierze() {
        try {
            String nazwa = zwierzeNazwaField.getText().trim();
            String gatunek = zwierzeGatunekField.getText().trim();
            String klatka = zwierzeKlatkaField.getText().trim();
            String opiekunText = zwierzeOpiekunField.getText().trim();

            // Walidacja pól
            if (nazwa.isEmpty()) {
                showError("Nazwa zwierzęcia jest wymagana!");
                zwierzeNazwaField.requestFocus();
                return;
            }

            if (gatunek.isEmpty()) {
                showError("Gatunek jest wymagany!");
                zwierzeGatunekField.requestFocus();
                return;
            }

            if (klatka.isEmpty()) {
                showError("Nazwa klatki jest wymagana!");
                zwierzeKlatkaField.requestFocus();
                return;
            }

            if (opiekunText.isEmpty()) {
                showError("ID opiekuna jest wymagane!");
                zwierzeOpiekunField.requestFocus();
                return;
            }

            int opiekunId;
            try {
                opiekunId = Integer.parseInt(opiekunText);
                if (opiekunId <= 0) {
                    showError("ID opiekuna musi być liczbą dodatnią!");
                    zwierzeOpiekunField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("ID opiekuna musi być liczbą całkowitą!");
                zwierzeOpiekunField.requestFocus();
                return;
            }

            // Walidacja długości tekstu
            if (nazwa.length() > 50) {
                showError("Nazwa zwierzęcia nie może przekraczać 50 znaków!");
                zwierzeNazwaField.requestFocus();
                return;
            }

            if (gatunek.length() > 50) {
                showError("Gatunek nie może przekraczać 50 znaków!");
                zwierzeGatunekField.requestFocus();
                return;
            }

            if (klatka.length() > 50) {
                showError("Nazwa klatki nie może przekraczać 50 znaków!");
                zwierzeKlatkaField.requestFocus();
                return;
            }

            Map<String, Object> result = zooController.dodajZwierze(nazwa, gatunek, klatka, opiekunId);

            String komunikat = (String) result.get("komunikat");
            Integer noweId = (Integer) result.get("nowe_zwierze_id");

            zwierzeResultLabel.setText(komunikat + (noweId != null ? " (ID: " + noweId + ")" : ""));

            if (komunikat.contains("Dodano")) {
                showSuccess(komunikat + (noweId != null ? " (ID: " + noweId + ")" : ""));
                // Czyszczenie pól po sukcesie
                zwierzeNazwaField.clear();
                zwierzeGatunekField.clear();
                zwierzeKlatkaField.clear();
                zwierzeOpiekunField.clear();
            } else {
                showError(komunikat);
            }

        } catch (IllegalArgumentException e) {
            showError("Błąd walidacji: " + e.getMessage());
        } catch (SQLException e) {
            showError("Błąd bazy danych: " + e.getMessage());
        }
    }

    @FXML
    private void szukajZwierze() {
        try {
            String nazwa = szukajZwierzeField.getText().trim();

            if (nazwa.isEmpty()) {
                showError("Wpisz nazwę zwierzęcia!");
                szukajZwierzeField.requestFocus();
                return;
            }

            if (nazwa.length() > 50) {
                showError("Nazwa zwierzęcia nie może przekraczać 50 znaków!");
                szukajZwierzeField.requestFocus();
                return;
            }

            String wynik = zooController.znajdzZwierzePoNazwie(nazwa);
            szukajResultTextArea.setText(wynik);

        } catch (IllegalArgumentException e) {
            showError("Błąd walidacji: " + e.getMessage());
        } catch (SQLException e) {
            showError("Błąd bazy danych: " + e.getMessage());
        }
    }

    @FXML
    private void wyczyscRaport() {
        raportTextArea.clear();
    }

    @FXML
    private void wyczyscSzukanie() {
        szukajZwierzeField.clear();
        szukajResultTextArea.clear();
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