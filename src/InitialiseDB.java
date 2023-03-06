import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class InitialiseDB {
    public static void main(String[] args) {
        String fname = "CS1003_P3DataBase";
        File file = new File(fname);
        if (file.exists()) {
            file.delete();
        }
        file = new File(fname);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scanner scan = null;
        Connection connection = null;
        try {
            scan = new Scanner(new File("CreateDataBase.txt"));
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
            Statement statement = connection.createStatement();
            while (scan.hasNext()) {
                statement.executeUpdate(scan.nextLine());
            }
            statement.close();
            System.out.println("OK");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
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