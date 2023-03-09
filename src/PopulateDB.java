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
import java.util.HashSet;

public class PopulateDB {
    private final String cachePath = "../cache";
    String url = "https://dblp.org/search/author/api?format=xml&c=0&h=40&q=";
    String encodedURL = "";
    HashSet<String> venues = new HashSet<>();

    public static void main(String[] args) {
        PopulateDB queryDBLP = new PopulateDB();
        queryDBLP.searchAuthor("Alan Dearle");
        queryDBLP.searchAuthor("Ian Gent");
        queryDBLP.searchAuthor("Ozgur Akgun");
    }

    public void searchAuthor(String authorName) {
        this.url += authorName.replace(" ", "+");
        this.encodedURL = URLEncoder.encode(this.url, StandardCharsets.UTF_8);
        if (!checkDirectory()) {
            System.out.println("Cache directory doesn't exist: " + this.cachePath);
            System.exit(1);
        }
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
            callToAuthorAPI(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.url = "https://dblp.org/search/author/api?format=xml&c=0&h=40&q=";
    }

    public boolean checkDirectory() {
        File directory = new File(this.cachePath);
        return directory.isDirectory();
    }

    public boolean checkCache() {
        String path = this.cachePath + "/" + this.encodedURL;
        File file = new File(path);
        return file.exists();
    }

    public void callToAuthorAPI(Document document) {
        try {
            String author = "";
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("hit");
            Node authorName = nodeList.item(0);
            if (authorName.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) authorName;
                author = element.getElementsByTagName("author").item(0).getTextContent();
                URL newURL = new URL(element.getElementsByTagName("url").item(0).getTextContent() + ".xml");
                callToPubl(newURL, author);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callToPubl(URL url, String name) {
        String venueURL = "https://dblp.org/";
        String VenID = null;
        try {
            int publications = 0;
            String title = null;
            int NumOfAuth = 0;
            String year = null;
            String PublID = null;
            String newEncodedURL = URLEncoder.encode(String.valueOf(url), StandardCharsets.UTF_8);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            if (checkCache()) {
                File file = new File(this.cachePath + "/" + newEncodedURL);
                document = builder.parse(file);
            } else {
                FileOutputStream outputStream = new FileOutputStream(this.cachePath + "/" + newEncodedURL);
                document = builder.parse(url.openStream());
                writeXMLtoCache(document, outputStream);
            }
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("inproceedings");
            System.out.println(nodeList.getLength());
            NodeList nodeList1 = document.getElementsByTagName("article");
            System.out.println(nodeList1.getLength());
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
                insertIntoDBPubl(title, NumOfAuth, year, PublID, VenID);
            }
            for (int i = 0; i < nodeList1.getLength(); i++) {
                Node publication = nodeList1.item(i);
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
                insertIntoDBPubl(title, NumOfAuth, year, PublID, VenID);
            }
            insertIntoDBAuth(name, publications);
            insertIntoDBOwner(name, PublID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String callToVenue(URL url) {
        try {
            String title = null;
            String newEncodedURL = URLEncoder.encode(String.valueOf(url), StandardCharsets.UTF_8);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;
            if (checkCache()) {
                File file = new File(this.cachePath + "/" + newEncodedURL);
                document = builder.parse(file);
            } else {
                FileOutputStream outputStream = new FileOutputStream(this.cachePath + "/" + newEncodedURL);
                document = builder.parse(url.openStream());
                writeXMLtoCache(document, outputStream);
            }
            document.getDocumentElement().normalize();
            title = document.getElementsByTagName("h1").item(0).getTextContent();
            insertIntoDBVenue(title);
            return title;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertIntoDBAuth(String name, int NumOfPubl) {
        File file = new File("CS1003_P3DataBase");
        Connection connection = null;
        try {
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
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
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void insertIntoDBVenue(String name) {
        File file = new File("CS1003_P3DataBase");
        Connection connection = null;
        try {
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
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
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void insertIntoDBPubl(String title, int NumOfAuth, String YearOfOcc, String PublID, String VenID) {
        File file = new File("CS1003_P3DataBase");
        Connection connection = null;
        try {
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Title FROM Publications WHERE PublID = '" + PublID + "';");
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
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void insertIntoDBOwner(String AuthorID, String PublID) {
        File file = new File("CS1003_P3DataBase");
        Connection connection = null;
        try {
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
            Statement statement = connection.createStatement();
            PreparedStatement stat = connection.prepareStatement("INSERT INTO AuthorOwner VALUES(?, ?)");
            stat.setString(1, AuthorID);
            stat.setString(2, PublID);
            stat.executeUpdate();
            statement.close();
        } catch (SQLException e) {
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
