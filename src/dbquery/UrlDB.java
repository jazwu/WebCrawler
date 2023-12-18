package dbquery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UrlDB {
    private static final Object lock = new Object();
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/myuser";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "535897";

    public static void createDatabase(String dbname){
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            // Create a Statement object
            try (Statement statement = connection.createStatement()) {
                // SQL query to create a new database
                String createDatabaseQuery = "CREATE DATABASE " + dbname;
                // Execute the query
                statement.executeUpdate(createDatabaseQuery);
                System.out.println("Database '" + dbname + "' created successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean contains(String dbname, String url) {
        synchronized (lock) {
            try {
                Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                String selectQuery = "SELECT COUNT(*) FROM ? WHERE url = ?"; // '?' is a placeholder
                try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                    preparedStatement.setString(1, dbname);
                    preparedStatement.setString(2, url);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false; // Handle the exception appropriately based on your requirements
            }
        }
    }

    public static void saveUrlToDatabase(String dbname, String url) {
        synchronized (lock) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                String insertQuery = "INSERT INTO ? (url) VALUES (?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, dbname);
                    preparedStatement.setString(2, url);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle the exception appropriately based on your requirements
            }
        }
    }
}
