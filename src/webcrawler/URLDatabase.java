package webcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class URLDatabase {
    private Object lock;
    private String dbname;
    private Connection connection;

    public URLDatabase(String dbname, String user, String password){
        this.dbname = dbname;
        this.lock = new Object();

        try {
            // connect to databases in MySQL
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+dbname, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable(String tbname){
            try (Statement statement = connection.createStatement()) {
                String dropTableQuery = "DROP TABLE IF EXISTS "+tbname;
                statement.executeUpdate(dropTableQuery);

                String createTableQuery = "CREATE TABLE "+tbname+" (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "url VARCHAR(255))";
                        // "age INT," +
                        // "email VARCHAR(255))";
                statement.executeUpdate(createTableQuery);

                System.out.println("Table "+tbname+" created successfully in database "+this.dbname);
            } catch(SQLException e){
                e.printStackTrace();
            }
    }

    public boolean contains(String tbname, String url) {
        synchronized (lock) {
            String selectQuery = "SELECT COUNT(*) FROM "+tbname+" WHERE url = ?"; // '?' is a placeholder
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, url);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next(); // resultSet initially before the first row
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false; 
            }
        }
    }

    public void saveData(String tbname, String url) {
        synchronized (lock) {
            String insertQuery = "INSERT INTO "+tbname+" (url) VALUES (?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, url);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int urlCount(String tbname){
        String countQuery = "SELECT COUNT(*) FROM "+tbname;
        try(Statement statement = connection.createStatement()){
            try(ResultSet resultSet = statement.executeQuery(countQuery)){
                resultSet.next();
                int count = resultSet.getInt(1);
                return count;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return 0;
        }
    }
}
