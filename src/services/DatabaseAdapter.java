package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple database connection helper.
 * Provides a JDBC connection to the application's MySQL database.
 */
public class DatabaseAdapter
{
    private static final String URL =
        "jdbc:mysql://localhost:3306/greengrocer_group4?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "myuser";
    private static final String PASS = "1234";

    public static Connection getConnection()
    {
        try
        {
            // Obtain a JDBC connection from DriverManager
            return DriverManager.getConnection(URL, USER, PASS);
        }
        catch (SQLException e)
        {
            System.err.println("Veritabanı bağlantısı başarısız!");
            System.err.println("Hata: " + e.getMessage());
            e.printStackTrace();  // print full stack trace for diagnostics
            return null;
        }
    }
}