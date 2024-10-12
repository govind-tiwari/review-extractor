package com.example.review_extractor.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.example.review_extractor.model.Review;
import com.example.review_extractor.model.ReviewResponse;

@Service
public class ReviewService {

    private final LLMService llmService;

    public ReviewService(LLMService llmService) {
        this.llmService = llmService;
    }

    /**
     * Extracts reviews from the given product page URL.
     *
     * @param url The URL of the product page.
     * @return A ReviewResponse containing the count and list of reviews.
     * @throws Exception If an error occurs during extraction.
     */
    public ReviewResponse extractReviews(String url) throws Exception {
        // Initialize Selenium WebDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Use headless mode
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get(url);

            // Wait until the body is loaded
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));

            // Get the page source
            String pageSource = driver.getPageSource();

            // Use LLM to identify CSS selectors for reviews and pagination
            String reviewCssSelector = llmService.getReviewCssSelector(url, pageSource);
            String nextPageCssSelector = llmService.getNextPageCssSelector(url, pageSource);

            if (reviewCssSelector == null || reviewCssSelector.isEmpty()) {
                throw new Exception("Unable to identify review CSS selector.");
            }

            List<Review> allReviews = new ArrayList<>();
            boolean hasNextPage = true;

            while (hasNextPage) {
                // Find review elements
                List<WebElement> reviewElements = driver.findElements(By.cssSelector(reviewCssSelector));

                for (WebElement element : reviewElements) {
                    // Extract review details based on the structure
                    String title = "";
                    String body = "";
                    int rating = 0;
                    String reviewer = "";

                    try {
                        // These selectors might need to be adjusted based on the identified structure
                        title = element.findElement(By.cssSelector(".review-title")).getText();
                    } catch (NoSuchElementException e) {
                        // Handle missing title
                    }

                    try {
                        body = element.findElement(By.cssSelector(".review-body")).getText();
                    } catch (NoSuchElementException e) {
                        // Handle missing body
                    }

                    try {
                        String ratingStr = element.findElement(By.cssSelector(".review-rating")).getText();
                        rating = Integer.parseInt(ratingStr.trim());
                    } catch (NoSuchElementException | NumberFormatException e) {
                        // Handle missing or malformed rating
                    }

                    try {
                        reviewer = element.findElement(By.cssSelector(".reviewer-name")).getText();
                    } catch (NoSuchElementException e) {
                        // Handle missing reviewer name
                    }

                    Review review = new Review(title, body, rating, reviewer);
                    allReviews.add(review);
                }

                // Attempt to navigate to the next page
                try {
                    WebElement nextPageButton = driver.findElement(By.cssSelector(nextPageCssSelector));
                    nextPageButton.click();
                    // Wait until the next page loads by waiting for the first review element to become stale
                    if (!reviewElements.isEmpty()) {
                        wait.until(ExpectedConditions.stalenessOf(reviewElements.get(0)));
                    }
                    // Wait until the body is visible again
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));
                } catch (NoSuchElementException | TimeoutException e) {
                    hasNextPage = false; // No more pages
                }
            }

            int totalReviews = allReviews.size();
            return new ReviewResponse(totalReviews, allReviews);
        } finally {
            driver.quit();
        }
    }
}

