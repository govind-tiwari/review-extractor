
package com.example.review_extractor.controller;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.review_extractor.model.ReviewResponse;
import com.example.review_extractor.service.ReviewService;

@CrossOrigin(origins = "http://localhost:3000") // Enable CORS for this controller
@RestController
@RequestMapping("/api")
public class ReviewController {

 @Autowired
 private ReviewService reviewService;

 /**
  * Endpoint to retrieve reviews from a given product page URL.
  *
  * @param url The URL of the product page.
  * @return A ResponseEntity containing the ReviewResponse or an error message.
  */
 @GetMapping("/reviews")
 public ResponseEntity<?> getReviews(@RequestParam("page") String url) {
     // Validate URL
     try {
         new URL(url);
     } catch (MalformedURLException e) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL provided.");
     }

     try {
         ReviewResponse response = reviewService.extractReviews(url);
         return ResponseEntity.ok(response);
     } catch (Exception e) {
         // Log the error (optional)
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body("Error extracting reviews: " + e.getMessage());
     }
 }
}

