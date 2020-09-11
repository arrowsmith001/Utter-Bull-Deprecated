package com.arrowsmith.llv1.classes;

public class Card {

    private String playerName;
    private boolean truth;

    public Card(String playerName, boolean truth) {
        this.playerName = playerName;
        this.truth = truth;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isTruth() {
        return truth;
    }

    public void setTruth(boolean truth) {
        this.truth = truth;
    }
}
