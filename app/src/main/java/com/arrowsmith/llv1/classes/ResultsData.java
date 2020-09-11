package com.arrowsmith.llv1.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultsData {


    private final List<TurnData> turnData;
    private final HashMap<String, Integer> oldScores;

    HashMap<String, Integer> newScores;
    HashMap<String, Integer> scoreDiff;
    List<String> playersOrdered;

    public ResultsData(List<TurnData> turnData, HashMap<String, Integer> playersScores) {

        this.turnData = turnData;
        this.oldScores = playersScores;
    }

    List<HashMap<String,Integer>> turnScores;

    public void calculate()
    {
        newScores = new HashMap<>();
        scoreDiff = new HashMap<>();

        for(String playerName : oldScores.keySet())
        {
            int score = 0;

            for(TurnData td : turnData)
            {
                List<Achievement> achievements = td.getAchievementsUnlocked().get(playerName);

                for (Achievement a : achievements)
                {
                    score += a.getPointsWorth();
                }
            }

            newScores.put(playerName, score + oldScores.get(playerName));
            scoreDiff.put(playerName, score);
        }

        // Figure out order
        playersOrdered = new ArrayList<>();

        for (int i = 0; i < newScores.keySet().size(); i++)
        {
            String highestScorer = "";
            int highScore = -10000000;

            for (String playerName : newScores.keySet())
            {
                if(newScores.get(playerName) > highScore
                        && !playersOrdered.contains(playerName)) {
                    highestScorer = playerName;
                    highScore = newScores.get(playerName);
                }
            }

            playersOrdered.add(highestScorer);
        }

    }

    public HashMap<String, Integer> getOldScores() {
        return oldScores;
    }

    public HashMap<String, Integer> getNewScores() {
        return newScores;
    }

    public List<String> getPlayersOrdered() {
        return playersOrdered;
    }

    public HashMap<String, Integer> getScoreDiff() {
        return scoreDiff;
    }

    public List<TurnData> getTurnData() {
        return turnData;
    }

}
