import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { FaSearch } from 'react-icons/fa';
import './SearchBar.css';

function SearchBar() {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [highlightIndex, setHighlightIndex] = useState(-1);

  const navigate = useNavigate();
  const inputRef = useRef(null);

  useEffect(() => {
    if (query.trim()) {
      const fetchSuggestions = async () => {
        try {
          const response = await axios.get(`http://localhost:7070/suggest?q=${encodeURIComponent(query)}`);
          console.log("Suggestions:", response.data); // Log the suggestions received
          setSuggestions(response.data || []);
          setShowSuggestions(true);
        } catch (error) {
          console.error('Error fetching suggestions:', error);
          setSuggestions([]);
        }
      };

      const debounce = setTimeout(fetchSuggestions, 300);
      return () => clearTimeout(debounce);
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
    }
  }, [query]);

  const handleSearch = (e) => {
    e.preventDefault();
    const searchQuery = highlightIndex >= 0 ? suggestions[highlightIndex] : query;
    if (!searchQuery.trim()) return;
    navigate(`/results?q=${encodeURIComponent(searchQuery)}`);
  };

  const handleSuggestionClick = (suggestion) => {
    setQuery(suggestion);
    setShowSuggestions(false);
    navigate(`/results?q=${encodeURIComponent(suggestion)}`);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'ArrowDown') {
      setHighlightIndex((prev) => Math.min(prev + 1, suggestions.length - 1));
    } else if (e.key === 'ArrowUp') {
      setHighlightIndex((prev) => Math.max(prev - 1, 0));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (highlightIndex >= 0) {
        handleSuggestionClick(suggestions[highlightIndex]);
      } else {
        handleSearch(e);
      }
    }
  };

  return (
    <div className="google-search-container">
      <form onSubmit={handleSearch} className="google-search-form">
        <div className="search-input-wrapper">
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setHighlightIndex(-1);
            }}
            onKeyDown={handleKeyDown}
            placeholder="Search Google or type a URL"
            onFocus={() => query && setShowSuggestions(true)}
            className="google-search-input"
            autoComplete="off"
          />
          <button type="submit" aria-label="Search" className="search-icon-button">
            <FaSearch />
          </button>
        </div>
        {showSuggestions && suggestions.length > 0 && (
          <ul className="google-suggestion-list">
            {suggestions.slice(0, 5).map((s, idx) => (
              <li
                key={idx}
                className={`google-suggestion-item ${idx === highlightIndex ? 'highlighted' : ''}`}
                onMouseDown={() => handleSuggestionClick(s)}
              >
                {s}
              </li>
            ))}
          </ul>
        )}
      </form>
    </div>
  );
}

export default SearchBar;
