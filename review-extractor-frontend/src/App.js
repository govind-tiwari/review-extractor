// src/App.js

import React, { useState } from 'react';
import axios from 'axios';
import './App.css'; // For styling (optional)

function App() {
  const [url, setUrl] = useState('');
  const [reviews, setReviews] = useState([]);
  const [reviewsCount, setReviewsCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Reset previous states
    setReviews([]);
    setReviewsCount(0);
    setError(null);

    // Simple URL validation
    try {
      new URL(url);
    } catch (_) {
      setError('Please enter a valid URL.');
      return;
    }

    setLoading(true);

    try {
      // Make GET request to the backend API
      const response = await axios.get('http://localhost:8080/api/reviews', {
        params: { page: url },
      });

      // Assuming the API returns data in the following format:
      // {
      //   "reviews_count": 100,
      //   "reviews": [
      //     {
      //       "title": "Great Product",
      //       "body": "I really enjoyed using this product because...",
      //       "rating": 5,
      //       "reviewer": "John Doe"
      //     },
      //     ...
      //   ]
      // }

      setReviewsCount(response.data.reviews_count);
      setReviews(response.data.reviews);
    } catch (err) {
      console.error(err);
      if (err.response) {
        // Server responded with a status other than 2xx
        setError(`Error: ${err.response.data}`);
      } else if (err.request) {
        // Request was made but no response received
        setError('Error: No response from the server.');
      } else {
        // Something else happened
        setError(`Error: ${err.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="App">
      <h1>Review Extractor</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="url"
          placeholder="Enter product page URL"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Extracting...' : 'Extract Reviews'}
        </button>
      </form>

      {error && <p className="error">{error}</p>}

      {reviewsCount > 0 && (
        <div className="reviews-section">
          <h2>Total Reviews Extracted: {reviewsCount}</h2>
          <ul>
            {reviews.map((review, index) => (
              <li key={index} className="review">
                <h3>{review.title || 'No Title'}</h3>
                <p>{review.body || 'No Body'}</p>
                <p>
                  <strong>Rating:</strong> {review.rating || 'N/A'} / 5
                </p>
                <p>
                  <strong>Reviewer:</strong> {review.reviewer || 'Anonymous'}
                </p>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default App;
