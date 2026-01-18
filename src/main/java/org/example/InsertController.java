package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsertController {

    @FXML
    private ChoiceBox<String> tableChoice;

    @FXML
    private VBox columnsContainer;

    @FXML
    private Button generateFormsButton;

    @FXML
    private Button insertButton;

    private ZooController zooController;

    public void initialize() {
        zooController = new ZooController();
        loadTableNames();
        generateFormsButton.setOnAction(event -> generateForms());
        insertButton.setOnAction(event -> insertData());
    }

    private void loadTableNames() {
        try {
            ChoiceBox<String> tables = zooController.get_table_names();
            tableChoice.setItems(tables.getItems());
            if (!tables.getItems().isEmpty()) {
                tableChoice.setValue(tables.getItems().get(0));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania tabel: " + e.getMessage());
            showError("Nie można załadować listy tabel: " + e.getMessage());
        }
    }

    private void generateForms() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            showError("Wybierz tabelę");
            return;
        }

        try {
            ChoiceBox<String> choiceBox = zooController.get_column_names(selectedTable);
            List<String> columnNames = new ArrayList<>(choiceBox.getItems());

            columnsContainer.getChildren().clear();
            for (String columnName : columnNames) {
                HBox row = new HBox(10);
                row.setPrefWidth(600);

                Label label = new Label(columnName + ":");
                label.setPrefWidth(200);

                TextField textField = new TextField();
                textField.setPrefWidth(300);
                textField.setUserData(columnName);

                row.getChildren().addAll(label, textField);
                columnsContainer.getChildren().add(row);
            }

        } catch (SQLException e) {
            System.err.println("Błąd podczas generowania formularzy: " + e.getMessage());
            showError("Nie można pobrać struktury tabeli: " + e.getMessage());
        }
    }

    private void insertData() {
        String selectedTable = tableChoice.getValue();
        if (selectedTable == null || selectedTable.isEmpty()) {
            showError("Wybierz tabelę");
            return;
        }

        try {
            List<Object> values = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            for (javafx.scene.Node node : columnsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;

                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            TextField textField = (TextField) child;
                            String columnName = (String) textField.getUserData();
                            String value = textField.getText();
                            columnNames.add(columnName);
                            values.add(value.isEmpty() ? null : value);
                        }
                    }
                }
            }
            zooController.insertIntoTable(selectedTable, columnNames, values);
            for (javafx.scene.Node node : columnsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;
                    for (javafx.scene.Node child : row.getChildren()) {
                        if (child instanceof TextField) {
                            ((TextField) child).clear();
                        }
                    }
                }
            }

            showSuccess("Dane zostały pomyślnie wstawione do tabeli");

        } catch (SQLException e) {
            System.err.println("Błąd podczas wstawiania danych: " + e.getMessage());
            showError("Nie można wstawić danych: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
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