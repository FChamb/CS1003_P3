import java.sql.Connection;
import java.sql.DriverManager;

public class Test {
    public static void main(String[] args) {
        Test test = new Test();
        test.testInitialization();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Database has been properly initialized: " + output);
    }
}
