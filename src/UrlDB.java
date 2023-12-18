import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UrlDB {
    private static final Object lock = new Object();
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/your_database_name";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public boolean isAlreadyVisited(String url) {
        synchronized (lock) {
            try {
                Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                String selectQuery = "SELECT COUNT(*) FROM crawled_urls WHERE url = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                    preparedStatement.setString(1, url);
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

    public void saveUrlToDatabase(String url) {
        synchronized (lock) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                String insertQuery = "INSERT INTO crawled_urls (url) VALUES (?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, url);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle the exception appropriately based on your requirements
            }
        }
    }
}
