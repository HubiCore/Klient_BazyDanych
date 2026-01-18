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
        // Inicjalizacja miesięcy
        ObservableList<Integer> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            months.add(i);
        }
        monthChoiceBox.setItems(months);
        monthChoiceBox.setValue(1);

        // Inicjalizacja lat
        ObservableList<Integer> years = FXCollections.observableArrayList();
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear - 5; i <= currentYear; i++) {
            years.add(i);
        }
        yearChoiceBox.setItems(years);
        yearChoiceBox.setValue(currentYear);

        // Inicjalizacja zoo
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
            showAlert("Błąd", "Nie udało się załadować listy zoo: " + e.getMessage());
        }
    }

    @FXML
    private void generujRaport() {
        try {
            String selectedZoo = zooChoiceBox.getValue();
            if (selectedZoo == null) {
                showAlert("Błąd", "Wybierz zoo!");
                return;
            }

            int zooId = Integer.parseInt(selectedZoo.split(" - ")[0]);
            int month = monthChoiceBox.getValue();
            int year = yearChoiceBox.getValue();

            Map<String, Object> raport = zooController.generujRaportSprzedazyBiletow(zooId, month, year);

            // Formatowanie raportu
            StringBuilder sb = new StringBuilder();
            sb.append("RAPORT SPRZEDAŻY BILETÓW\n");
            sb.append("=======================\n\n");
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

        } catch (NumberFormatException e) {
            showAlert("Błąd", "Nieprawidłowy format ID zoo!");
        } catch (SQLException e) {
            showAlert("Błąd bazy danych", e.getMessage());
        }
    }

    @FXML
    private void dodajZwierze() {
        try {
            String nazwa = zwierzeNazwaField.getText();
            String gatunek = zwierzeGatunekField.getText();
            String klatka = zwierzeKlatkaField.getText();
            int opiekunId;

            try {
                opiekunId = Integer.parseInt(zwierzeOpiekunField.getText());
            } catch (NumberFormatException e) {
                showAlert("Błąd", "ID opiekuna musi być liczbą!");
                return;
            }

            if (nazwa.isEmpty() || gatunek.isEmpty() || klatka.isEmpty()) {
                showAlert("Błąd", "Wypełnij wszystkie pola!");
                return;
            }

            Map<String, Object> result = zooController.dodajZwierze(nazwa, gatunek, klatka, opiekunId);

            String komunikat = (String) result.get("komunikat");
            Integer noweId = (Integer) result.get("nowe_zwierze_id");

            zwierzeResultLabel.setText(komunikat + (noweId != null ? " (ID: " + noweId + ")" : ""));

            // Czyść pola po udanym dodaniu
            if (komunikat.contains("Dodano")) {
                zwierzeNazwaField.clear();
                zwierzeGatunekField.clear();
                zwierzeKlatkaField.clear();
                zwierzeOpiekunField.clear();
            }

        } catch (SQLException e) {
            showAlert("Błąd bazy danych", e.getMessage());
        }
    }

    @FXML
    private void szukajZwierze() {
        try {
            String nazwa = szukajZwierzeField.getText();

            if (nazwa.isEmpty()) {
                showAlert("Błąd", "Wpisz nazwę zwierzęcia!");
                return;
            }

            String wynik = zooController.znajdzZwierzePoNazwie(nazwa);
            szukajResultTextArea.setText(wynik);

        } catch (SQLException e) {
            showAlert("Błąd bazy danych", e.getMessage());
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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