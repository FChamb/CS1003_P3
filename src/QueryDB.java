import java.io.File;
import java.sql.*;
import java.time.Year;

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
            queryDB.Query2(connection);
        } else if (choice == 3) {
            queryDB.Query3(connection);
        } else if (choice == 4) {
            queryDB.Query4(connection);
        } else if (choice == 5) {
            queryDB.Query5(connection);
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

    public void Query2(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT NumOfPubl FROM Authors WHERE Name = 'Özgür Akgün'");
            resultSet.next();
            int num = resultSet.getInt(1);
            System.out.println("Total Number of Publications by “Özgür Akgün”: " + num);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void Query3(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT(Publications.Title) FROM Publications INNER JOIN AuthorOwner ON AuthorOwner.PublID = Publications.PublID WHERE AuthorOwner.AuthorID = 'Özgür Akgün'");
            while(resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void Query4(Connection connection) {
        try {
            String year = String.valueOf(Year.now().getValue() - 3);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT(Venues.Name) FROM Venues INNER JOIN Publications ON Venues.Name = Publications.VenID INNER JOIN AuthorOwner ON AuthorOwner.PublID = Publications.PublID WHERE AuthorOwner.AuthorID = 'Özgür Akgün' AND Publications.YearOfOcc > " + year);
            while(resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void Query5(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Publications.Title, Venues.Name FROM Publications INNER JOIN Venues ON Publications.VenID = Venues.Name INNER JOIN AuthorOwner ON AuthorOwner.PublID = Publications.PublID WHERE AuthorOwner.AuthorID = 'Özgür Akgün'");
            while(resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
