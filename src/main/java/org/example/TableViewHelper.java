package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class TableViewHelper {


    public static void populateTableView(TableView<ObservableList<Object>> tableView, ResultSet resultSet) {
        try {
            tableView.getItems().clear();
            tableView.getColumns().clear();

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i;
                TableColumn<ObservableList<Object>, String> column = new TableColumn<>(metaData.getColumnName(i));

                column.setCellValueFactory(cellData -> {
                    ObservableList<Object> row = cellData.getValue();
                    Object value = row.get(columnIndex - 1);
                    return new SimpleStringProperty(value != null ? value.toString() : "");
                });

                tableView.getColumns().add(column);
            }
            ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

            while (resultSet.next()) {
                ObservableList<Object> row = FXCollections.observableArrayList();

                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getObject(i));
                }

                data.add(row);
            }
            tableView.setItems(data);
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void populateTableViewWithModel(TableView<T> tableView, ResultSet resultSet, Class<T> modelClass) {
        try {
            tableView.getItems().clear();
            tableView.getColumns().clear();

            ObservableList<T> data = FXCollections.observableArrayList();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String propertyName = convertToCamelCase(columnName);

                TableColumn<T, Object> column = new TableColumn<>(columnName);
                column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
                tableView.getColumns().add(column);
            }



            tableView.setItems(data);
            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String convertToCamelCase(String columnName) {
        String[] parts = columnName.toLowerCase().split("_");
        StringBuilder camelCase = new StringBuilder(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            camelCase.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1));
        }

        return camelCase.toString();
    }
}