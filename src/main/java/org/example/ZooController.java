package org.example;

import javafx.scene.control.ChoiceBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ZooController {
    private Connection connection;

    public ZooController() {
        this.connection = DatabaseConnection.getConnection();
    }

    private Connection ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DatabaseConnection.getConnection();
        }
        return connection;
    }
    public void addColumn(String tableName, String columnName, String dataType) throws SQLException {
        String query = "ALTER TABLE " + tableName + " ADD " + columnName + " " + dataType;
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }

    public void dropColumn(String tableName, String columnName) throws SQLException {
        String query = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }

    public void modifyColumnType(String tableName, String columnName, String newDataType) throws SQLException {
        String query = "ALTER TABLE " + tableName + " MODIFY " + columnName + " " + newDataType;
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }

    public void renameColumn(String tableName, String oldColumnName, String newColumnName) throws SQLException {
        String query = "ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + " TO " + newColumnName;
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }

    public void addConstraint(String tableName, String constraintDefinition, String constraintName) throws SQLException {
        String query;
        if (constraintName != null && !constraintName.trim().isEmpty()) {
            query = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName.trim() + " " + constraintDefinition;
        } else {
            query = "ALTER TABLE " + tableName + " " + constraintDefinition;
        }
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }

    public void dropConstraint(String tableName, String constraintName) throws SQLException {
        String query = "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName;
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }

    public void renameTable(String oldTableName, String newTableName) throws SQLException {
        String query = "ALTER TABLE " + oldTableName + " RENAME TO " + newTableName;
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }
    public void create_table(String table, String[] kolumny, String[] typy_danych) throws SQLException {

    }
    public void delete_table(String table) throws SQLException {
        String query = "DELETE FROM " + table;
        try (Statement statement = ensureConnection().createStatement()) {
            statement.execute(query);
        }
    }

    public void delete_table_where(String table, String where) throws SQLException {
        String query = "DELETE FROM " + table + " WHERE " + where;
        try (Statement statement = ensureConnection().createStatement()) {
            statement.execute(query);
        }
    }
    public void update_table(String table, String nazwa, String dane) throws SQLException {
        String query = "UPDATE " + table + " SET " + nazwa + " = ?";
        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            if (isNumeric(dane)) {
                if (dane.contains(".")) {
                    pstmt.setDouble(1, Double.parseDouble(dane));
                } else {
                    pstmt.setInt(1, Integer.parseInt(dane));
                }
            } else {
                pstmt.setString(1, dane);
            }
            pstmt.executeUpdate();
        }
    }

    public void update_table_where(String table, String nazwa, String dane, String where) throws SQLException {
        String query = "UPDATE " + table + " SET " + nazwa + " = ? WHERE " + where;
        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            if (isNumeric(dane)) {
                if (dane.contains(".")) {
                    pstmt.setDouble(1, Double.parseDouble(dane));
                } else {
                    pstmt.setInt(1, Integer.parseInt(dane));
                }
            } else {
                pstmt.setString(1, dane);
            }
            pstmt.executeUpdate();
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

    public void Drop_Table (String table) throws SQLException {
        String query = "DROP TABLE "+ table + " CASCADE CONSTRAINTS";
        Statement statement = ensureConnection().createStatement();
        statement.execute(query);
    }

    public ResultSet Select_Table(String dane, String table) throws SQLException {
        String query = "SELECT " + dane + " FROM " + table;
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet Select_Table_where(String dane, String table, String where) throws SQLException {
        String query = "SELECT " + dane + " FROM " + table + " WHERE " + where;
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet Select_Table_order(String dane, String table, String order) throws SQLException {
        String query = "SELECT " + dane + " FROM " + table + " ORDER BY  " + order;
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet Select_Table_where_order(String dane, String table,String where, String order) throws SQLException {
        String query = "SELECT " + dane + " FROM " + table +" WHERE " + where + " ORDER BY  " + order;
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ChoiceBox<String> get_column_names(String table_name) throws SQLException {
        String query = "SELECT * FROM " + table_name + " WHERE 1=0";
        Statement stmt = ensureConnection().createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            columnNames.add(columnName);
        }
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(columnNames);
        return choiceBox;
    }
    public List<String> getConstraintNames(String tableName) throws SQLException {
        List<String> constraints = new ArrayList<>();
        String query = "SELECT constraint_name FROM user_constraints WHERE table_name = ?";

        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            pstmt.setString(1, tableName.toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                constraints.add(rs.getString("constraint_name"));
            }
        }
        return constraints;
    }
    public void insertPracownik(int id, String imie, String nazwisko, int wiek,
                                double pensja, String miejscePracy) throws SQLException {
        String query = "INSERT INTO Pracownicy (Pracownik_ID, Imie, Nazwisko, Wiek, Pensja, Miejsce_Pracy) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, imie);
            pstmt.setString(3, nazwisko);
            pstmt.setInt(4, wiek);
            pstmt.setDouble(5, pensja);
            pstmt.setString(6, miejscePracy);
            pstmt.executeUpdate();
        }
    }

    public void Insert(String table, int id, String imie, String nazwisko, int wiek,
                                double pensja, String miejscePracy) throws SQLException {
        String query = "INSERT INTO " + table + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, imie);
            pstmt.setString(3, nazwisko);
            pstmt.setInt(4, wiek);
            pstmt.setDouble(5, pensja);
            pstmt.setString(6, miejscePracy);
            pstmt.executeUpdate();
        }
    }
    public void create_tab(String[] dane){


    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}