package org.example;

import java.lang.reflect.Method;
import java.sql.ResultSet;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class ZooService {
    private ZooController zooController;
    private TableView<ObservableList<Object>> tableView;

    public ZooService(ZooController controller, TableView<ObservableList<Object>> tableView) {
        this.zooController = controller;
        this.tableView = tableView;
    }

    /**
     * Generic method to execute any SELECT query and display in TableView
     */
    public void displayTableData(String methodName, Object... parameters) {
        try {
            ResultSet resultSet = null;

            // Use reflection to call the appropriate method
            Class<?>[] paramTypes = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                paramTypes[i] = parameters[i].getClass();
            }

            Method method = ZooController.class.getMethod(methodName, paramTypes);
            resultSet = (ResultSet) method.invoke(zooController, parameters);

            // Populate TableView
            TableViewHelper.populateTableView(tableView, resultSet);

        } catch (Exception e) {
            e.printStackTrace();
            // Show error message to user
        }
    }

    // Convenience methods for specific tables
    public void displayPracownicy() {
        try {
            ResultSet rs = zooController.getPracownicy("*");
            TableViewHelper.populateTableView(tableView, rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayPracownicyWhere(String condition) {
        try {
            ResultSet rs = zooController.getPracownicyWhere("*", condition);
            TableViewHelper.populateTableView(tableView, rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayKlienci() {
        try {
            ResultSet rs = zooController.getKlienci("*");
            TableViewHelper.populateTableView(tableView, rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add similar methods for other tables...
}