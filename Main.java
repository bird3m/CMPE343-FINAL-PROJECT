import java.sql.Connection;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args)
    {
        // Veritabanı bağlantısını test ediyoruz
        try (Connection conn = DatabaseAdapter.getConnection())
        {
            if (conn != null)
            {
                System.out.println("Veritabanı bağlantısı başarılı!");
            }
            else
            {
                System.out.println("Veritabanına bağlanılamadı.");
            }
        }
        catch (SQLException e)
        {
            System.out.println("Bağlantı sırasında hata oluştu: " + e.getMessage());
        }
    }
}
