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
    public void update_table(String table, String nazwa, String dane) throws SQLException {
        String query = "UPDATE "+table+" SET " + nazwa + " = " +dane;
        Statement statement = ensureConnection().createStatement();
        statement.execute(query);
    }

    public void update_table_where(String table, String nazwa, String dane, String where) throws SQLException {
        String query = "UPDATE "+table+" SET " + nazwa + " = " +dane;
        Statement statement = ensureConnection().createStatement();
        statement.execute(query);
    }

    public void Drop_Table (String table) throws SQLException {
        String query = "DROP TABLE "+ table + " CASCADE CONSTRAINTS";
        Statement statement = ensureConnection().createStatement();
        statement.execute(query);
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
    public ResultSet getPracownicy(String dane) throws SQLException {
        String query = "SELECT " + dane + " FROM Pracownicy";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getPracownicyWhere(String dane, String warunek) throws SQLException {
        String query = "SELECT " + dane + " FROM Pracownicy WHERE " + warunek;
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getPracownicyOrderBy(String dane, String kolejcosc) throws SQLException {
        String query = "SELECT " + dane + " FROM Pracownicy ORDER BY " + kolejcosc;
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
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
    public void create_tab(String[] dane){


    }
    public ResultSet getBilety(String dane) throws SQLException {
        String query = "SELECT " + dane + " FROM Bilet";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getKlienci(String dane) throws SQLException {
        String query = "SELECT " + dane + " FROM Klienci";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getWybiegi(String dane) throws SQLException {
        String query = "SELECT " + dane + " FROM Wybiegi";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getKlatki(String dane) throws SQLException {
        String query = "SELECT " + dane + " FROM Klatki";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getKarmienia(String dane) throws SQLException {
        String query = "SELECT " + dane + " FROM Karmienia";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public void updatePracownicy(String nazwa, String dane) throws SQLException {
        String query = "UPDATE Pracownicy SET " + nazwa + " = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void updatePracownicyWhere(String nazwa, String dane, String warunek) throws SQLException {
        String query = "UPDATE Pracownicy SET " + nazwa + " = ? WHERE " + warunek;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void updateBilet(String nazwa, String dane) throws SQLException {
        String query = "UPDATE Bilet SET " + nazwa + " = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void updateBiletWhere(String nazwa, String dane, String warunek) throws SQLException {
        String query = "UPDATE Bilet SET " + nazwa + " = ? WHERE " + warunek;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void insertKlient(int klientId, String imie, int wiek,
                             String zooOdwiedzone, int numerBiletu) throws SQLException {
        String query = "INSERT INTO Klienci (Klient_ID, Imie, Wiek, Zoo_odwiedzone, Numer_biletu) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, klientId);
            pstmt.setString(2, imie);
            pstmt.setInt(3, wiek);
            pstmt.setString(4, zooOdwiedzone);
            pstmt.setInt(5, numerBiletu);
            pstmt.executeUpdate();
        }
    }

    public ResultSet getKlienciWhere(String dane, String warunek) throws SQLException {
        String query = "SELECT " + dane + " FROM Klienci WHERE " + warunek;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getKlienciOrderBy(String dane, String order) throws SQLException {
        String query = "SELECT " + dane + " FROM Klienci ORDER BY " + order;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public void updateKlienci(String nazwa, String dane) throws SQLException {
        String query = "UPDATE Klienci SET " + nazwa + " = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void updateKlienciWhere(String nazwa, String dane, String warunek) throws SQLException {
        String query = "UPDATE Klienci SET " + nazwa + " = ? WHERE " + warunek;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public ResultSet getWybiegiWhere(String dane, String warunek) throws SQLException {
        String query = "SELECT " + dane + " FROM Wybiegi WHERE " + warunek;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public void updateWybiegi(String nazwa, String dane) throws SQLException {
        String query = "UPDATE Wybiegi SET " + nazwa + " = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void updateWybiegiWhere(String nazwa, String dane, String warunek) throws SQLException {
        String query = "UPDATE Wybiegi SET " + nazwa + " = ? WHERE " + warunek;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public ResultSet getKlatkiWhere(String dane, String warunek) throws SQLException {
        String query = "SELECT " + dane + " FROM Klatki WHERE " + warunek;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public void updateKlatki(String nazwa, String dane) throws SQLException {
        String query = "UPDATE Klatki SET " + nazwa + " = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void updateKlatkiWhere(String nazwa, String dane, String warunek) throws SQLException {
        String query = "UPDATE Klatki SET " + nazwa + " = ? WHERE " + warunek;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public ResultSet getKarmieniaWhere(String dane, String warunek) throws SQLException {
        String query = "SELECT " + dane + " FROM Karmienia WHERE " + warunek;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    public void updateKarmienia(String nazwa, String dane) throws SQLException {
        String query = "UPDATE Karmienia SET " + nazwa + " = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void updateKarmieniaWhere(String nazwa, String dane, String warunek) throws SQLException {
        String query = "UPDATE Karmienia SET " + nazwa + " = ? WHERE " + warunek;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, dane);
            pstmt.executeUpdate();
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}