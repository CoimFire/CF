package com.nexustech.comicfire.domains;

public class Contestants {
    String UserId,Points;

    public Contestants() {
    }

    public Contestants(String userId, String points) {
        UserId = userId;
        Points = points;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getPoints() {
        return Points;
    }

    public void setPoints(String points) {
        Points = points;
    }
}
