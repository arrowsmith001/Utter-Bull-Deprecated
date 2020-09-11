package com.arrowsmith.llv1.classes;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class TurnData {

    private final Integer turnNumber;
    private final String name;
    private final String wasInFact;
    private final String writtenBy;
    private final ArrayList<Voter> trueVoters;
    private final ArrayList<Voter> lieVoters;
    private final ArrayList<Voter> xVoters;
    private final ArrayList<String> fastestPlayers;
    private final HashMap<String,ArrayList<Achievement>> achievementsUnlocked;
    private final HashMap<String, String> playersText;
    private final ArrayList<String> correctVoterNames;
    private final ArrayList<String> incorrectVoterNames;
    private final ArrayList<String> didntVoteNames;
    private final String saboName;
    private final Boolean s_badSabo;
    private final Boolean s_mostEarned;
    private final Boolean s_allEarned;
    private final boolean p_fiftyFiftyEarned;
    private final boolean p_mostEarned;
    private final boolean p_allEarned;
    private final boolean p_nobodyVoted;

    public TurnData(Integer turnNumber, String name, String wasInFact, String writtenBy, ArrayList<Voter> trueVoters, ArrayList<Voter> lieVoters, ArrayList<Voter> xVoters, ArrayList<String> fastestPlayers, HashMap<String, ArrayList<Achievement>> achievementsUnlocked, HashMap<String, String> playersText,
                    ArrayList<String> correctVoterNames, ArrayList<String> incorrectVoterNames, ArrayList<String> didntVoteNames, @Nullable String saboName, Boolean s_badSabo, Boolean s_mostEarned, Boolean s_allEarned, boolean p_fiftyFiftyEarned, boolean p_mostEarned, boolean p_allEarned, boolean p_nobodyVoted) {

        Log.i(TAG, "TurnData: CALLED");
        this.turnNumber = turnNumber;
        this.name = name;
        this.wasInFact = wasInFact;
        this.writtenBy = writtenBy;
        this.trueVoters = trueVoters;
        this.lieVoters = lieVoters;
        this.xVoters = xVoters;
        this.fastestPlayers = fastestPlayers;
        this.achievementsUnlocked = achievementsUnlocked;
        this.playersText = playersText;

        this.correctVoterNames = correctVoterNames;
        this.incorrectVoterNames = incorrectVoterNames;
        this.didntVoteNames = didntVoteNames;
        this.saboName = saboName;
        this.s_badSabo = s_badSabo;
        this.s_mostEarned = s_mostEarned;
        this.s_allEarned = s_allEarned;
        this.p_fiftyFiftyEarned = p_fiftyFiftyEarned;
        this.p_mostEarned = p_mostEarned;
        this.p_allEarned = p_allEarned;
        this.p_nobodyVoted = p_nobodyVoted;
    }

    public Integer getTurnNumber() {
        return turnNumber;
    }

    public String getName() {
        return name;
    }

    public String getWasInFact() {
        return wasInFact;
    }

    public String getWrittenBy() {
        return writtenBy;
    }

    public ArrayList<Voter> getTrueVoters() {
        return trueVoters;
    }

    public ArrayList<Voter> getLieVoters() {
        return lieVoters;
    }

    public ArrayList<Voter> getxVoters() {
        return xVoters;
    }

    public ArrayList<String> getFastestPlayers() {
        return fastestPlayers;
    }

    public HashMap<String, ArrayList<Achievement>> getAchievementsUnlocked() {
        return achievementsUnlocked;
    }

    public HashMap<String, String> getPlayersText() {
        return playersText;
    }

    public ArrayList<String> getCorrectVoterNames() {
        return correctVoterNames;
    }

    public ArrayList<String> getIncorrectVoterNames() {
        return incorrectVoterNames;
    }

    public ArrayList<String> getDidntVoteNames() {
        return didntVoteNames;
    }

    public @Nullable String getSaboName() {
        return saboName;
    }

    public Boolean getS_badSabo() {
        return s_badSabo;
    }

    public Boolean getS_mostEarned() {
        return s_mostEarned;
    }

    public Boolean getS_allEarned() {
        return s_allEarned;
    }

    public boolean isP_fiftyFiftyEarned() {
        return p_fiftyFiftyEarned;
    }

    public boolean isP_mostEarned() {
        return p_mostEarned;
    }

    public boolean isP_allEarned() {
        return p_allEarned;
    }

    public boolean isP_nobodyVoted() {
        return p_nobodyVoted;
    }
}
