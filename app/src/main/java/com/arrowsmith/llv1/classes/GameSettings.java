package com.arrowsmith.llv1.classes;

import java.io.Serializable;

public class GameSettings implements Serializable {

    private int roundTimeMins;
    private boolean allTrueEnabled;
    private boolean lewdnessEnabled;

    public GameSettings(){
        this.roundTimeMins = 3;
        this.allTrueEnabled = true;
        this.lewdnessEnabled = false;
    };

    public GameSettings(int roundTimeMins,boolean allTrueEnabled, boolean lewdNessEnabled)
    {
        this.roundTimeMins = roundTimeMins;
        this.allTrueEnabled = allTrueEnabled;
        this.lewdnessEnabled = lewdNessEnabled;
    }

    public boolean isAllTrueEnabled() {
        return allTrueEnabled;
    }

    public void setAllTrueEnabled(boolean allTrueEnabled) {
        this.allTrueEnabled = allTrueEnabled;
    }

    public boolean isLewdnessEnabled() {
        return lewdnessEnabled;
    }

    public void setLewdnessEnabled(boolean lewdnessEnabled) {
        this.lewdnessEnabled = lewdnessEnabled;
    }

    public GameSettings(int roundTimeMins){
        this.roundTimeMins = roundTimeMins;
    }

    public int getRoundTimeMins() {
        return roundTimeMins;
    }

    public void setRoundTimeMins(int roundTimeMins) {
        this.roundTimeMins = roundTimeMins;
    }
}
