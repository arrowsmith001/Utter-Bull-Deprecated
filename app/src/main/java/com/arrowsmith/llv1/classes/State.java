package com.arrowsmith.llv1.classes;

import java.io.Serializable;

public class State implements Serializable {

    private boolean inSession; // Whether game is in session, or not
    private String phase; // State of the game

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public State(){};

    public State(boolean inSession){
        this.inSession = inSession;
    }

    public boolean isInSession() {
        return inSession;
    }

    public void setInSession(boolean inSession) {
        this.inSession = inSession;
    }
}
