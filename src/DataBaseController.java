import org.neo4j.driver.Config;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;

public class DataBaseController {

    private String URI= System.getenv("NEO4J_URI");
    private String USER = System.getenv("NEO4J_USERNAME");
    private String PASSWORD = System.getenv("NEO4J_PASSWORD");

    private User currentUser ;
    private ArrayList<Game> filteredGames;
    private ArrayList<Game> recommendedgames;

    public void setCurrentUser(){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            System.out.println("Connected");
            currentUser = app.mapUser(app.findUserNode("Chuy","Chuy123"));
        }
    }
    public void setFilteredGames(){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(currentUser != null){
                filteredGames = app.getCompatibleGames(currentUser);
            }
        }
    }
    public static void main(String[] args) {
        String URI= System.getenv("NEO4J_URI");
        String USER = System.getenv("NEO4J_USERNAME");
        String PASSWORD = System.getenv("NEO4J_PASSWORD");

        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            System.out.println("Connected");
            Game nGame = app.mapGame(app.getGameNodeByName("Mario"));

            User nUser = app.mapUser(app.findUserNode("Chuy","Chuy123"));

            ArrayList<Game> filteredGames = app.getCompatibleGames(nUser);

            for (Game game: filteredGames) {
                System.out.println("The name of the game: " + game.getGameName());
            }
            System.out.println("This game: " + nGame.getGameName() + " This var: " + nGame.getCategory2());
            System.out.println("Primer jugado persona: " + nUser.getPlayedGames().get(2).getGameName());
            System.out.println("Fav:" + nUser.getFavoriteGames().get(0).getGameName());
        }
    }
}
