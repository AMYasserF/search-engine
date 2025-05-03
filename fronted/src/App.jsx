import React, { useState } from 'react';
import SearchBar from './components/SearchBar';
import './App.css';
import logo from './assets/data-searching (1).png';
function App() {
  const [darkMode, setDarkMode] = useState(false);

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  return (
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
        <SearchBar />
      </div>
    </div>
  );
}

export default App;
