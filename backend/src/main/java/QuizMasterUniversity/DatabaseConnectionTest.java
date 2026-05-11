package QuizMasterUniversity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/quizmaster_university";
        String user = "postgres";
        String password = "postgres";

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Подключение к базе данных успешно!");
            connection.close();
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Драйвер PostgreSQL не найден: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Ошибка подключения к БД: " + e.getMessage());
        }
    }
}