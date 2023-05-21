import java.util.ArrayList;

public class User {
    private String userID;
    private String userName;
    private String userPassword;
    private int userAge;
    private boolean prefersNintendo;
    private boolean prefersPC;
    private boolean prefersMobile;
    private boolean prefersXbox;
    private boolean prefersPlaystation;
    private boolean prefersMultiplayer;
    private ArrayList<Game> playedGames;
    private ArrayList<Game> favoriteGames;

    public User(String userID, String userName, String userPassword, int userAge,
                boolean prefersNintendo, boolean prefersPC, boolean prefersMobile,
                boolean prefersXbox, boolean prefersPlaystation, boolean prefersMultiplayer) {
        this.userID = userID;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userAge = userAge;
        this.prefersNintendo = prefersNintendo;
        this.prefersPC = prefersPC;
        this.prefersMobile = prefersMobile;
        this.prefersXbox = prefersXbox;
        this.prefersPlaystation = prefersPlaystation;
        this.prefersMultiplayer = prefersMultiplayer;
        this.playedGames = new ArrayList<>();
        this.favoriteGames = new ArrayList<>();
    }

    // Getters and Setters

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public boolean isPrefersNintendo() {
        return prefersNintendo;
    }

    public void setPrefersNintendo(boolean prefersNintendo) {
        this.prefersNintendo = prefersNintendo;
    }

    public boolean isPrefersPC() {
        return prefersPC;
    }

    public void setPrefersPC(boolean prefersPC) {
        this.prefersPC = prefersPC;
    }

    public boolean isPrefersMobile() {
        return prefersMobile;
    }

    public void setPrefersMobile(boolean prefersMobile) {
        this.prefersMobile = prefersMobile;
    }

    public boolean isPrefersXbox() {
        return prefersXbox;
    }

    public void setPrefersXbox(boolean prefersXbox) {
        this.prefersXbox = prefersXbox;
    }

    public boolean isPrefersPlaystation() {
        return prefersPlaystation;
    }

    public void setPrefersPlaystation(boolean prefersPlaystation) {
        this.prefersPlaystation = prefersPlaystation;
    }

    public boolean isPrefersMultiplayer() {
        return prefersMultiplayer;
    }

    public void setPrefersMultiplayer(boolean prefersMultiplayer) {
        this.prefersMultiplayer = prefersMultiplayer;
    }

    public ArrayList<Game> getPlayedGames() {
        return playedGames;
    }

    public void setPlayedGames(ArrayList<Game> playedGames) {
        this.playedGames = playedGames;
    }

    public ArrayList<Game> getFavoriteGames() {
        return favoriteGames;
    }

    public void setFavoriteGames(ArrayList<Game> favoriteGames) {
        this.favoriteGames = favoriteGames;
    }
}
