import React, { useEffect, useState, useCallback } from 'react';
import { useLocation } from 'react-router-dom';
import axios from 'axios';

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

function SearchResults() {
  const query = useQuery().get('q');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [info, setInfo] = useState({ time: 0, total: 0 });

  useEffect(() => {
    if (query) {
      const fetchResults = async () => {
        setLoading(true);
        try {
          const response = await axios.get(`http://localhost:7070/search?q=${encodeURIComponent(query)}`);
          setResults(response.data.results || []);
          setInfo({ time: response.data.time, total: response.data.total });
        } catch (err) {
          console.error(err);
          setResults([]);
        } finally {
          setLoading(false);
        }
      };
      fetchResults();
    }
  }, [query]);

  const highlight = useCallback((text, term) => {
    if (!term) return text;
    const regex = new RegExp(`(${term.split(' ').join('|')})`, 'gi');
    return text.split(regex).map((part, i) =>
      regex.test(part) ? <span key={i} className="font-bold bg-yellow-100">{part}</span> : part
    );
  }, []);

  return (
    <div className="max-w-2xl mx-auto my-10 px-4">
      <h2 className="text-2xl mb-4">Search results for "<strong>{query}</strong>"</h2>
      {loading ? (
        <p>Loading...</p>
      ) : results.length === 0 ? (
        <p>No results found.</p>
      ) : (
        <>
          <p className="text-sm text-gray-600 mb-4">
            {info.total} results found in {info.time}.
          </p>
          {results.map((result, idx) => (
            <div key={idx} className="mb-6">
              <a
                href={result.url}
                target="_blank"
                rel="noreferrer"
                className="text-lg text-blue-600 hover:underline"
              >
                {highlight(result.title, query)}
              </a>
              <p>{highlight(result.snippet, query)}</p>
            </div>
          ))}
        </>
      )}
    </div>
  );
}

export default SearchResults;
