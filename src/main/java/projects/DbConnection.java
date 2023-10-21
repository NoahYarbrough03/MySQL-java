package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import projects.exception.DbException;

public class DbConnection {
    // Constants for MySQL connection
    private static final String HOST = "localhost";
    private static final String PORT = "3306"; 
    private static final String SCHEMA = "projects";
    private static final String USER = "projects";
    private static final String PASSWORD = "projects";

    // JDBC URL for the MySQL database
    public static String URI = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", HOST, PORT, SCHEMA, USER, PASSWORD);


    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URI);
            System.out.println("Connected to the MySQL database." + URI);
            return connection;
        } catch (SQLException e) {
            throw new DbException("Failed to connect to the database.", e);
        }
    }
}