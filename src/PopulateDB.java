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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class PopulateDB {
    private final String cachePath = "../cache";
    String url = "https://dblp.org/search/author/api?format=xml&c=0&h=40&q=";
    String encodedURL = "";

    public static void main(String[] args) {
        PopulateDB queryDBLP = new PopulateDB();
        queryDBLP.searchAuthor("Alan Dearle");
        //queryDBLP.searchAuthor("Ian Gent");
        //queryDBLP.searchAuthor("Ozgur Akgun");
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
            if (nodeList.getLength() == 0) {
                //System.out.println("This author has 0 publications with 0 co-authors.");
                //System.exit(1);
            }
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

    public void callToPubl(URL url, String name) {
        String venueURL = "https://dblp.org/";
        try {
            int publications = 0;
            String title = null;
            int NumOfAuth = 0;
            String year = null;
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
            NodeList nodeList1 = document.getElementsByTagName("article");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node publication = nodeList.item(i);
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
                    callToVenue(theNextURL);
                }
                //System.out.println("Publications (" + publications + ") + title:" + title + ", numOfAuth:" + NumOfAuth + ", year:" + year);
            }
            for (int i = 0; i < nodeList1.getLength(); i++) {
                Node publication = nodeList1.item(i);
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
                    callToVenue(theNextURL);
                }
                //System.out.println("Publications (" + publications + ") + title:" + title + ", numOfAuth:" + NumOfAuth + ", year:" + year);
            }
            insertIntoDBAuth(name, publications);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callToVenue(URL url) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            int num = statement.executeUpdate("SELECT count(*) FROM Venues WHERE Name = '" + name + "';");
            if (num == 0) {
                statement.executeUpdate("INSERT INTO Venues VALUES ('" + name + "');");
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

    public void insertIntoDBPubl(String title, int NumOfAuth, String YearOfOcc, String AuthorID, String VenID) {
        File file = new File("CS1003_P3DataBase");
        Connection connection = null;
        try {
            String path = "jdbc:sqlite:" + file.getName();
            connection = DriverManager.getConnection(path);
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO Publications VALUES ('" + title + "','" + NumOfAuth + "','" + YearOfOcc + "','" + AuthorID + "','" + VenID + "');");
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
