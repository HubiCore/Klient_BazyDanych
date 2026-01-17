package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import java.sql.SQLException;

public class DropController {
    private ZooController zooController;

    @FXML
    private ChoiceBox<String> tableChoiceBox;

    @FXML
    public void initialize() {
        try {
            zooController = new ZooController();

            // Pobierz dynamicznie nazwy tabel z bazy danych
            tableChoiceBox.setItems(zooController.get_table_names().getItems());

            // Ustaw pierwszą tabelę jako domyślną, jeśli istnieje
            if (!tableChoiceBox.getItems().isEmpty()) {
                tableChoiceBox.setValue(tableChoiceBox.getItems().get(0));
            }

            tableChoiceBox.setOnAction(event -> {
                String selectedTable = tableChoiceBox.getValue();
                // Możesz dodać dodatkową logikę po zmianie wyboru
            });

        } catch (Exception e) {
            System.out.println("Błąd inicjalizacji Nie można zainicjalizować kontrolera:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void drop_table() {
        String selectedTable = tableChoiceBox.getValue();

        if (selectedTable == null || selectedTable.trim().isEmpty()) {
            System.out.println("Nie wybrano tabeli do usunięcia");
            return;
        }

        try {
            zooController.Drop_Table(selectedTable);

            // Odśwież listę tabel po usunięciu
            tableChoiceBox.getItems().clear();
            tableChoiceBox.setItems(zooController.get_table_names().getItems());

            // Ustaw nową domyślną tabelę
            if (!tableChoiceBox.getItems().isEmpty()) {
                tableChoiceBox.setValue(tableChoiceBox.getItems().get(0));
            } else {
                tableChoiceBox.setValue(null);
            }

        } catch (Exception e) {
            System.out.println("Błąd podczas usuwania tabeli: " + e.getMessage());
            e.printStackTrace();
        }
    }
}