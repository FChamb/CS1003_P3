import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class InitialiseDB {
    public static void main(String[] args) {
        File file = new File("CS1003_P3DataBase");
        if (file.exists()) {
            file.delete();
        }
        file = new File(file.getName());
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scanner scan = null;
        Connection connection = null;
        try {
            scan = new Scanner(file);
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS popStars");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS popStars(Name VARCHAR(100) NOT NULL PRIMARY KEY, Label VARCHAR(100) NOT NULL, HighestNum INTEGER)");
            statement.close();
            statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM popStars");
            System.out.println("\ncontents of table:");
            while (result.next()) {
                String name = result.getString(1);
                String label = result.getString(2);
                int highest = result.getInt("HighestNum");
                System.out.println("name: " + name);
                System.out.println("label: " + label);
                System.out.println("highest: " + highest);
                System.out.println();
            }
            statement.close();
        } catch (SQLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}