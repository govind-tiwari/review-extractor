package com.example.review_extractor.model;


import java.util.List;

public class ReviewResponse {
    private int reviews_count;
    private List<Review> reviews;

    public ReviewResponse(int reviews_count, List<Review> reviews) {
        this.reviews_count = reviews_count;
        this.reviews = reviews;
    }

    // Getters and Setters

    public int getReviews_count() {
        return reviews_count;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews_count(int reviews_count) {
        this.reviews_count = reviews_count;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
