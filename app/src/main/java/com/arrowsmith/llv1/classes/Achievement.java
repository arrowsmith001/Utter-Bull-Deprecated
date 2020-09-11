package com.arrowsmith.llv1.classes;

import androidx.annotation.NonNull;

public class Achievement {

    String message;
    String name;
    Boolean achieved;
    int pointsWorth;

    public Achievement() {
    }

    public Achievement(String name, String message, int pointsWorth) {
        this.name = name;
        this.message = message;
        this.pointsWorth = pointsWorth;
        this.achieved = false;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " - " + message + " +" + pointsWorth + "pts";
    }

    public String toSimpleString(){

        return (pointsWorth > 0 ? "+" : "-") + Integer.toString(Math.abs(pointsWorth)) + " - " + message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAchieved() {
        return achieved;
    }

    public void setAchieved(Boolean achieved) {
        this.achieved = achieved;
    }

    public int getPointsWorth() {
        return pointsWorth;
    }

    public void setPointsWorth(int pointsWorth) {
        this.pointsWorth = pointsWorth;
    }

    public String getPointsString(){
        return (pointsWorth < 0 ? "-" : "+")+Integer.toString(Math.abs(pointsWorth))+" ";
    }

}