package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class UpdateController {
    private ZooController zooController;

    @FXML
    private ChoiceBox<String> tableChoiceBox;
    @FXML
    private ChoiceBox<String> Wybierz_kolumne;
    @FXML
    private TextField Warunek;
    @FXML
    private TextField Wartosc;
    @FXML
    private CheckBox czy_warunek;

    @FXML
    public void initialize() {
        try {
            zooController = new ZooController();
            tableChoiceBox.getItems().addAll("Pracownicy", "Bilet", "Klienci", "Wybiegi", "Klatki", "Karmienia");
            tableChoiceBox.setValue("Pracownicy");
        } catch (Exception e) {
            System.out.println("Błąd inicjalizacji Nie można zainicjalizować kontrolera:\n" + e.getMessage());
            e.printStackTrace();
        }
    }
    public void update_wybierz_kolumne(){
        //dokończyć
    }
    public void modify_table() {
        String selectedTable = tableChoiceBox.getValue();
        String selectedColumn = Wybierz_kolumne.getValue();
        String wartoscValue = Wartosc.getText();
        String warunekValue = Warunek.getText();
        boolean czyWarunek = czy_warunek.isSelected();
        if (czyWarunek) {
            try {
                zooController.update_table_where(selectedTable, selectedColumn, wartoscValue, warunekValue);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        else {
            try {
                zooController.update_table(selectedTable, selectedColumn, wartoscValue);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}