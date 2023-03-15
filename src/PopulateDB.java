import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class PopulateDB {
    private final String cachePath = "../cache";
    String url = "https://dblp.org/search/author/api?format=xml&c=0&h=40&q=";
    String encodedURL = "";

    /**
     * Main method in PopulateDB which creates a new PopulateDB object. Using that object, searchAuthor, is
     * called three times with the specification required author searches.
     * @param args - command line arguments for the method. They are never used or needed for program to function.
     */
    public static void main(String[] args) {
        PopulateDB queryDBLP = new PopulateDB();
        queryDBLP.searchAuthor("Alan Dearle");
        queryDBLP.searchAuthor("Ian Gent");
        queryDBLP.searchAuthor("Ozgur Akgun");
    }

    /**
     * THIS METHOD WAS TAKEN FROM MY SUBMISSION OF CS1003P2
     * searchAuthor is the method that sets the appropriate url, checks the cache directory, creates a document
     * builder, decides if the cache contains a search inquiry, and if not calls to the author API
     * search method. If the cache directory does not exist an error message is printed and the program terminates.
     * A try-catch loop creates a Document Build Factory and Builder for reading the xml file. A check then sees
     * if the cache directory contains the search inquiry. If it does, the document is parsed the cached file. If not,
     * the document is parsed an url link to the api and a call to writeXMLtoCache creates an instance of the data in
     * cache.
     * @param authorName - String value containing the name of the author to insert into database.
     */
    public void searchAuthor(String authorName) {
        this.url += authorName.replace(" ", "+");
        this.encodedURL = URLEncoder.encode(this.url, StandardCharsets.UTF_8);
        if (!checkDirectory()) {
            System.out.println("Cache directory doesn't exist: " + this.cachePath);
            System.exit(1);
        }
        Document document = checkDocument();
        if (document != null) {
            callToAuthorAPI(document);
        }
        this.url = "https://dblp.org/search/author/api?format=xml&c=0&h=40&q=";
    }

    /**
     * checkDocument is a new method created in this practical that uses code from my CS1003P2
     * submission. This method uses a Document builder factory and builder to create a document.
     * If the file is in the cache, the document is parsed the saved xml file. Otherwise, the file
     * is pulled API.
     * @return Document - A Document object is returned. Either it contains a xml file or is null if it could not be parsed.
     */
    public Document checkDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            if (checkCache()) {
                File file = new File(this.cachePath + "/" + this.encodedURL);
                document = builder.parse(file);
            } else {
                FileOutputStream outputStream = new FileOutputStream(this.cachePath + "/" + this.encodedURL);
                document = builder.parse(new URL(this.url).openStream());
                writeXMLtoCache(document, outputStream);
            }
            return document;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * THIS METHOD WAS TAKEN FROM MY SUBMISSION OF CS1003P2
     * This method checks that the provided cache directory exists and is a directory.
     * @return boolean - returns a boolean value, true if the directory exists, false otherwise
     */
    public boolean checkDirectory() {
        File directory = new File(this.cachePath);
        return directory.isDirectory();
    }

    /**
     * THIS METHOD WAS TAKEN FROM MY SUBMISSION OF CS1003P2
     * This method checks if the current search query has already been called. In other terms,
     * this method ensures that the cache directory contains a file titled the encoded url.
     * @return boolean - returns a boolean value, true if the file exists in the cache, false otherwise
     */
    public boolean checkCache() {
        String path = this.cachePath + "/" + this.encodedURL;
        File file = new File(path);
        return file.exists();
    }

    /**
     * THIS METHOD WAS TAKEN FROM MY SUBMISSION OF CS1003P2 AND MODIFIED
     * Instances of looking at an author xml file. This method retrieves the data for an author search.
     * A try-catch loop exists as a new URL is created later on. The document is normalized and a list of nodes
     * pertaining to "hit" is found. For every hit, the item is grabbed and checked to see if it is an element
     * node. Then it is cast to an element and the author name is retrieved and saved. A newURL is created with
     * the element url under the author and sent to callToPubl() with the author's name.
     * @param document - the xml document given by search for data retrieval
     */
    public void callToAuthorAPI(Document document) {
        try {
            String author = "";
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("hit");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node authorName = nodeList.item(i);
                if (authorName.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) authorName;
                    author = element.getElementsByTagName("author").item(0).getTextContent();
                    URL newURL = new URL(element.getElementsByTagName("url").item(0).getTextContent() + ".xml");
                    callToPubl(newURL, author);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * THIS METHOD WAS TAKEN FROM MY SUBMISSION OF CS1003P2 AND MODIFIED
     * Another instance of looking at a xml file. This particular method retrieves publication data. The document
     * is normalized, and then two list of nodes are created from the tag name "inproceedings" or "article". Using
     * two for loop, every hit checks if the node is an element. Then the node is cast to an element and the number
     * of authors, the title of the publication and year of occurrence is retrieved. These are saved and then passed
     * to insertIntoDBPubl. The name of the author which is given as a parameter to this method is passed along with
     * the PublID to insertIntoDBOwner. A new URL is created with the element url under the author and sent to
     * callToVenue() which returns the VenID. Finally, at the very end, the author name and total number of publications
     * is passed to insertIntoDBAuth().
     * @param url - the url for the publication information
     * @param name - String value containing the name of the Author
     */
    public void callToPubl(URL url, String name) {
        String venueURL = "https://dblp.org/";
        String VenID = null;
        try {
            int publications = 0;
            String title = null;
            int NumOfAuth = 0;
            String year = null;
            String PublID = null;
            this.encodedURL = URLEncoder.encode(String.valueOf(url), StandardCharsets.UTF_8);
            Document document = checkDocument();
            if (document == null) {
                throw new Exception("Document is not valid!");
            }
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("inproceedings");
            NodeList nodeList1 = document.getElementsByTagName("article");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node publication = nodeList.item(i);
                PublID = publication.getAttributes().getNamedItem("key").getTextContent();
                if (publication.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) publication;
                    publications += 1;
                    title = element.getElementsByTagName("title").item(0).getTextContent();
                    NumOfAuth = element.getElementsByTagName("author").getLength();
                    year = element.getElementsByTagName("year").item(0).getTextContent();
                    String nextURL = element.getElementsByTagName("url").item(0).getTextContent();
                    nextURL = nextURL.substring(0, nextURL.indexOf(".html"));
                    nextURL = venueURL + nextURL + ".xml";
                    URL theNextURL = new URL(nextURL);
                    VenID = callToVenue(theNextURL);
                }
                insertIntoDBOwner(name, PublID);
                insertIntoDBPubl(title, NumOfAuth, year, PublID, VenID);
            }
            for (int i = 0; i < nodeList1.getLength(); i++) {
                Node publication = nodeList1.item(i);
                String[] PublicID = publication.getAttributes().getNamedItem("key").getTextContent().split("\\/");
                PublID = PublicID[PublicID.length - 1];
                if (publication.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) publication;
                    publications += 1;
                    title = element.getElementsByTagName("title").item(0).getTextContent().replace("\n", " ");
                    NumOfAuth = element.getElementsByTagName("author").getLength();
                    year = element.getElementsByTagName("year").item(0).getTextContent();
                    String nextURL = element.getElementsByTagName("url").item(0).getTextContent();
                    nextURL = nextURL.substring(0, nextURL.indexOf(".html"));
                    nextURL = venueURL + nextURL + ".xml";
                    URL theNextURL = new URL(nextURL);
                    VenID = callToVenue(theNextURL);
                }
                insertIntoDBOwner(name, PublID);
                insertIntoDBPubl(title, NumOfAuth, year, PublID, VenID);
            }
            insertIntoDBAuth(name, publications);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * THIS METHOD WAS TAKEN FROM MY SUBMISSION OF CS1003P2 AND MODIFIED
     * The final instance of looking at a xml file. For finding the venue information, the document is first
     * normalized. Then the title of the venue is stored by getting the element from tag name "h1". This name
     * replaces all new line characters with a space to improve format. This value is then passed to
     * insertIntoDBVenue. Finally, the title is returned so that it can be given to the publications.
     * @param url - the url for the venue information
     * @return String - A string value containing the title of the venue
     */
    public String callToVenue(URL url) {
        try {
            String title = null;
            this.encodedURL = URLEncoder.encode(String.valueOf(url), StandardCharsets.UTF_8);
            Document document = checkDocument();
            if (document == null) {
                throw new Exception("Document is not valid!");
            }
            document.getDocumentElement().normalize();
            title = document.getElementsByTagName("h1").item(0).getTextContent().replace("\n", " ");
            insertIntoDBVenue(title);
            return title;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * insertIntoDBAuth takes two parameters and creates a connection to the database. A sql statement
     * with a hard coded command is filled with the name and number of publications. This statement
     * is executed and finally the connection is closed.
     * @param name - String value containing the name of the author
     * @param NumOfPubl - integer value containing the number of publications an author worked on
     */
    public void insertIntoDBAuth(String name, int NumOfPubl) {
        Connection connection = createConnection();
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO Authors VALUES ('" + name + "','" + NumOfPubl + "');");
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * insertIntoDBVenue takes one parameter and creates a connection to the database. A check to ensure
     * that the venue does not already exist in the database is executed and if the result is null then
     * a sql statement inserts the name into the database. This statement is executed and finally the
     * connection is closed.
     * @param name - String value containing the name of the venue
     */
    public void insertIntoDBVenue(String name) {
        Connection connection = createConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Name FROM Venues WHERE Name = '" + name.replaceAll("'", "''") + "';");
            if (resultSet.getString(1) == null) {
                PreparedStatement stat = connection.prepareStatement("INSERT INTO Venues VALUES (?)");
                stat.setString(1, name);
                stat.executeUpdate();
                statement.close();
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * insertIntoDBPubl takes 5 parameters and creates a connection to the database. A check to ensure
     * that the publication does not already exist in the database is executed and if the result is null then
     * a sql statement inserts the values into the database. This statement is executed and finally the
     * connection is closed.
     * @param title - String value containing the title of the venue
     * @param NumOfAuth - integer value containing the number of authors that worked on this publication
     * @param YearOfOcc - String value containing the year published
     * @param PublID - String value containing the publication id
     * @param VenID - String value containing the venue id
     */
    public void insertIntoDBPubl(String title, int NumOfAuth, String YearOfOcc, String PublID, String VenID) {
        Connection connection = createConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Title FROM Publications WHERE PublID = '" + PublID + "'");
            if (resultSet.getString(1) == null) {
                PreparedStatement stat = connection.prepareStatement("INSERT INTO Publications VALUES(?, ?, ?, ?, ?)");
                stat.setString(1, title);
                stat.setInt(2, NumOfAuth);
                stat.setString(3, YearOfOcc);
                stat.setString(4, PublID);
                stat.setString(5, VenID);
                stat.executeUpdate();
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * insertIntoDBOwner takes two parameters and creates a connection to the database. A sql statement
     * with a hard coded command is filled with the name and the publication's id. This statement
     * is executed and finally the connection is closed.
     * @param AuthorID - String value containing the author id
     * @param PublID - String value containing the publication id
     */
    public void insertIntoDBOwner(String AuthorID, String PublID) {
        Connection connection = createConnection();
        try {
            PreparedStatement stat = connection.prepareStatement("INSERT INTO AuthorOwner VALUES(?, ?)");
            stat.setString(1, AuthorID);
            stat.setString(2, PublID);
            stat.executeUpdate();
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * createConnection() creates a connection variable that is linked to the
     * database. This value is returned for use of insertion.
     * @return Connection - a connection object with a link to the database
     */
    public Connection createConnection() {
        File file = new File("CS1003_P3DataBase");
        Connection connection = null;
        try {
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * THIS METHOD WAS TAKEN FROM MY SUBMISSION OF CS1003P2
     * Write XML to cache has a try-catch loop to check transformer issues. A new TransformerFactory,
     * Transformer, DOMSource, and StreamResult are created with the source xml and future cache location.
     * The transformer puts the xml data into the new cache file for future reference.
     * @param document - the xml document given by search for reading
     * @param output - the output stream which contains the location of where to put the new cached file
     */
    public void writeXMLtoCache(Document document, OutputStream output) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(output);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            System.out.println(e.getMessage());
        }
    }

}
