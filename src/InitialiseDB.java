import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class InitialiseDB {
    /**
     * The main method is half of the functionality for this class. It is run when the class is executed, and it does
     * three things. First it checks to see if the directory contains an SQLLite database. If so the file is deleted.
     * If the file does not exist or the database has been deleted, then a new database is created in its place.
     * Finally, a scanner object reads through the sql commands in CreateDataBase.txt, and executes each one.
     * @param args - command line arguments for the method. They are never used or needed for program to function.
     */
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
            if (checkInitialized(connection, fname)) {
                System.out.println("OK");
            } else {
                throw new FileNotFoundException("Database not formatted properly!");
            }
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
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

    /**
     * checkInitialized is the second half of this class. It takes a file name for the database and
     * checks to see if it has been properly initialized. If 
     * @param fname
     * @return
     */
    public static boolean checkInitialized(Connection connection, String fname) {
        String command = "SELECT Name FROM sqlite_master WHERE type = 'table' AND Name = Authors";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(command);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return false;
    }
}