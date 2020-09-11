package com.arrowsmith.llv1.classes;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class RoundData {

    private final Player me;
    private final ArrayList<Player> playersWhovePlayed;
    private final HashMap<String, Boolean> playersTruth;
    private final HashMap<String, ArrayList<String>> playersVotes;
    private final HashMap<String, ArrayList<Integer>> playersTimes;
    private final HashMap<String, Integer> playersScores;
    private final HashMap<String, String> playersText;

    public RoundData(Player me, // Player representing me, the player
                     ArrayList<Player> playersWhovePlayed,  // List of players in the order they played
                     HashMap<String, Boolean> playersTruth, // Final player to truth hashmap
                     HashMap<String, ArrayList<String>> playersVotes, // Final player votes
                     HashMap<String, ArrayList<Integer>> playersTimes, // Final player vote times
                     HashMap<String, Integer> playersScores, HashMap<String, String> playersText) // Final player scores
    {
        Log.i(TAG, "RoundData: CALLED");

        this.me = me;
        this.playersWhovePlayed = playersWhovePlayed;
        this.playersTruth = playersTruth;
        this.playersVotes = playersVotes;
        this.playersTimes = playersTimes;
        this.playersScores = playersScores;
        this.playersText = playersText;
    }

    public Player getMe() {
        return me;
    }

    public ArrayList<Player> getPlayersWhovePlayed() {
        return playersWhovePlayed;
    }

    public HashMap<String, Boolean> getPlayersTruth() {
        return playersTruth;
    }

    public HashMap<String, ArrayList<String>> getPlayersVotes() {
        return playersVotes;
    }

    public HashMap<String, ArrayList<Integer>> getPlayersTimes() {
        return playersTimes;
    }

    public HashMap<String, Integer> getPlayersScores() {
        return playersScores;
    }

    public HashMap<String, String> getPlayersText() {
        return playersText;
    }
}
