import java.io.File;
import java.sql.*;

public class QueryDB {
    public static void main(String[] args) {
        int choice = Integer.parseInt(args[0]);
        QueryDB queryDB = new QueryDB();
        File file = new File("CS1003_P3DataBase");
        String path = "jdbc:sqlite:" + file.getName();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(path);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (choice == 1) {
            queryDB.Query1(connection);
        } else if (choice == 2) {
            queryDB.Query2();
        } else if (choice == 3) {
            queryDB.Query3();
        } else if (choice == 4) {
            queryDB.Query4();
        } else if (choice == 5) {
            queryDB.Query5();
        } else {
            try {
                throw new Exception("Not a valid query option!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Query1(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT count(DISTINCT Title) FROM Publications");
            resultSet.next();
            int num = resultSet.getInt(1);
            System.out.println("Total Number of Publications by “Ozgur Akgun” or “Ian Gent” or “Alan Dearle”: " + num);
            statement.close();
        } catch (SQLException e) {
        throw new RuntimeException(e);
        }
    }

    public void Query2() {

    }

    public void Query3() {

    }

    public void Query4() {

    }

    public void Query5() {

    }
}
