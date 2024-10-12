package com.example.review_extractor.model;


public class Review {
    private String title;
    private String body;
    private int rating;
    private String reviewer;

    public Review(String title, String body, int rating, String reviewer) {
        this.title = title;
        this.body = body;
        this.rating = rating;
        this.reviewer = reviewer;
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public int getRating() {
        return rating;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }
}
