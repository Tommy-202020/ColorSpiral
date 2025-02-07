package mc.highlitemc.colorSpiral;

import java.sql.*;

public class DatabaseHelper {

    private final String dbFilePath;

    public DatabaseHelper(String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }
    public void createTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
             Statement statement = connection.createStatement()) {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY," +
                    "number INTEGER" +
                    ");";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerData(String uuid, int number) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
             Statement statement = connection.createStatement()) {

            String insertDataSQL = "INSERT OR REPLACE INTO players (uuid, number) VALUES ('" + uuid + "', " + number + ");";
            statement.execute(insertDataSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Integer loadPlayerData(String uuid) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
             PreparedStatement statement = connection.prepareStatement("SELECT number FROM players WHERE uuid = ?")) {

            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
