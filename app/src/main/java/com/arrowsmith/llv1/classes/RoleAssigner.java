package com.arrowsmith.llv1.classes;

import android.util.Log;

import androidx.annotation.Nullable;

import com.arrowsmith.llv1.MainActivity;

import java.util.ArrayList;
import java.util.Collections;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class RoleAssigner {

    private ArrayList<Player> playersList;
    boolean allTrueAllowed;

    public RoleAssigner() {
        allTrueAllowed = true;
    }

    public RoleAssigner(ArrayList<Player> playersList) {
        allTrueAllowed = true;
        this.playersList = playersList;
    }

    public ArrayList<Player> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(ArrayList<Player> playersList) {
        this.playersList = playersList;
    }

    public boolean isAllTrueAllowed() {
        return allTrueAllowed;
    }

    public void setAllTrueAllowed(boolean allTrueAllowed) {
        this.allTrueAllowed = allTrueAllowed;
    }

    public void assignRoles() {

        if(playersList == null) return;

        int numberOfPlayers = playersList.size();

        ArrayList<Integer> lieIndices = new ArrayList<>();
        ArrayList<Integer> trueIndices = new ArrayList<>();

        int lieCount;
        int timesDone = 0;

        do{
            lieCount = 0;

            lieIndices.clear();
            trueIndices.clear();

            for (int i = 0; i < numberOfPlayers; i++){

                double randomNum = Math.random();

                if (randomNum <= 0.5) {
                    playersList.get(i).setTruth(true);
                    trueIndices.add(i);
                }
                else {
                    lieCount++;
                    playersList.get(i).setTruth(false);
                    lieIndices.add(i);
                }
            }

            Log.i(MainActivity.TAG, "assignRoles: All true check loop run " + timesDone++);

        } while(!allTrueAllowed && lieCount == 0);


        if (lieCount == 1){
            Log.i(MainActivity.TAG, "assignRoles: Single liar scenario invoked");

            int newIndex = (int) (trueIndices.size()*Math.random()) ;

            playersList.get(trueIndices.get(newIndex)).setTruth(false);
            lieIndices.add(trueIndices.get(newIndex));

            lieCount++;
        }

        for (int j = 0; j < numberOfPlayers; j++){
            Log.i(TAG, "assignRoles: FINAL ROLE: "
                    +playersList.get(j).getName()+": "
                    +playersList.get(j).getTruth());
        }

        if (lieCount != 0){

            assignTargets(lieIndices, trueIndices);

        }else{

            // do nothing
        }

    }

    private void assignTargets(@Nullable ArrayList<Integer> lieIndices, @Nullable ArrayList<Integer> trueIndices) {

        // Produces the lieIndices list if not provided
        if (lieIndices == null) {
            lieIndices = new ArrayList<Integer>();
            for (int i = 0; i < playersList.size(); i++) {
                if (!playersList.get(i).getTruth())
                    lieIndices.add(i);
            }
        }
        // Produces the trueIndices list if not provided
        if (trueIndices == null) {
            trueIndices = new ArrayList<Integer>();
            for (int i = 0; i < playersList.size(); i++) {
                if (playersList.get(i).getTruth())
                    trueIndices.add(i);
            }
        }

        // Creates permutations until a derangement is found
        ArrayList<Integer> targets = new ArrayList<>(lieIndices);
        boolean isDerangement = false;

        Log.i(TAG, "assignTargets: "+printList(targets)+" - ORIGINAL");

        while (!isDerangement) {

            Log.i(TAG, "assignTargets: shuffle performed");
            Collections.shuffle(targets);
            Log.i(TAG, "assignTargets: "+printList(targets)+" - shuffled");
            isDerangement = true;

            for (int i = 0; i < lieIndices.size(); i++) {
                if (lieIndices.get(i).equals(targets.get(i))) {
                    isDerangement = false;
                    break;
                }
            }
        }

        // Assigns targets to players
        for (int i=0; i < lieIndices.size(); i++){
            playersList.get(
                    lieIndices.get(i)).setTarget(playersList.get(targets.get(i)).getName());
        }

        // end

    }

    public static String printList(ArrayList<Integer> list){

        String txt = "";

        for (int i =0; i < list.size(); i++){
            txt += list.get(i) + ", ";
        }

        return txt;
    }


}
