package com.example.dishcovery1.ui.notifications;

public class Comment {
    private String username;
    private String comment;
    private float rating;

    // Getters
    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }

    public float getRating() {
        return rating;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
