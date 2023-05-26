import org.neo4j.driver.Config;
import org.neo4j.driver.types.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class DataBaseController {

    private String URI= System.getenv("NEO4J_URI");
    private String USER = System.getenv("NEO4J_USERNAME");
    private String PASSWORD = System.getenv("NEO4J_PASSWORD");
    private User currentUser;
    private ArrayList<Game> filteredGames = new ArrayList<>();
    private ArrayList<Game> recommendedgames = new ArrayList<>();

    public ArrayList<Game> getUserPlayedGames(){
        return currentUser.getPlayedGames();
    }

    public ArrayList<Game> getUserFavoriteGames(){
        return currentUser.getFavoriteGames();
    }
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
    public ArrayList<Game> getGamesFilteredByCategory(ArrayList<String> Categories){
        ArrayList<Game> newFilteredGames = new ArrayList<>();

        for (Game game: filteredGames){
            if(Categories.contains(game.getCategory1()) || Categories.contains(game.getCategory2()) || Categories.contains(game.getCategory3())){
                filteredGames.add(game);
            }

        }
        return newFilteredGames;
    }

    public ArrayList<Game> recommendGames() {
        ArrayList<Game> playedGames = currentUser.getPlayedGames();
        ArrayList<Game> favoriteGames = currentUser.getFavoriteGames();
        ArrayList<Game> allGames = filteredGames;

        // Add favorite games to the played games list
        for (Game game : favoriteGames) {
            if (!playedGames.contains(game)) {
                playedGames.add(game);
            }
        }

        int[][] similarityMatrix = new int[playedGames.size()][favoriteGames.size()];

        // Calculate similarity scores and populate the similarity matrix
        for (int i = 0; i < playedGames.size(); i++) {
            Game playedGame = playedGames.get(i);

            for (int j = 0; j < favoriteGames.size(); j++) {
                Game favoriteGame = favoriteGames.get(j);

                // Calculate similarity score for each game pair
                int similarityScore = calculateSimilarity(playedGame, favoriteGame);

                // Assign similarity score to the matrix
                similarityMatrix[i][j] = similarityScore;
            }
        }

        ArrayList<Game> recommendedGames = new ArrayList<>();
        int numRecommendations = Math.min(5, allGames.size()); // Get up to 5 recommendations

        // Generate recommendations based on the similarity matrix
        for (int k = 0; k < numRecommendations; k++) {
            int maxSimilarity = Integer.MIN_VALUE;
            int maxSimilarityIndex = -1;

            for (int i = 0; i < playedGames.size(); i++) {
                for (int j = 0; j < favoriteGames.size(); j++) {
                    int similarity = similarityMatrix[i][j];
                    if (similarity > maxSimilarity) {
                        maxSimilarity = similarity;
                        maxSimilarityIndex = j;
                    }
                }
            }

            if (maxSimilarityIndex != -1) {
                Game recommendedGame = allGames.get(maxSimilarityIndex);
                recommendedGames.add(recommendedGame);
                // Set the similarity score to -1 to avoid selecting it again
                Arrays.fill(similarityMatrix[maxSimilarityIndex], -1);
            }
        }

        return recommendedGames;
    }
    private int calculateSimilarity(Game playedGame, Game game) {
        int similarity = 0;

        if (playedGame.getDuration().equals(game.getDuration())) {
            similarity += 5;
        }
        if (playedGame.getCategory1().equals(game.getCategory1())
                || playedGame.getCategory1().equals(game.getCategory2())
                || playedGame.getCategory1().equals(game.getCategory3())) {
            similarity += 10;
        }
        if (playedGame.getCategory2().equals(game.getCategory1())
                || playedGame.getCategory2().equals(game.getCategory2())
                || playedGame.getCategory2().equals(game.getCategory3())) {
            similarity += 10;
        }
        if (playedGame.getCategory3().equals(game.getCategory1())
                || playedGame.getCategory3().equals(game.getCategory2())
                || playedGame.getCategory3().equals(game.getCategory3())) {
            similarity += 10;
        }

        return similarity;
    }

    public static void main(String[] args) {
        String URI= System.getenv("NEO4J_URI");
        String USER = System.getenv("NEO4J_USERNAME");
        String PASSWORD = System.getenv("NEO4J_PASSWORD");

        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            System.out.println("Connected");
            Game nGame = app.mapGame(app.getGameNodeByName("Mario"));

            User nUser = app.mapUser(app.findUserNode("Chuy","Chuy123"));

            System.out.println("Found user: " + nUser.getUserName());
            System.out.println("Age: " + nUser.getUserAge());
            ArrayList<Game> filteredGames = app.getCompatibleGames(nUser);

            for (Game game: filteredGames) {
                System.out.println("The name of the played game: " + game.getGameName());
            }
            System.out.println("\nGetting game of name: " + nGame.getGameName() + " |The second category of the game is: " + nGame.getCategory2());
            System.out.println("Primer jugado persona: " + nUser.getPlayedGames().get(2).getGameName());
            System.out.println("Fav:" + nUser.getFavoriteGames().get(0).getGameName());
        }
    }
}
