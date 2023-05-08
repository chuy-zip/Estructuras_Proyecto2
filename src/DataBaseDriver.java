import org.neo4j.driver.*;

import java.util.logging.Logger;

public class DataBaseDriver implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(DataBaseDriver.class.getName());
    private final Driver driver;

    public DataBaseDriver(String uri, String user, String password, Config config) {
        // The driver is a long living object and should be opened during the start of your application
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
    }

    @Override
    public void close() {
        // The driver object should be closed before the application ends.
        driver.close();
    }

    public void createGame(String name, String hours, String description) {
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            Transaction tx = session.beginTransaction();
            String query = "CREATE (n:Game {Name: $name, Hours: $hours, Description: $description})";
            tx.run(query, Values.parameters("name", name, "hours", hours, "description", description));
            tx.commit();
        }
    }

    public void deleteGame(String name) {
        try (Session session = driver.session()) {
            session.writeTransaction(transaction -> {
                String query = "MATCH (n:Game {Name: $name}) DELETE n";
                Result result = transaction.run(query, Values.parameters("name", name));
                return null;
            });
            LOGGER.info("Game node with name " + name + " deleted successfully.");
        } catch (Exception e) {
            LOGGER.severe("Error deleting game node: " + e.getMessage());
        }
    }
}