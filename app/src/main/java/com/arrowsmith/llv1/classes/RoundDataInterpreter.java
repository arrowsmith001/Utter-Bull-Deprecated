package com.arrowsmith.llv1.classes;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class RoundDataInterpreter {
    
    private final RoundData data;
    private Player me;
    private ArrayList<Player> playersWhovePlayed;
    private HashMap<String, ArrayList<Integer>> playersTimes;
    private HashMap<String, Boolean> playersTruth;
    private HashMap<String, ArrayList<String>> playersVotes;
    private HashMap<String, Integer> playersScores;

    Achievement v_correctVoteTrue, v_correctVoteLie, v_minorityVote, v_fastestVote;
    Achievement s_changedToTrue, s_mostVote, s_allVote;
    Achievement p_splitVote, p_mostVote, p_allVote;
    Achievement p_zeroVote;
    
    // Output
    private List<TurnData> turnData;
    private ResultsData resultsData;

    private HashMap<String, String> playersText;

    public RoundDataInterpreter(RoundData data) {
        Log.i(TAG, "RoundDataInterpreter: CALLED");
        this.data = data;
        unpackData(data);
        addAchievements();
    }
    
    private void unpackData(RoundData data) {
        Log.i(TAG, "unpackData: CALLED");
        
        this.me = data.getMe();
        this.playersTimes = data.getPlayersTimes();
        this.playersTruth = data.getPlayersTruth();
        this.playersVotes = data.getPlayersVotes();
        this.playersWhovePlayed = data.getPlayersWhovePlayed();
        this.playersScores = data.getPlayersScores();
        this.playersText = data.getPlayersText();
    }

    public void interpret() {
        Log.i(TAG, "interpret: CALLED");

        turnData = new ArrayList<>();

        for (int i = 0; i < playersWhovePlayed.size(); i++)
        {
            Log.i(TAG, "interpret: PLAYERSWHOVEPLAYED: RUN: "+i);

            Integer turnNumber;
            String name;
            String wasInFact;
            String writtenBy;
            ArrayList<Voter> trueVoters;
            ArrayList<Voter> lieVoters;
            ArrayList<Voter> xVoters;
            ArrayList<String> fastestPlayers;
            HashMap<String,ArrayList<Achievement>> achievementsUnlocked;

            saboName = null;
            s_mostEarned = false;
            s_allEarned = false;

            p_fiftyFiftyEarned = false;
            p_mostEarned = false;
            p_allEarned = false;
            p_nobodyVoted = false;

            turnNumber = i + 1;

            Player playerThisTurn = playersWhovePlayed.get(i);
            name = playerThisTurn.getName();

            wasInFact = (playersTruth.get(name) ? "T" : "L");

            // Get just votes for this round
            HashMap<String, String> votesThisRound = new HashMap<>();
            for (String k : playersVotes.keySet()) {
                votesThisRound.put(k, playersVotes.get(k).get(i));
            }

            // Get just vote times for this round
            HashMap<String, Integer> timesThisRound = new HashMap<>();
            for (String k : playersTimes.keySet()) {

                // If player was legitimate voter
               // if (votesThisRound.get(k).equals("T") || (votesThisRound.get(k).equals("L")))
                    timesThisRound.put(k, playersTimes.get(k).get(i));
            }

            // Determine author
            writtenBy = name;
            for (String k : votesThisRound.keySet()) {
                if (votesThisRound.get(k).equals("s")) {
                    writtenBy = k;
                }
            }

            // Get fastest time & fastest players
            int fastestTime = 0;
            fastestPlayers = new ArrayList<>();

            for (String k : timesThisRound.keySet()) {
                if (timesThisRound.get(k) > fastestTime && votesThisRound.get(k).equals(wasInFact))
                    fastestTime = timesThisRound.get(k);
            }
            for (String k : timesThisRound.keySet()) {
                if (timesThisRound.get(k) == fastestTime && votesThisRound.get(k).equals(wasInFact))
                    fastestPlayers.add(k);
            }

            // Compile voter lists of each kind
            trueVoters = new ArrayList<>();
            lieVoters = new ArrayList<>();
            xVoters = new ArrayList<>();

            correctVoterNames = new ArrayList<>();
            incorrectVoterNames = new ArrayList<>();
            didntVoteNames = new ArrayList<>();

            for (String k : votesThisRound.keySet()) {
                if (votesThisRound.get(k).equals("T")) {
                    Voter v;
                    v = new Voter(k, timesThisRound.get(k), true);
                    trueVoters.add(v);
                    if (wasInFact.equals("T"))
                    {
                        correctVoterNames.add(v.getName());
                        if(fastestPlayers.contains(k)) v.setFastestAndCorrect(true);
                    }
                }
                if (votesThisRound.get(k).equals("L")) {
                    Voter v;
                    v = new Voter(k, timesThisRound.get(k), false);
                    lieVoters.add(v);
                    if (wasInFact.equals("L"))
                    {
                        incorrectVoterNames.add(v.getName());
                        if(fastestPlayers.contains(k)) v.setFastestAndCorrect(true);
                    }
                }
                if (votesThisRound.get(k).equals("X")) {
                    Voter v;
                    v = new Voter(k, timesThisRound.get(k), null);
                    xVoters.add(v);
                    didntVoteNames.add(v.getName());
                }
            }

            // Apply achievements logic
            achievementsUnlocked = new HashMap<>();
            for (Player p : playersWhovePlayed)
            {
                ArrayList<Achievement> achievements =
                        applyAchievementsLogic(p.getName(), name, wasInFact,
                                writtenBy, trueVoters, lieVoters, xVoters, fastestPlayers);

                achievementsUnlocked.put(p.getName(), achievements);
            }

            Log.i(TAG, "interpret: ABOUT TO ADD TURN DATA");
            turnData.add(new TurnData(turnNumber, name, wasInFact,
                    writtenBy, trueVoters, lieVoters, xVoters, fastestPlayers, achievementsUnlocked, playersText,
                    correctVoterNames,incorrectVoterNames,didntVoteNames,
                    saboName,s_badSabo,s_mostEarned,s_allEarned,
                    p_fiftyFiftyEarned,p_mostEarned,p_allEarned,p_nobodyVoted));
            Log.i(TAG, "interpret: TURN DATA ADDED SUCCESSFULLY");

        }

        calculateResultsData();
    }

    private void calculateResultsData() {
        Log.i(TAG, "calculateResultsData: CALLED");

        resultsData = new ResultsData(turnData, playersScores);
        resultsData.calculate();

    }

    String saboName;
    boolean p_fiftyFiftyEarned, p_mostEarned, p_allEarned, p_nobodyVoted;
    boolean s_mostEarned, s_allEarned, s_badSabo;

    ArrayList<String> correctVoterNames = new ArrayList<>();
    ArrayList<String> incorrectVoterNames = new ArrayList<>();
    ArrayList<String> didntVoteNames = new ArrayList<>();

    private ArrayList<Achievement> applyAchievementsLogic(String achieverName,
                                                          String playerWhoseTurn,
                                                          String wasInFact,
                                                          String writtenBy,
                                                          ArrayList<Voter> trueVoters,
                                                          ArrayList<Voter> lieVoters,
                                                          ArrayList<Voter> xVoters,
                                                          ArrayList<String> fastestPlayers) {

        Log.i(TAG, "applyAchievementsLogic: CALLED");

        ArrayList<Achievement> achievements = new ArrayList<>();

        // If is player themselves...
        if (achieverName.equals(playerWhoseTurn)) {

            // Nobody voted scenario
            if(lieVoters.size() == 0 && trueVoters.size() == 0)
            {
                achievements.add(p_zeroVote);
                p_nobodyVoted = true;
            }

            // Provided people voted...
            if (lieVoters.size() > 0 || trueVoters.size() > 0) {

                if (wasInFact.equals("T")) {
                    if (trueVoters.size() == 0) {
                        achievements.add(p_allVote);
                        p_allEarned = true;
                    } else if (trueVoters.size() < lieVoters.size()) {
                        achievements.add(p_mostVote);
                        p_mostEarned = true;
                    } else if (trueVoters.size() == lieVoters.size()) {
                        achievements.add(p_splitVote);
                        p_fiftyFiftyEarned = true;
                    }
                } else {
                    if (lieVoters.size() == 0) {
                        achievements.add(p_allVote);
                        p_allEarned = true;
                    } else if (lieVoters.size() < trueVoters.size()) {
                        achievements.add(p_mostVote);
                        p_mostEarned = true;
                    } else if (lieVoters.size() == trueVoters.size()) {
                        achievements.add(p_splitVote);
                        p_fiftyFiftyEarned = true;
                    }
                }
            }
        }

        // If player wrote the statement, and is not the player playing...
        if (!achieverName.equals(playerWhoseTurn) && achieverName.equals(writtenBy)) {

            saboName = achieverName;

            // Provided people voted...
            if (lieVoters.size() > 0 || trueVoters.size() > 0) {
                // If statement was true (bad saboteur scenario)...
                if (wasInFact.equals("T")) {
                    achievements.add(s_changedToTrue);
                    s_badSabo = true;
                } else if (lieVoters.size() == 0) {
                    achievements.add(s_allVote);
                    s_allEarned = true;
                } else if (lieVoters.size() < trueVoters.size()) {
                    achievements.add(s_mostVote);
                    s_mostEarned = true;
                }
            }
        }

        // If player is neither player, nor a writer (player is voter)...
        if (!achieverName.equals(playerWhoseTurn) && !achieverName.equals(writtenBy)) {

            if (wasInFact.equals("T")) {

                boolean playerFound = false;
                boolean playerFastest = false;

                for (Voter v : trueVoters) {
                    if (achieverName.equals(v.getName())) {

                        playerFound = true;

                        if (v.getFastestAndCorrect() && fastestPlayers.contains(v.getName())
                                && trueVoters.size() > 1) playerFastest = true;
                    }
                }

                if (playerFound) {
                    // Correct vote points
                    achievements.add(v_correctVoteTrue);
                    correctVoterNames.add(achieverName);

                    // Minority vote points
                    if (trueVoters.size() < lieVoters.size()) {
                        achievements.add(v_minorityVote);
                    }

                    // Fastest players points
                    if (playerFastest) {
                        achievements.add(v_fastestVote);
                    }
                }
                else {
                    incorrectVoterNames.add(achieverName);
                }


            } else {

                boolean playerFound = false;
                boolean playerFastest = false;

                for (Voter v : lieVoters) {
                    if (achieverName.equals(v.getName())) {

                        playerFound = true;

                        if (v.getFastestAndCorrect() && fastestPlayers.contains(v.getName())
                                && lieVoters.size() > 1) playerFastest = true;
                    }
                }

                if (playerFound) {
                    // Correct vote points
                    achievements.add(v_correctVoteLie);
                    correctVoterNames.add(achieverName);

                    // Minority vote points
                    if (lieVoters.size() < trueVoters.size()) {
                        achievements.add(v_minorityVote);
                    }

                    // Fastest players points
                    if (playerFastest) {
                        achievements.add(v_fastestVote);
                    }
                }
                else {
                    incorrectVoterNames.add(achieverName);
                }
            }
        }

        return achievements;
    };

    private void addAchievements(){
        v_correctVoteTrue = new Achievement("Got To The Truth","You voted correctly!",30); // Player voted correctly
        v_correctVoteLie = new Achievement("Lie Detector","You voted correctly!",30); // Player voted correctly
        v_minorityVote = new Achievement("Against The Grain","You voted correctly in the minority!",20); // Player voted correctly in minority of overall vote
        v_fastestVote = new Achievement("Finger On The Button","You voted correctly the quickest!",20);

        s_changedToTrue = new Achievement("Saboteur Sabotaged","The lie you wrote turned out to be true!",-30); // Saboteurs lie turned out to be true
        s_mostVote = new Achievement("Great Work Of Fiction","Most people fell for a lie you wrote!",30); // Saboteurs lie mostly convinced the group true
        s_allVote = new Achievement("Biggest Lie Ever Sold","Everyone fell for a lie you wrote!",50); // Saboteurs lie wholly convinced the group true

        p_zeroVote = new Achievement("Complete Waste of Time","Nobody voted. Oh well, here's some points.",20);
        p_splitVote  = new Achievement("House Divided","You fooled half the room!",20); // Player's statement split the group 50/50
        p_mostVote  = new Achievement("Professional Actor","You fooled most of the room!",60); // Player's statement mostly convinced the group the opposite
        p_allVote  = new Achievement("Oscar-Worthy","You fooled everyone!",100); // Player's statement wholly convinced the group the opposite
    }

    public int getTurnsCount() {return turnData.size();}

    public List<TurnData> getTurnData() {
        return turnData;
    }

    public ResultsData getResultsData() {
        return resultsData;
    }

    public HashMap<String, String> getPlayersText() {
        return playersText;
    }
}
