package com.angelzbg.communityforum.models;

public class User {

    private String username;
    private String avatar;
    private int points;
    private long date;

    public User(){}

    public User(String username, String avatar, int points, long date) {
        this.username = username;
        this.avatar = avatar;
        this.points = points;
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
} // User{}