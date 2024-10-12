package com.example.review_extractor.service;


import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

@Service
public class LLMService {

    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    // Choose a suitable model. Replace 'gpt2' with your chosen model if different.
    private static final String HUGGINGFACE_API_URL = "https://api-inference.huggingface.co/models/gpt2";

    // Define maximum characters to send
    private static final int MAX_CHARACTERS = 5000;

    /**
     * Sends a prompt to the Hugging Face Inference API and retrieves the generated CSS selector.
     *
     * @param url        The URL of the product page.
     * @param pageSource The HTML content of the product page.
     * @return The CSS selector for review elements.
     * @throws UnirestException If an error occurs during the HTTP request.
     */
    public String getReviewCssSelector(String url, String pageSource) throws UnirestException {
        logger.debug("Fetching review CSS selector for URL: {}", url);

        // Extract relevant HTML sections using Jsoup
        String relevantHtml = extractRelevantHtml(pageSource);

        // Truncate if still too large
        if (relevantHtml.length() > MAX_CHARACTERS) {
            relevantHtml = relevantHtml.substring(0, MAX_CHARACTERS);
            logger.debug("Truncated HTML content to {} characters.", MAX_CHARACTERS);
        }

        String prompt = String.format(
                "Analyze the following HTML content of a product page: [URL: %s]. Identify the CSS selector that can be used to select all review elements on this page.\n\nHTML Content:\n%s\n\nProvide only the CSS selector without any additional explanation.",
                url, relevantHtml
        );

        // Create a Map for the request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("inputs", prompt);
        Map<String, Object> options = new HashMap<>();
        options.put("use_cache", false);
        requestBodyMap.put("options", options);

        // Serialize to JSON using Jackson
        ObjectMapper mapper = new ObjectMapper();
        String requestBodyJson;
        try {
            requestBodyJson = mapper.writeValueAsString(requestBodyMap);
        } catch (Exception e) {
            logger.error("Failed to serialize request body to JSON.", e);
            throw new RuntimeException("Failed to serialize request body to JSON.", e);
        }

        logger.debug("Sending request to Hugging Face API.");

        HttpResponse<String> response;
        try {
            response = Unirest.post(HUGGINGFACE_API_URL)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + huggingFaceApiKey)
                    .body(requestBodyJson)
                    .asString();
        } catch (UnirestException e) {
            logger.error("Failed to connect to Hugging Face API.", e);
            throw e;
        }

        logger.debug("Received response from Hugging Face API with status: {}", response.getStatus());

        if (response.getStatus() == 200) {
            // Parse the response to extract the CSS selector
            try {
                JsonNode root = mapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    String generatedText = root.get(0).get("generated_text").asText();
                    logger.debug("Generated CSS Selector: {}", generatedText.trim());
                    return generatedText.trim();
                }
            } catch (Exception e) {
                logger.error("Failed to parse Hugging Face API response.", e);
                throw new RuntimeException("Failed to parse Hugging Face API response.", e);
            }
        } else {
            logger.error("Failed to get response from Hugging Face API: {} - {}", response.getStatus(), response.getBody());
            throw new RuntimeException("Failed to get response from Hugging Face API: " + response.getStatus() + " - " + response.getBody());
        }

        return null;
    }

    /**
     * Sends a prompt to the Hugging Face Inference API and retrieves the CSS selector for the "Next" page button.
     *
     * @param url        The URL of the product page.
     * @param pageSource The HTML content of the product page.
     * @return The CSS selector for the "Next" page button.
     * @throws UnirestException If an error occurs during the HTTP request.
     */
    public String getNextPageCssSelector(String url, String pageSource) throws UnirestException {
        logger.debug("Fetching next page CSS selector for URL: {}", url);

        // Extract relevant HTML sections using Jsoup
        String relevantHtml = extractRelevantHtml(pageSource);

        // Truncate if still too large
        if (relevantHtml.length() > MAX_CHARACTERS) {
            relevantHtml = relevantHtml.substring(0, MAX_CHARACTERS);
            logger.debug("Truncated HTML content to {} characters.", MAX_CHARACTERS);
        }

        String prompt = String.format(
                "Analyze the following HTML content of a product page: [URL: %s]. Identify the CSS selector for the 'Next' button or link that navigates to the next page of reviews.\n\nHTML Content:\n%s\n\nProvide only the CSS selector without any additional explanation.",
                url, relevantHtml
        );

        // Create a Map for the request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("inputs", prompt);
        Map<String, Object> options = new HashMap<>();
        options.put("use_cache", false);
        requestBodyMap.put("options", options);

        // Serialize to JSON using Jackson
        ObjectMapper mapper = new ObjectMapper();
        String requestBodyJson;
        try {
            requestBodyJson = mapper.writeValueAsString(requestBodyMap);
        } catch (Exception e) {
            logger.error("Failed to serialize request body to JSON.", e);
            throw new RuntimeException("Failed to serialize request body to JSON.", e);
        }

        logger.debug("Sending request to Hugging Face API for next page CSS selector.");

        HttpResponse<String> response;
        try {
            response = Unirest.post(HUGGINGFACE_API_URL)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + huggingFaceApiKey)
                    .body(requestBodyJson)
                    .asString();
        } catch (UnirestException e) {
            logger.error("Failed to connect to Hugging Face API.", e);
            throw e;
        }

        logger.debug("Received response from Hugging Face API with status: {}", response.getStatus());

        if (response.getStatus() == 200) {
            // Parse the response to extract the CSS selector
            try {
                JsonNode root = mapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    String generatedText = root.get(0).get("generated_text").asText();
                    logger.debug("Generated Next Page CSS Selector: {}", generatedText.trim());
                    return generatedText.trim();
                }
            } catch (Exception e) {
                logger.error("Failed to parse Hugging Face API response.", e);
                throw new RuntimeException("Failed to parse Hugging Face API response.", e);
            }
        } else {
            logger.error("Failed to get response from Hugging Face API: {} - {}", response.getStatus(), response.getBody());
            throw new RuntimeException("Failed to get response from Hugging Face API: " + response.getStatus() + " - " + response.getBody());
        }

        return null;
    }

    /**
     * Extracts relevant HTML sections that likely contain reviews to reduce the payload size.
     *
     * @param html The full HTML content of the page.
     * @return A string containing only the relevant HTML sections.
     */
    private String extractRelevantHtml(String html) {
        Document doc = Jsoup.parse(html);
        Elements reviewSections = new Elements();

        // Common patterns in review sections
        String[] reviewKeywords = {"review", "feedback", "customer-reviews", "user-reviews", "ratings"};

        for (String keyword : reviewKeywords) {
            reviewSections.addAll(doc.select("*[class*=" + keyword + "], *[id*=" + keyword + "]"));
        }

        // If no sections found, fallback to the entire body
        if (reviewSections.isEmpty()) {
            reviewSections = doc.select("body");
            logger.debug("No specific review sections found. Using the entire body.");
        } else {
            logger.debug("Found {} review sections.", reviewSections.size());
        }

        // Remove scripts and styles to reduce size
        reviewSections.select("script, style").remove();

        // Convert the selected elements back to HTML
        return reviewSections.html();
    }
}
