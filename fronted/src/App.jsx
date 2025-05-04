import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SearchBar from './components/SearchBar';
import SearchResults from './components/SearchResults';
import './App.css';
import logo from './assets/data-searching (1).png';

function App() {
  const [darkMode, setDarkMode] = useState(false);

  const toggleDarkMode = () => setDarkMode(!darkMode);

  return (
    <Router>
      <div className={darkMode ? 'app dark' : 'app'}>
        <div className="dark-mode-container">
          <button onClick={toggleDarkMode} className="dark-mode-toggle">
            {darkMode ? '☀️' : '🌙'}
          </button>
        </div>
        <div className="content">
          <div className="header">
            <img src={logo} alt="query" className="logo" />
            <h1 className="title">Query Snippet Seeker</h1>
          </div>
          <Routes>
            <Route path="/" element={<SearchBar />} />
            <Route path="/results" element={<SearchResults />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
