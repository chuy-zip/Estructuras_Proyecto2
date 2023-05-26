import java.util.Objects;

public class Game {
    private String gameName;
    private String description;
    private String duration;
    private String category1;
    private String category2;
    private String category3;
    private boolean isOnNintendo;
    private boolean isOnPC;
    private boolean isOnMobile;
    private boolean isOnXbox;
    private boolean isOnPlayStation;
    private boolean isMultiplayer;
    private String esrbRating;

    public Game(String gameName, String description, String duration,
                String category1, String category2, String category3,
                boolean isOnNintendo, boolean isOnPC, boolean isOnMobile,
                boolean isOnXbox, boolean isOnPlayStation, boolean isMultiplayer,
                String esrbRating) {
        this.gameName = gameName;
        this.description = description;
        this.duration = duration;
        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
        this.isOnNintendo = isOnNintendo;
        this.isOnPC = isOnPC;
        this.isOnMobile = isOnMobile;
        this.isOnXbox = isOnXbox;
        this.isOnPlayStation = isOnPlayStation;
        this.isMultiplayer = isMultiplayer;
        this.esrbRating = esrbRating;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public String getCategory2() {
        return category2;
    }

    public void setCategory2(String category2) {
        this.category2 = category2;
    }

    public String getCategory3() {
        return category3;
    }

    public void setCategory3(String category3) {
        this.category3 = category3;
    }

    public boolean isOnNintendo() {
        return isOnNintendo;
    }

    public void setOnNintendo(boolean onNintendo) {
        isOnNintendo = onNintendo;
    }

    public boolean isOnPC() {
        return isOnPC;
    }

    public void setOnPC(boolean onPC) {
        isOnPC = onPC;
    }

    public boolean isOnMobile() {
        return isOnMobile;
    }

    public void setOnMobile(boolean onMobile) {
        isOnMobile = onMobile;
    }

    public boolean isOnXbox() {
        return isOnXbox;
    }

    public void setOnXbox(boolean onXbox) {
        isOnXbox = onXbox;
    }

    public boolean isOnPlayStation() {
        return isOnPlayStation;
    }

    public void setOnPlayStation(boolean onPlayStation) {
        isOnPlayStation = onPlayStation;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public void setMultiplayer(boolean multiplayer) {
        isMultiplayer = multiplayer;
    }

    public String getEsrbRating() {
        return esrbRating;
    }

    public void setEsrbRating(String esrbRating) {
        this.esrbRating = esrbRating;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Game otherGame = (Game) obj;
        return Objects.equals(gameName, otherGame.gameName);
    }

}
