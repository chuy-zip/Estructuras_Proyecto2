import org.neo4j.driver.Config;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.Iterator;

public class DataBaseController {

    private String URI= System.getenv("NEO4J_URI");
    private String USER = System.getenv("NEO4J_USERNAME");
    private String PASSWORD = System.getenv("NEO4J_PASSWORD");

    private User currentUser ;
    private ArrayList<Game> filteredGames = new ArrayList<>();
    private ArrayList<Game> recommendedgames = new ArrayList<>();

    public boolean accountExists(String name, String password){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(!app.userExists(name, password)){
                return true;
            }
        }
        return false;
    }

    public void validLogin(String name, String password){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(accountExists(name, password)){
                setCurrentUserFromDataBase(name, password);
                setFilteredGamesFromDataBase();
                filteredGames = getFilteredGamesByESRB(filteredGames, currentUser);
            }
        }
    }

    public void setCurrentUserFromDataBase(String name, String password){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(app.userExists(name, password)){
                currentUser = app.mapUser(app.findUserNode(name, password));
            }
        }
    }

    public void setFilteredGamesFromDataBase(){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(currentUser != null){
                filteredGames = app.getCompatibleGames(currentUser);
            }
        }
    }

    public void validSignIn(String name, String password, int age, boolean preferNintendo, boolean preferPC,
                           boolean preferMobile, boolean preferXbox, boolean preferPlayStation, boolean preferMulti){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(!accountExists(name, password)){
                app.crearNodoPersona(name, age, password, preferNintendo, preferPC, preferMobile, preferXbox, preferPlayStation, preferMulti);
                validLogin(name, password);
            }
        }
    }

    public ArrayList<Game> getFilteredGamesByESRB(ArrayList<Game> games, User user) {
        int userAge = user.getUserAge();
        ArrayList<Game> permittedGames = new ArrayList<>();

        for (Game currentGame : games) {
            String gameRating = currentGame.getEsrbRating();

            if (userAge < 17 && userAge >= 13) {
                if (!gameRating.equals("M")) {
                    permittedGames.add(currentGame);
                }
            } else if (userAge < 13 && userAge >= 10) {
                if (!gameRating.equals("M") && !gameRating.equals("T")) {
                    permittedGames.add(currentGame);
                }
            } else if (userAge < 10) {
                if (!gameRating.equals("M") && !gameRating.equals("T") && !gameRating.equals("E10+")) {
                    permittedGames.add(currentGame);
                }
            } else {
                // For users 17 and above, all games are permitted
                permittedGames.add(currentGame);
            }
        }

        return permittedGames;
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
