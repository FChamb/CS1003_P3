import java.sql.*;

public class Test {
    public static void main(String[] args) {
        Test test = new Test();
        System.out.println("----------Testing Initialization----------");
        test.testInitialization();
        System.out.println("------------------------------------------");
        System.out.println("-----------Testing Load Authors-----------");
        test.loadAuthors();
        System.out.println("------------------------------------------");
        System.out.println("-----------Testing Print Query------------");
        test.printAllQuery();
        System.out.println("------------------------------------------");
    }

    /**
     * This method creates a new InitializedDB object and creates a connection to the
     * database. That connection is passed to checkInitialized() which runs through
     * a check to see if the tables in the database have been properly initialized
     * and formatted.
     */
    public void testInitialization() {
        System.out.println("Checking to see if database has been initialized...");
        InitialiseDB test = new InitialiseDB();
        boolean output = false;
        try {
            String path = "jdbc:sqlite:CS1003_P3DataBase";
            Connection connection = DriverManager.getConnection(path);
            output = test.checkInitialized(connection);
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Database has been properly initialized: " + output);
    }

    /**
     * This method creates a new PopulateDB object and creates a connection to the
     * database. The test object is called to insert an author with a random set
     * of variables. Then using the connection to the database all of the author's
     * information is pulled.
     */
    public void loadAuthors() {
        System.out.println("Loading the database with a new author...");
        PopulateDB test = new PopulateDB();
        String Name = "Michael John";
        int NumOfPubl = 23;
        System.out.println("Creating a new author with name: " + Name + ", and " + NumOfPubl + " publications...");
        try {
            String path = "jdbc:sqlite:CS1003_P3DataBase";
            Connection connection = DriverManager.getConnection(path);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Name FROM Authors WHERE Name = '" + Name + "';");
            if (resultSet.getString(1) == null) {
                test.insertIntoDBAuth(Name, NumOfPubl);
            }
            resultSet.close();
            ResultSet set = statement.executeQuery("SELECT Authors.Name, Authors.NumOfPubl FROM Authors WHERE Name = '" + Name + "'");
            set.next();
            System.out.println("Pulling user from database: ");
            System.out.println(set.getString(1) + "|" + set.getString(2));
            set.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new QueryDB object and uses it to call each individual query.
     * Their responses are printed to the terminal in a neat format.
     */
    public void printAllQuery() {
        QueryDB test = new QueryDB();
        try {
            String path = "jdbc:sqlite:CS1003_P3DataBase";
            Connection connection = DriverManager.getConnection(path);
            System.out.println("Query 1:");
            test.Query1(connection);
            System.out.println("\nQuery 2:");
            test.Query2(connection);
            System.out.println("\nQuery 3:");
            test.Query3(connection);
            System.out.println("\nQuery 4:");
            test.Query4(connection);
            System.out.println("\nQuery 5:");
            test.Query5(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
