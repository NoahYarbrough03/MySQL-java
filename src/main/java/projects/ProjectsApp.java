package projects;

import java.sql.Connection;
import projects.dao.DbConnection;
import projects.exception.DbException;

public class ProjectsApp {
    public static void main(String[] args) {
        Connection connection = null;
        
        try {
            // Attempt to obtain a connection to the database
            connection = DbConnection.getConnection();

            // Your application logic can go here
            // For example, you can execute SQL queries using 'connection'

            System.out.println("Database connection successful.");
        } catch (DbException e) {
            // Handle database connection exception
            System.err.println("Database connection error: " + e.getMessage());
        } finally {
            // Close the connection in the 'finally' block to ensure it's always closed
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    System.err.println("Failed to close the database connection: " + e.getMessage());
                }
            }
        }
    }
}
