package com.arrowsmith.llv1.classes;

import java.io.Serializable;
import java.util.HashMap;

public class Manifest<T> implements Serializable {

    private String refresh;
    private HashMap<String,T> players;

    public Manifest(){};

    public Manifest(HashMap<String,T> hashMap){
            refresh = "";
            players = hashMap;
    }

    public HashMap<String,T> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<String,T> players) {
        this.players = players;
    }
}
