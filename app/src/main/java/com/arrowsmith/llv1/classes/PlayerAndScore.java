package com.arrowsmith.llv1.classes;

public class PlayerAndScore {

    private String playername;
    private String scoreString;

    public PlayerAndScore(String playername, String scoreString) {
        this.playername = playername;
        this.scoreString = scoreString;
    }

    public PlayerAndScore(String playername) {
        this.playername = playername;
    }

    public String getPlayername() {
        return playername;
    }

    public void setPlayername(String playername) {
        this.playername = playername;
    }

    public String getScoreString() {
        return scoreString;
    }

    public void setScoreString(String scoreString) {
        this.scoreString = scoreString;
    }
}
