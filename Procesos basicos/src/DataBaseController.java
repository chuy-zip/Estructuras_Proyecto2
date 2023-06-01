import org.neo4j.driver.Config;
import org.neo4j.driver.types.Node;

import java.lang.reflect.Array;
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

    // Getters for user-related data
    // This will only have data after the user has logged in or has previously selected an action
    // Such as get recommendations for example
    public ArrayList<Game> getUserPlayedGames(){
        return currentUser.getPlayedGames();
    }

    public ArrayList<Game> getUserFavoriteGames(){
        return currentUser.getFavoriteGames();
    }

    public ArrayList<Game> getFilteredGames() {
        return filteredGames;
    }

    public ArrayList<Game> getRecommendedgames() {
        return recommendedgames;
    }

    // Check if an account exists in the database
    public boolean accountExists(String name, String password){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(!app.userExists(name, password)){
                return true;
            }
        }
        return false;
    }

    // Validate login credentials and set the current user and filtered games
    // Once the user has logged in, the program automatically gets the user from the database and filters
    // The games according to the user preferences and age
    public void validLogin(String name, String password){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(accountExists(name, password)){
                setCurrentUserFromDataBase(name, password);
                setFilteredGamesFromDataBase();
                filteredGames = getFilteredGamesByESRB(filteredGames, currentUser);
            }
        }
    }

    // Set the current user based on the provided name and password
    public void setCurrentUserFromDataBase(String name, String password){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(app.userExists(name, password)){
                currentUser = app.mapUser(app.findUserNode(name, password));
            }
        }
    }

    // Set the filtered games based on the current user
    public void setFilteredGamesFromDataBase(){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(currentUser != null){
                filteredGames = app.getCompatibleGames(currentUser);
            }
        }
    }

    // Validate sign-in credentials, create a new account, and set the current user and filtered games
    // If the user does not exist a new user is created and its favorite games are saved
    public void validSignIn(String name, String password, int age, boolean preferNintendo, boolean preferPC,
                            boolean preferMobile, boolean preferXbox, boolean preferPlayStation, boolean preferMulti, ArrayList<String> playedGamesNames){
        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            if(!accountExists(name, password)){
                app.crearNodoPersona(name, age, password, preferNintendo, preferPC, preferMobile, preferXbox, preferPlayStation, preferMulti);
                for (String gameName: playedGamesNames){
                    app.crearRelacionPersonaJuego(name,gameName);
                }
                validLogin(name, password);
            }
        }
    }

    // Filter games based on ESRB rating and user's age
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

    // Filter games based on categories
    //When a user tries to do a filtered search by selecting categories
    public ArrayList<Game> getGamesFilteredByCategory(ArrayList<String> Categories){
        ArrayList<Game> newFilteredGames = new ArrayList<>();

        for (Game game: filteredGames){
            if(Categories.contains(game.getCategory1()) || Categories.contains(game.getCategory2()) || Categories.contains(game.getCategory3())){
                filteredGames.add(game);
            }
        }
        return newFilteredGames;
    }

    // Recommend games based on user's played and favorite games
    // The games are stored in the variable of the class recommendedgames
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
                recommendedgames.add(recommendedGame);
                // Set the similarity score to -1 to avoid selecting it again
                Arrays.fill(similarityMatrix[maxSimilarityIndex], -1);
            }
        }

        return recommendedgames;
    }

    // Calculate similarity between two games based on certain criteria
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

    public ArrayList<String> getAllExistingCategories(){
        ArrayList<String> allCategories = new ArrayList<>();

        try (var app = new DataBaseDriver(URI, USER, PASSWORD, Config.defaultConfig())) {
            ArrayList<Game> allGames = app.getAllGames();

            for ( Game game : allGames){
                if (!allCategories.contains(game.getCategory1())){
                    allCategories.add(game.getCategory1());
                }
                if (!allCategories.contains(game.getCategory2())){
                    allCategories.add(game.getCategory2());
                }
                if (!allCategories.contains(game.getCategory3())){
                    allCategories.add(game.getCategory3());
                }
            }
        }
        return allCategories;
    }
}
