import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './SearchResults.css';

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

export default function SearchResults() {
  const query     = useQuery().get('q') || '';
  const pageParam = parseInt(useQuery().get('page') || '1', 10);
  const [results, setResults] = useState([]);
  const [info,    setInfo]    = useState({ time: 0, total: 0, size: 10 });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!query) return;
    setLoading(true);
    axios.get(`http://localhost:7070/search?q=${encodeURIComponent(query)}&page=${pageParam}&size=10`)
      .then(res => {
        setResults(res.data.results || []);
        setInfo({ time: res.data.time, total: res.data.total, size: res.data.size });
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  }, [query, pageParam]);

  const totalPages = Math.ceil(info.total / info.size);
  const goPage     = p => { if (p >= 1 && p <= totalPages) navigate(`?q=${encodeURIComponent(query)}&page=${p}`); };

  const getDomain = url => {
    try { return new URL(url).hostname.replace(/^www\./, ''); }
    catch { return ''; }
  };

  return (
    <div className="results-container">
      <h2 className="results-header">Results for “{query}”</h2>
      {loading ? (
        <p>Loading…</p>
      ) : results.length === 0 ? (
        <p>No results found.</p>
      ) : (
        <>
          <p className="results-info">{info.total} results in {info.time}</p>
          {results.map((r, i) => {
            const domain = getDomain(r.url);
            return (
              <div key={i} className="result-item">
                <div className="result-title">
                  {r.title}{domain && <> | {domain}</>}
                </div>
                <div className="result-url">
                  URL: <a href={r.url} target="_blank" rel="noreferrer">{r.url}</a>
                </div>
                <div className="result-snippet" dangerouslySetInnerHTML={{ __html: r.snippet }} />
              </div>
            );
          })}
          <div className="pagination">
            <button onClick={() => goPage(pageParam - 1)} disabled={pageParam === 1}>Previous</button>
            {Array.from({ length: totalPages }, (_, idx) => (
              <button key={idx} onClick={() => goPage(idx + 1)} className={pageParam === idx + 1 ? 'active' : ''}>
                {idx + 1}
              </button>
            ))}
            <button onClick={() => goPage(pageParam + 1)} disabled={pageParam === totalPages}>Next</button>
          </div>
        </>
      )}
    </div>
  );
}
