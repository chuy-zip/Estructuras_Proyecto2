import org.neo4j.driver.Config;
import org.neo4j.driver.types.Node;

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
            Node node = app.getGameNodeByName("Mario");

            Game nGame = app.mapGame(node);
            System.out.println("This game: " + nGame.getGameName() + "This var: " + nGame.getCategory2());

        }


    }
}
