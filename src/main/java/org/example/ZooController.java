package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import oracle.jdbc.internal.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        String query = "ALTER TABLE " + tableName + " ADD (" + columnName + " " + dataType + ")";
        System.out.println("SQL: " + query); // Debug
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }
    public void addColumnWithDefault(String tableName, String columnName, String dataType, String defaultValue) throws SQLException {
        // Sprawdź, czy wartość domyślna wymaga apostrofów
        String formattedDefault = formatDefaultValue(defaultValue);

        String query = "ALTER TABLE " + tableName + " ADD (" + columnName + " " + dataType + " DEFAULT " + formattedDefault + ")";
        System.out.println("SQL: " + query); // Debug
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }
    public void dropColumn(String tableName, String columnName) throws SQLException {
        String query = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName;
        System.out.println("SQL: " + query);
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

    public void insertIntoTable(String tableName, List<String> columnNames, List<Object> values) throws SQLException {
        if (columnNames.size() != values.size()) {
            throw new IllegalArgumentException("Liczba kolumn i wartości musi być taka sama");
        }
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < columnNames.size(); i++) {
            queryBuilder.append(columnNames.get(i));
            if (i < columnNames.size() - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(") VALUES (");
        for (int i = 0; i < values.size(); i++) {
            queryBuilder.append("?");
            if (i < values.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(")");

        String query = queryBuilder.toString();

        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                if (value == null) {
                    pstmt.setNull(i + 1, java.sql.Types.VARCHAR);
                } else {
                    String strValue = value.toString();
                    if (isNumeric(strValue)) {
                        if (strValue.contains(".")) {
                            pstmt.setDouble(i + 1, Double.parseDouble(strValue));
                        } else {
                            pstmt.setInt(i + 1, Integer.parseInt(strValue));
                        }
                    } else {
                        pstmt.setString(i + 1, strValue);
                    }
                }
            }

            pstmt.executeUpdate();
        }
    }
    public void createTable(String tableName, List<CreateController.ColumnDefinition> columns) throws SQLException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa tabeli nie może być pusta");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Tabela musi mieć co najmniej jedną kolumnę");
        }
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE ").append(tableName).append(" (\n");
        List<String> primaryKeys = new ArrayList<>();
        List<String> foreignKeys = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            CreateController.ColumnDefinition column = columns.get(i);
            queryBuilder.append("    ").append(column.getColumnName())
                    .append(" ").append(column.getDataType());
            if (column.getConstraint() != null && !column.getConstraint().isEmpty()) {
                if ("PRIMARY KEY".equals(column.getConstraint())) {
                    primaryKeys.add(column.getColumnName());
                } else if ("FOREIGN KEY".equals(column.getConstraint())) {
                    if (column.getForeignKeyTable() != null && column.getForeignKeyColumn() != null) {
                        foreignKeys.add("    FOREIGN KEY (" + column.getColumnName() + ") REFERENCES "
                                + column.getForeignKeyTable() + "(" + column.getForeignKeyColumn() + ")");
                    }
                } else {
                    queryBuilder.append(" ").append(column.getConstraint());
                }
            }

            if (i < columns.size() - 1 || !primaryKeys.isEmpty() || !foreignKeys.isEmpty()) {
                queryBuilder.append(",");
            }
            queryBuilder.append("\n");
        }
        if (!primaryKeys.isEmpty()) {
            queryBuilder.append("    CONSTRAINT ").append(tableName).append("_PK PRIMARY KEY (")
                    .append(String.join(", ", primaryKeys)).append(")");
            if (!foreignKeys.isEmpty()) {
                queryBuilder.append(",");
            }
            queryBuilder.append("\n");
        }
        for (int i = 0; i < foreignKeys.size(); i++) {
            queryBuilder.append(foreignKeys.get(i));
            if (i < foreignKeys.size() - 1) {
                queryBuilder.append(",");
            }
            queryBuilder.append("\n");
        }
        queryBuilder.append(")");
        String query = queryBuilder.toString();
        System.out.println("Executing query:\n" + query);

        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
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

    public ChoiceBox<String> get_table_names() throws SQLException {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        String query = "SELECT table_name FROM user_tables " + "WHERE table_name NOT LIKE 'LOGMNR%' " + "AND table_name NOT LIKE 'LOGSTDBY$%' " + "AND table_name NOT LIKE 'ROLLING$%' " + "AND table_name NOT LIKE 'REPL_%' " + "AND table_name NOT LIKE 'OL$%' " + "AND table_name <> 'SQLPLUS_PRODUCT_PROFILE' " + "ORDER BY table_name";
        //Te wszystkie NOT LIKE żeby śmieci się nie pokazywały
        Statement stmt = ensureConnection().createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ObservableList<String> tableNames = FXCollections.observableArrayList();
        while (rs.next()) {
            tableNames.add(rs.getString("table_name"));
        }
        choiceBox.setItems(tableNames);
        if (!tableNames.isEmpty()) {
            choiceBox.setValue(tableNames.get(0));
        }
        rs.close();
        stmt.close();
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

    private String formatDefaultValue(String defaultValue) {
        if (defaultValue == null || defaultValue.trim().isEmpty()) {
            return "NULL";
        }

        String trimmed = defaultValue.trim();

        // Sprawdź czy to już jest w apostrofach
        if (trimmed.startsWith("'") && trimmed.endsWith("'")) {
            return trimmed;
        }

        // Sprawdź czy to liczba
        try {
            Double.parseDouble(trimmed);
            return trimmed; // Liczby nie potrzebują apostrofów
        } catch (NumberFormatException e) {
            // Sprawdź czy to funkcja SQL
            if (trimmed.equalsIgnoreCase("SYSDATE") ||
                    trimmed.equalsIgnoreCase("CURRENT_TIMESTAMP") ||
                    trimmed.equalsIgnoreCase("NULL")) {
                return trimmed.toUpperCase();
            }

            // Dla wartości tekstowych - dodaj apostrofy
            return "'" + trimmed + "'";
        }
    }
    public Map<String, Object> generujRaportSprzedazyBiletow(int zooId, int miesiac, int rok) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        try (CallableStatement stmt = ensureConnection().prepareCall(
                "{call GenerujRaportSprzedazyBiletow(?, ?, ?, ?, ?)}")) {

            stmt.setInt(1, zooId);
            stmt.setInt(2, miesiac);
            stmt.setInt(3, rok);
            stmt.registerOutParameter(4, OracleTypes.CURSOR);  // Podsumowanie
            stmt.registerOutParameter(5, OracleTypes.CURSOR);  // Szczegóły

            stmt.execute();

            // Pobranie podsumowania
            try (ResultSet rsPodsumowanie = (ResultSet) stmt.getObject(4)) {
                if (rsPodsumowanie.next()) {
                    result.put("nazwa_zoo", rsPodsumowanie.getString("nazwa_zoo"));
                    result.put("okres", rsPodsumowanie.getString("okres"));
                    result.put("laczna_sprzedaz", rsPodsumowanie.getDouble("laczna_sprzedaz"));
                    result.put("liczba_biletow", rsPodsumowanie.getInt("liczba_biletow"));
                    result.put("srednia_cena", rsPodsumowanie.getDouble("srednia_cena"));
                }
            }

            // Pobranie szczegółów
            List<Map<String, Object>> szczegoly = new ArrayList<>();
            try (ResultSet rsSzczegoly = (ResultSet) stmt.getObject(5)) {
                while (rsSzczegoly.next()) {
                    Map<String, Object> detal = new HashMap<>();
                    detal.put("data_biletu", rsSzczegoly.getString("data_biletu"));
                    detal.put("cena", rsSzczegoly.getDouble("cena"));
                    detal.put("klient_id", rsSzczegoly.getInt("klient_id"));
                    szczegoly.add(detal);
                }
            }
            result.put("szczegoly", szczegoly);

        } catch (SQLException e) {
            throw new SQLException("Błąd podczas generowania raportu: " + e.getMessage());
        }

        return result;
    }

    /**
     * Wywołanie procedury DodajZwierze
     */
    public Map<String, Object> dodajZwierze(String nazwaZwierzęcia, String nazwaGatunku,
                                            String nazwaKlatki, int idOpiekuna) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        try (CallableStatement stmt = ensureConnection().prepareCall(
                "{call DodajZwierze(?, ?, ?, ?, ?, ?)}")) {

            stmt.setString(1, nazwaZwierzęcia);
            stmt.setString(2, nazwaGatunku);
            stmt.setString(3, nazwaKlatki);
            stmt.setInt(4, idOpiekuna);
            stmt.registerOutParameter(5, Types.NUMERIC);  // nowe_zwierze_id
            stmt.registerOutParameter(6, Types.VARCHAR);  // komunikat

            stmt.execute();

            result.put("nowe_zwierze_id", stmt.getInt(5));
            result.put("komunikat", stmt.getString(6));

        } catch (SQLException e) {
            throw new SQLException("Błąd podczas dodawania zwierzęcia: " + e.getMessage());
        }

        return result;
    }

    /**
     * Wywołanie funkcji ZnajdzZwierzePoNazwie
     */
    public String znajdzZwierzePoNazwie(String nazwaZwierzęcia) throws SQLException {
        String wynik = null;

        try (CallableStatement stmt = ensureConnection().prepareCall(
                "{ ? = call ZnajdzZwierzePoNazwie(?) }")) {

            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.setString(2, nazwaZwierzęcia);

            stmt.execute();

            wynik = stmt.getString(1);

        } catch (SQLException e) {
            throw new SQLException("Błąd podczas wyszukiwania zwierzęcia: " + e.getMessage());
        }

        return wynik;
    }

    public ResultSet getZooList() throws SQLException {
        String query = "SELECT Zoo_ID, Nazwa FROM Zoo ORDER BY Nazwa";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getPracownicyList() throws SQLException {
        String query = "SELECT Pracownik_ID, Imie || ' ' || Nazwisko AS Nazwa FROM Pracownicy ORDER BY Nazwisko";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getKlatkiList() throws SQLException {
        String query = "SELECT Klatka_ID, Nazwa FROM Klatki ORDER BY Nazwa";
        Statement stmt = ensureConnection().createStatement();
        return stmt.executeQuery(query);
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}