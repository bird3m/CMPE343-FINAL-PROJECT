package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
            // JDBC sürücüsünü manuel yüklememize gerek yok, zaten classpath'te
            return DriverManager.getConnection(URL, USER, PASS);
        }
        catch (SQLException e)
        {
            System.out.println("Veritabanı bağlantısı başarısız!");
            System.out.println("Hata: " + e.getMessage());
            e.printStackTrace();  // Detaylı hata çıktısı al
            return null;
        }
    }
}
