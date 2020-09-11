package com.arrowsmith.llv1.classes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Room implements Serializable {

    private RoomCode roomCode; // Code for room
    private ArrayList<Player> players; // List of players in this room
    private State state; // Game state
    private GameSettings settings; // Game settings

    // private String refresh;

    // Shallow lists
/*    private HashMap<String,String> playersManifest; // Shallow listing of players
    private HashMap<String,Boolean> playersReadyManifest; // Shallow listing of players for readiness
    private HashMap<String,Integer> playersScoreManifest; // Shallow listing of players for scores*/

    private Manifest<String> playersManifest; // Shallow listing of players
    private Manifest<Boolean> playersReadyManifest; // Shallow listing of players for readiness
    private Manifest<Integer> playersScoreManifest; // Shallow listing of players for scores*/
    private Manifest<String> playersTextManifest; // Shallow listing of players for text
    private Manifest<Boolean> playersTruthManifest; // Shallow listing of players for text
    private Manifest<ArrayList<String>> playersVotesManifest; // Shallow listing of players for scores
    private Manifest<ArrayList<Integer>> playersTimesManifest; // Shallow listing of players for scores
    private String whoseTurn; // Player whose turn it is next

    public Manifest<Boolean> getPlayersTruthManifest() {
        return playersTruthManifest;
    }

    public void setPlayersTruthManifest(Manifest<Boolean> playersTruthManifest) {
        this.playersTruthManifest = playersTruthManifest;
    }

    public Manifest<ArrayList<Integer>> getPlayersTimesManifest() {
        return playersTimesManifest;
    }

    public void setPlayersTimesManifest(Manifest<ArrayList<Integer>> playersTimesManifest) {
        this.playersTimesManifest = playersTimesManifest;
    }

    public Manifest<ArrayList<String>> getPlayersVotesManifest() {
        return playersVotesManifest;
    }

    public void setPlayersVotesManifest(Manifest<ArrayList<String>> playersVotesManifest) {
        this.playersVotesManifest = playersVotesManifest;
    }

    public String getWhoseTurn() {
        return whoseTurn;
    }

    public void setWhoseTurn(String whoseTurn) {
        this.whoseTurn = whoseTurn;
    }

    public Manifest<String> getPlayersTextManifest() {
        return playersTextManifest;
    }

    public void setPlayersTextManifest(Manifest<String> playersTextManifest) {
        this.playersTextManifest = playersTextManifest;
    }

    /**
     * CONSTRUCTOR
     */
    public Room(){}

    /**
     * CONSTRUCTOR
     * @param roomCode
     * @param hostPlayer
     */
    public Room(RoomCode roomCode, Player hostPlayer){

        players = new ArrayList<Player>();

        playersManifest = new Manifest<String>(new HashMap<String, String>());
        /*playersReadyManifest = new Manifest<Boolean>(new HashMap<String, Boolean>());
        playersScoreManifest = new Manifest<Integer>(new HashMap<String, Integer>());*/

        // Add hostPlayer to list
        players.add(hostPlayer); // Does this directly as there can't possibly be a name conflict

        // Add hostPlayer to manifest(s)
        playersManifest.getPlayers().put(hostPlayer.getName(),"");
        /*playersReadyManifest.getPlayers().put(hostPlayer.getName(),hostPlayer.isReady());
        playersScoreManifest.getPlayers().put(hostPlayer.getName(),hostPlayer.getPoints());*/

        // Save a shallow copy of hostPlayer
        // this.roomHost = hostPlayer.getName();

        //this.roomCode = roomCode; // Unnecessary?
        this.state = new State(false);
        this.settings = new GameSettings(); // TODO: Make settings dynamic based on user preferences
    }

    /**
     * ADD PLAYER METHOD, THROWS EXCEPTION
     * @param newPlayer
     * @throws Exception
     */
    public void addPlayer(Player newPlayer) throws Exception {

        boolean conflictFound = false;

        for (Player p : players){
            if (p.getName().equals(newPlayer.getName())) conflictFound = true;
        }

        if(!conflictFound) players.add(newPlayer);
        else throw new Exception("Sorry, there's already someone named "+newPlayer.getName()+" in this room!");

    }


    public void setPlayers(ArrayList<Player> players) {

        this.players = players;

        /*if(playersManifest != null){
            Log.i(TAG, "setPlayers: SUBROUTINE 1 TRIGGERED");
            HashMap<String,String> hashMap1 = new HashMap<>();
            for (Player p: players){
                hashMap1.put(p.getName(),p.getName());
            }
            playersManifest.setPlayers(hashMap1);
        }

        if(playersReadyManifest != null){
            Log.i(TAG, "setPlayers: SUBROUTINE 2 TRIGGERED");
            HashMap<String,Boolean> hashMap2 = new HashMap<>();
            for (Player p: players){
                hashMap2.put(p.getName(),p.isReady());
            }
            playersReadyManifest.setPlayers(hashMap2);
        }

        if(playersScoreManifest != null){
            Log.i(TAG, "setPlayers: SUBROUTINE 3 TRIGGERED");
            HashMap<String,Integer> hashMap3 = new HashMap<>();
            for (Player p: players){
                hashMap3.put(p.getName(),p.getPoints());
            }
            playersScoreManifest.setPlayers(hashMap3);
        }*/

    }

    public Manifest<String> getPlayersManifest() {

        /*HashMap<String,String> hashMap = new HashMap<>();
        for (Player p: players){
            hashMap.put(p.getName(),"");
        }
        playersManifest.setPlayers(hashMap);
*/
        return playersManifest;
    }

    public void setPlayersManifest(Manifest<String> playersManifest) {

        this.playersManifest = playersManifest;
    }

    public Manifest<Boolean> getPlayersReadyManifest() {

       /* HashMap<String,Boolean> hashMap = new HashMap<>();
        for (Player p: players){
            hashMap.put(p.getName(),p.isReady());
        }
        playersReadyManifest.setPlayers(hashMap);
*/
        return playersReadyManifest;
    }

    public void setPlayersReadyManifest(Manifest<Boolean> playersReadyManifest) {
        this.playersReadyManifest = playersReadyManifest;
    }

    public Manifest<Integer> getPlayersScoreManifest() {

        /*HashMap<String,Integer> hashMap = new HashMap<>();
        for (Player p: players){
            hashMap.put(p.getName(),p.getPoints());
        }
        playersScoreManifest.setPlayers(hashMap);*/

        return playersScoreManifest;
    }

    public void setPlayersScoreManifest(Manifest<Integer> playersScoreManifest) {
        this.playersScoreManifest = playersScoreManifest;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    public RoomCode getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(RoomCode roomCode) {
        this.roomCode = roomCode;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    @NonNull
    @Override
    public String toString() {
        return this.roomCode.getCode();
    }
}
