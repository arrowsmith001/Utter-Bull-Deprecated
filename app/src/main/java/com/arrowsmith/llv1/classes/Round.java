package com.arrowsmith.llv1.classes;

import java.util.ArrayList;
import java.util.HashMap;

public class Round {

    private int roundNumber;
    private boolean trueOrBull;
    private String content;
    private String playerName;

    ArrayList<PlayerAndScore> playersAndScoresheet;

    public Round(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Round(int roundNumber, boolean trueOrBull) {
        this.roundNumber = roundNumber;
        this.trueOrBull = trueOrBull;
    }

    public Round(int roundNumber, boolean trueOrBull, String content) {
        this.roundNumber = roundNumber;
        this.trueOrBull = trueOrBull;
        this.content = content;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public boolean isTrueOrBull() {
        return trueOrBull;
    }

    public void setTrueOrBull(boolean trueOrBull) {
        this.trueOrBull = trueOrBull;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<PlayerAndScore> getPlayersAndScoresheet() {
        return playersAndScoresheet;
    }

    public void setPlayersAndScoresheet(ArrayList<PlayerAndScore> playersAndScoresheet) {
        this.playersAndScoresheet = playersAndScoresheet;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
