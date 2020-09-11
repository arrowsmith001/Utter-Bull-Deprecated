package com.arrowsmith.llv1.classes;

import java.io.Serializable;
import java.util.ArrayList;


public class Player implements Serializable {

    private String name; // Player name
    private Boolean isHosting; // Whether player is hosting or not
    private Boolean isReady; // Whether player is ready or not
    private Boolean isTruth; // Whether player is telling truth or not
    private String text; // The content this player must read out
    private String readyFor; // What player is ready for
    private Integer points; // How many points the player has earned
    private String target; // Target of lie
    private ArrayList<String> votes; // Keeps tracks of votes cast / round tracking info (T,L,s,p)
    private ArrayList<Integer> voteTimes; // Keeps tracks of vote times

    public ArrayList<Integer> getVoteTimes() {
        return voteTimes;
    }

    public void setVoteTimes(ArrayList<Integer> voteTimes) {
        this.voteTimes = voteTimes;
    }

    public void addVote(String vote){

        if(votes == null) votes = new ArrayList<>();

        switch(vote){
            case "T":
                votes.add(vote);
                break;
            case "L":
                votes.add(vote);
                break;
            case "s":
                votes.add(vote);
                break;
            case "p":
                votes.add(vote);
                break;
            case "X":
                votes.add(vote);
                break;
            default:
               // Log.i(TAG, "addVote: ERROR: INVALID VOTE CAST");
        }

    }
    public void addTime(Integer time){

        if(voteTimes == null) voteTimes = new ArrayList<>();

        voteTimes.add(time);

    }

    public ArrayList<String> getVotes() {
        return votes;
    }

    public void setVotes(ArrayList<String> votes) {
        this.votes = votes;
    }

    public Player(){};

    public Player(Boolean isReady, String readyFor){
        this.isReady = isReady;
        this.readyFor = readyFor;
    }

    public Player(String name){

        this.name = name;
    }

    public Player(Integer points) {

        this.points = points;
    }

    public Boolean getHosting() {
        return isHosting;
    }

    public void setHosting(Boolean hosting) {
        isHosting = hosting;
    }

    private Boolean isMe; // Whether player corresponds to "me"

    public void addPoints(Integer newPoints){
        this.points = this.points + newPoints;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public Boolean getTruth() {
        return isTruth;
    }

    public void setTruth(Boolean truth) {
        isTruth = truth;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReadyFor() {
        return readyFor;
    }

    public void setReadyFor(String readyFor) {
        this.readyFor = readyFor;
    }

    @androidx.annotation.NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
