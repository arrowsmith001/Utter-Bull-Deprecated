package com.arrowsmith.llv1.classes;

public class Voter {

    String name;
    int timeLeft;
    Boolean fastestAndCorrect;
    Boolean truth;

    public Voter() {
    }

    public Voter(String name, int timeLeft, Boolean truth) {
        this.name = name;
        this.timeLeft = timeLeft;
        this.truth = truth;
        this.fastestAndCorrect = false;
    }

    public Boolean getFastestAndCorrect() {
        return fastestAndCorrect;
    }

    public void setFastestAndCorrect(Boolean fastestAndCorrect) {
        this.fastestAndCorrect = fastestAndCorrect;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getTruth() {
        return truth;
    }

    public void setTruth(Boolean truth) {
        this.truth = truth;
    }
}