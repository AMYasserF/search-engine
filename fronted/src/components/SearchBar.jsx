import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

function SearchBar() {
    const [query, setQuery] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchInfo, setSearchInfo] = useState({});

    useEffect(() => {
        if (query.trim()) {
            const fetchSuggestions = async () => {
                try {
                    const response = await axios.get(`http://localhost:7070/suggest?q=${encodeURIComponent(query)}`);
                    setSuggestions(response.data || []);
                    setShowSuggestions(true);
                } catch (error) {
                    console.error('Error fetching suggestions:', error);
                    setSuggestions([]);
                }
            };

            const delayDebounceFn = setTimeout(fetchSuggestions, 300);
            return () => clearTimeout(delayDebounceFn);
        } else {
            setSuggestions([]);
            setShowSuggestions(false);
        }
    }, [query]);

    const handleSearch = async (e) => {
        e.preventDefault();
        if (!query.trim()) return;

        setLoading(true);
        setShowSuggestions(false);

        try {
            const response = await axios.get(`http://localhost:7070/search?q=${encodeURIComponent(query)}`);
            const data = response.data;
            setResults(data.results || []);
            setSearchInfo({ time: data.time, total: data.total });
        } catch (error) {
            console.error('Search error:', error);
            setResults([]);
        } finally {
            setLoading(false);
        }
    };

    const handleSuggestionClick = (suggestion) => {
        setQuery(suggestion);
        setShowSuggestions(false);
        setSuggestions([]);
        // Trigger search
        setTimeout(() => document.getElementById('search-button').click(), 0);
    };

    const highlight = useCallback((text, highlightTerm) => {
        if (!highlightTerm) return text;
        const terms = highlightTerm.split(/\s+/).filter(Boolean);
        if (terms.length === 0) return text;

        const escaped = terms.map(t => t.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'));
        const regex = new RegExp(`(${escaped.join('|')})`, 'gi');

        return text.split(regex).map((part, i) =>
            regex.test(part) ? <span key={i} className="font-bold">{part}</span> : part
        );
    }, []);

    return (
        <div className="max-w-xl mx-auto my-8">
            <form onSubmit={handleSearch} className="flex relative">
                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    className="flex-grow border border-gray-300 px-4 py-2 rounded-l-md focus:outline-none"
                    placeholder="Search..."
                    onFocus={() => query && setShowSuggestions(true)}
                    autoComplete="off"
                />
                <button
                    id="search-button"
                    type="submit"
                    className="bg-blue-500 text-white px-4 py-2 rounded-r-md hover:bg-blue-600"
                >
                    Search
                </button>
            </form>

            {showSuggestions && suggestions.length > 0 && (
                <ul className="absolute z-10 w-full border border-gray-300 rounded-b-md mt-0.5 bg-white max-h-60 overflow-y-auto shadow">
                    {suggestions.slice(0, 5).map((s, index) => (
                        <li
                            key={index}
                            onClick={() => handleSuggestionClick(s)}
                            className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                        >
                            {s}
                        </li>
                    ))}
                </ul>
            )}

            {loading && <div className="mt-4 text-gray-600">Loading...</div>}

            {!loading && results.length > 0 && (
                <div className="mt-4">
                    <div className="text-sm text-gray-600 mb-4">
                        {searchInfo.total} results found in {searchInfo.time}.
                    </div>
                    {results.map((result, idx) => (
                        <div key={idx} className="mb-4">
                            <a
                                href={result.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-xl font-semibold text-blue-600 hover:underline"
                            >
                                {highlight(result.title, query)}
                            </a>
                            <p>{highlight(result.snippet, query)}</p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default SearchBar;
