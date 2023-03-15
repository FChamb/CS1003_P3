import java.io.File;
import java.sql.*;
import java.time.Year;

public class QueryDB {
    /**
     * Main method in QueryDB which creates a new QueryDB object. First the command
     * line argument is found and if it is a valid choice, it is sent to the switch
     * case which decides the appropriate method to call. If the command line argument
     * is not a valid option the appropriate error message will print to terminal.
     * @param args - command line arguments for the method. Used to determine choice of query.
     */
    public static void main(String[] args) {
        int choice = -1;
        try {
            if (Integer.parseInt(args[0]) >= 1 && Integer.parseInt(args[0]) <= 5) {
                choice = Integer.parseInt(args[0]);
            } else {
                throw new IllegalArgumentException("Not a valid query choice!");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        QueryDB queryDB = new QueryDB();
        File file = new File("CS1003_P3DataBase");
        String path = "jdbc:sqlite:" + file.getName();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(path);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        switch (choice) {
            case 1 -> queryDB.Query1(connection);
            case 2 -> queryDB.Query2(connection);
            case 3 -> queryDB.Query3(connection);
            case 4 -> queryDB.Query4(connection);
            case 5 -> queryDB.Query5(connection);
        }
    }

    /**
     * Query1 takes a connection parameter and using a hard coded sql command the number of total publications
     * from Ozgur Akgun, Ian Gent, and Alan Dearle is printed to the terminal.
     * @param connection - connection to the database
     */
    public void Query1(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT count(DISTINCT Title) FROM Publications INNER JOIN AuthorOwner ON AuthorOwner.PublID = Publications.PublID WHERE AuthorOwner.AuthorID != 'Ian Gent'");
            resultSet.next();
            int num = resultSet.getInt(1);
            System.out.println("Total Number of Publications by “Ozgur Akgun” or “Ian Gent” or “Alan Dearle”: " + num);
            statement.close();
        } catch (SQLException e) {
        throw new RuntimeException(e);
        }
    }

    /**
     * Query2 takes a connection parameter and using a hard coded sql command the number of total publications
     * from Ozgur Akgun is printed to the terminal.
     * @param connection - connection to the database
     */
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

    /**
     * Query3 takes a connection parameter and using a hard coded sql command the distinct publication titles
     * from Ozgur Akgun are printed to the terminal. Join is used in this statement to link AuthorOwner and
     * Publications.
     * @param connection - connection to the database
     */
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

    /**
     * Query4 takes a connection parameter and using a hard coded sql command the distinct venue names
     * which Ozgur Akgun published at in the last three years are printed to the terminal. Join is used
     * twice in this statement to link AuthorOwner, Publications, and Venues. A year object is found and
     * used to determine if the publication was published in the past three years.
     * @param connection - connection to the database
     */
    public void Query4(Connection connection) {
        try {
            String year = String.valueOf(Year.now().getValue() - 3);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT(Venues.Name) FROM Venues INNER JOIN Publications ON Venues.Name = Publications.VenID INNER JOIN AuthorOwner ON AuthorOwner.PublID = Publications.PublID WHERE AuthorOwner.AuthorID = 'Özgür Akgün' AND Publications.YearOfOcc >= " + year);
            while(resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Query5 takes a connection parameter and using a hard coded sql command the publication titles and venue
     * names from Ozgur Akgun are printed to the terminal. Join is used twice in this statement to link
     * AuthorOwner, Publications, and Venues.
     * @param connection - connection to the database
     */
    public void Query5(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Publications.Title, Venues.Name FROM Publications INNER JOIN Venues ON Publications.VenID = Venues.Name INNER JOIN AuthorOwner ON AuthorOwner.PublID = Publications.PublID WHERE AuthorOwner.AuthorID = 'Özgür Akgün'");
            while(resultSet.next()) {
                System.out.println(resultSet.getString(1) + "               " + resultSet.getString(2));
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
