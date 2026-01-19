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
    public List<org.example.ColumnInfo> getColumnDetails(String tableName) throws SQLException {
        List<org.example.ColumnInfo> columnDetails = new ArrayList<>();

        String query = "SELECT column_name, data_type, nullable FROM user_tab_columns WHERE table_name = ? ORDER BY column_id";

        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            pstmt.setString(1, tableName.toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                String nullableStr = rs.getString("nullable");
                boolean nullable = "Y".equalsIgnoreCase(nullableStr);

                columnDetails.add(new org.example.ColumnInfo(columnName, dataType, nullable));
            }
        }

        return columnDetails;
    }
    public void insertIntoTable(String tableName, List<String> columnNames, List<Object> values) throws SQLException {
        if (columnNames.size() != values.size()) {
            throw new IllegalArgumentException("Liczba kolumn i wartości musi być taka sama");
        }
        validateInsertData(tableName, columnNames, values);

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
                String columnName = columnNames.get(i);

                if (value == null) {
                    if (!isColumnNullable(tableName, columnName)) {
                        throw new SQLException("Kolumna '" + columnName + "' nie pozwala wartości NULL");
                    }
                    pstmt.setNull(i + 1, java.sql.Types.VARCHAR);
                } else {
                    String strValue = value.toString();
                    String columnType = getColumnType(tableName, columnName);
                    setPreparedStatementValue(pstmt, i + 1, strValue, columnType);
                }
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Wstawienie danych nie powiodło się");
            }
        }
    }

    private void validateInsertData(String tableName, List<String> columnNames, List<Object> values) throws SQLException {
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            Object value = values.get(i);
            String columnType = getColumnType(tableName, columnName);

            if (value != null) {
                String strValue = value.toString();
                if (columnType.toUpperCase().contains("NUMBER")) {
                    try {
                        if (columnType.toUpperCase().contains(",")) {
                            Double.parseDouble(strValue);
                        } else {
                            Long.parseLong(strValue);
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Nieprawidłowa wartość dla kolumny '" + columnName +
                                "'. Oczekiwano liczby typu: " + columnType);
                    }
                }
                else if (columnType.toUpperCase().contains("DATE")) {
                    if (!isValidDateValue(strValue)) {
                        throw new IllegalArgumentException("Nieprawidłowy format daty dla kolumny '" + columnName +
                                "'. Użyj formatu YYYY-MM-DD");
                    }
                }
            }
        }
    }

    private String getColumnType(String tableName, String columnName) throws SQLException {
        String query = "SELECT data_type FROM user_tab_columns WHERE table_name = ? AND column_name = ?";

        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            pstmt.setString(1, tableName.toUpperCase());
            pstmt.setString(2, columnName.toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("data_type");
            } else {
                throw new SQLException("Nie znaleziono kolumny '" + columnName + "' w tabeli '" + tableName + "'");
            }
        }
    }

    private boolean isColumnNullable(String tableName, String columnName) throws SQLException {
        String query = "SELECT nullable FROM user_tab_columns WHERE table_name = ? AND column_name = ?";

        try (PreparedStatement pstmt = ensureConnection().prepareStatement(query)) {
            pstmt.setString(1, tableName.toUpperCase());
            pstmt.setString(2, columnName.toUpperCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return "Y".equalsIgnoreCase(rs.getString("nullable"));
            }
        }
        return false;
    }
    private void setPreparedStatementValue(PreparedStatement pstmt, int index, String value, String columnType)
            throws SQLException {
        if (value == null || value.isEmpty()) {
            pstmt.setNull(index, java.sql.Types.VARCHAR);
            return;
        }

        columnType = columnType.toUpperCase();

        try {
            if (columnType.contains("NUMBER") || columnType.contains("INT") ||
                    columnType.contains("FLOAT") || columnType.contains("DECIMAL")) {

                if (columnType.contains(",") || columnType.contains("FLOAT") || columnType.contains("DECIMAL")) {
                    pstmt.setDouble(index, Double.parseDouble(value));
                } else {
                    pstmt.setLong(index, Long.parseLong(value));
                }

            } else if (columnType.contains("DATE") || columnType.contains("TIMESTAMP")) {
                pstmt.setDate(index, java.sql.Date.valueOf(value));
            } else {
                pstmt.setString(index, value);
            }
        } catch (NumberFormatException e) {
            throw new SQLException("Nie można przekonwertować wartości '" + value +
                    "' na typ: " + columnType);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Nieprawidłowy format danych dla wartości '" + value +
                    "' i typu: " + columnType);
        }
    }

    private boolean isValidDateValue(String value) {
        try {
            java.sql.Date.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public void addColumn(String tableName, String columnName, String dataType) throws SQLException {
        String query = "ALTER TABLE " + tableName + " ADD (" + columnName + " " + dataType + ")";
        System.out.println("SQL: " + query); // Debug
        try (Statement stmt = ensureConnection().createStatement()) {
            stmt.execute(query);
        }
    }
    public void addColumnWithDefault(String tableName, String columnName, String dataType, String defaultValue) throws SQLException {
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

    public ResultSet Select_Table_where_order(String dane, String table, String where, String order) throws SQLException {
        String query = "SELECT " + dane + " FROM " + table + " WHERE " + where + " ORDER BY " + order;
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
        try {
            Double.parseDouble(trimmed);
            return trimmed;
        } catch (NumberFormatException e) {
            if (trimmed.equalsIgnoreCase("SYSDATE") ||
                    trimmed.equalsIgnoreCase("CURRENT_TIMESTAMP") ||
                    trimmed.equalsIgnoreCase("NULL")) {
                return trimmed.toUpperCase();
            }
            return "'" + trimmed + "'";
        }
    }
    public Map<String, Object> generujRaportSprzedazyBiletow(int zooId, int miesiac, int rok) throws SQLException {
        if (zooId <= 0) {
            throw new IllegalArgumentException("ID zoo musi być liczbą dodatnią");
        }

        if (miesiac < 1 || miesiac > 12) {
            throw new IllegalArgumentException("Miesiąc musi być w zakresie 1-12");
        }

        if (rok < 1900 || rok > 2100) {
            throw new IllegalArgumentException("Rok musi być w rozsądnym zakresie");
        }

        Map<String, Object> result = new HashMap<>();

        try (CallableStatement stmt = ensureConnection().prepareCall(
                "{call GenerujRaportSprzedazyBiletow(?, ?, ?, ?, ?)}")) {

            stmt.setInt(1, zooId);
            stmt.setInt(2, miesiac);
            stmt.setInt(3, rok);
            stmt.registerOutParameter(4, OracleTypes.CURSOR);
            stmt.registerOutParameter(5, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rsPodsumowanie = (ResultSet) stmt.getObject(4)) {
                if (rsPodsumowanie.next()) {
                    result.put("nazwa_zoo", rsPodsumowanie.getString("nazwa_zoo"));
                    result.put("okres", rsPodsumowanie.getString("okres"));
                    result.put("laczna_sprzedaz", rsPodsumowanie.getDouble("laczna_sprzedaz"));
                    result.put("liczba_biletow", rsPodsumowanie.getInt("liczba_biletow"));
                    result.put("srednia_cena", rsPodsumowanie.getDouble("srednia_cena"));
                } else {
                    throw new SQLException("Brak danych dla podanych parametrów");
                }
            }

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

    public Map<String, Object> dodajZwierze(String nazwaZwierzęcia, String nazwaGatunku, String nazwaKlatki, int idOpiekuna) throws SQLException {
        if (nazwaZwierzęcia == null || nazwaZwierzęcia.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa zwierzęcia nie może być pusta");
        }

        if (nazwaGatunku == null || nazwaGatunku.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa gatunku nie może być pusta");
        }

        if (nazwaKlatki == null || nazwaKlatki.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa klatki nie może być pusta");
        }

        if (idOpiekuna <= 0) {
            throw new IllegalArgumentException("ID opiekuna musi być liczbą dodatnią");
        }

        if (nazwaZwierzęcia.length() > 50) {
            throw new IllegalArgumentException("Nazwa zwierzęcia nie może przekraczać 50 znaków");
        }

        if (nazwaGatunku.length() > 50) {
            throw new IllegalArgumentException("Nazwa gatunku nie może przekraczać 50 znaków");
        }

        if (nazwaKlatki.length() > 50) {
            throw new IllegalArgumentException("Nazwa klatki nie może przekraczać 50 znaków");
        }

        Map<String, Object> result = new HashMap<>();

        try (CallableStatement stmt = ensureConnection().prepareCall(
                "{call DodajZwierze(?, ?, ?, ?, ?, ?)}")) {

            stmt.setString(1, nazwaZwierzęcia.trim());
            stmt.setString(2, nazwaGatunku.trim());
            stmt.setString(3, nazwaKlatki.trim());
            stmt.setInt(4, idOpiekuna);
            stmt.registerOutParameter(5, Types.NUMERIC);
            stmt.registerOutParameter(6, Types.VARCHAR);

            stmt.execute();

            int newId = stmt.getInt(5);
            String komunikat = stmt.getString(6);

            if (newId <= 0 && komunikat.contains("Dodano")) {
                throw new SQLException("Nieprawidłowe ID zwierzecia zwrócone przez procedurę");
            }

            result.put("nowe_zwierze_id", newId);
            result.put("komunikat", komunikat);

        } catch (SQLException e) {
            throw new SQLException("Błąd podczas dodawania zwierzęcia: " + e.getMessage());
        }

        return result;
    }

    public String znajdzZwierzePoNazwie(String nazwaZwierzęcia) throws SQLException {
        // Walidacja wejścia
        if (nazwaZwierzęcia == null || nazwaZwierzęcia.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa zwierzęcia do wyszukania nie może być pusta");
        }

        if (nazwaZwierzęcia.trim().length() > 50) {
            throw new IllegalArgumentException("Nazwa zwierzęcia do wyszukania nie może przekraczać 50 znaków");
        }

        String wynik = null;

        try (CallableStatement stmt = ensureConnection().prepareCall(
                "{ ? = call ZnajdzZwierzePoNazwie(?) }")) {

            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.setString(2, nazwaZwierzęcia.trim());

            stmt.execute();

            wynik = stmt.getString(1);

            if (wynik == null) {
                wynik = "Nie znaleziono zwierzęcia o podanej nazwie";
            }

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

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

}