import org.neo4j.driver.Config;

public class DataBaseController {

    String URI= System.getenv("NEO4J_URI");
    String USER = System.getenv("NEO4J_USERNAME");
    String PASSWORD = System.getenv("NEO4J_PASSWORD");

    public static void main(String[] args) {
        String URI= System.getenv("NEO4J_URI");
        String USER = System.getenv("NEO4J_USERNAME");
        String PASSWORD = System.getenv("NEO4J_PASSWORD");

        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            System.out.println("Connected");
            app.deleteFavoriteConnection("Chuy","Ajedrez");

        }


    }
}
