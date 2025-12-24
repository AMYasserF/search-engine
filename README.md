# Query Snippet Seeker – Search Engine

A full-stack web search engine built from scratch. The system includes a multi-threaded web crawler, an inverted indexer using MongoDB, a ranked query processor, and a modern React-based user interface.

---

## 🚀 Features

- **Multi-threaded Crawler**: Efficiently crawls web pages starting from seed URLs  
- **Indexer**: Parses crawled content and builds an inverted index stored in MongoDB  
- **Search & Ranking**:
  - Supports single-word and phrase searching (e.g., `"search term"`)
  - Returns ranked results using TF-IDF and relevance scoring
  - Provides context snippets with highlighted query terms
- **Autocomplete**: Real-time query suggestions based on search history  
- **Modern UI**: React + Vite frontend with Dark Mode support  

---

## 🛠️ Tech Stack

### Backend (Java)
- **Language**: Java 17  
- **Build Tool**: Maven  
- **Web Framework**: Javalin (Port 7070)  
- **Database**: MongoDB (`mongodb-driver-sync`)  
- **Libraries**:
  - Jsoup – HTML parsing
  - Lucene Analyzers – text processing
  - Jackson – JSON serialization/deserialization

### Frontend (JavaScript)
- **Framework**: React 19  
- **Build Tool**: Vite  
- **Styling**: CSS (Dark Mode supported)  
- **Routing**: React Router DOM  

---

## 📋 Prerequisites

Ensure the following are installed:

- Java JDK 17+  
- Maven  
- Node.js & npm  
- MongoDB (running locally on port `27017`)  

---

## ⚙️ Installation & Setup

### 1️⃣ Clone the Repository

```bash
git clone <repository-url>
cd search-engine
```

### 2️⃣ Backend Setup

Navigate to the directory containing `pom.xml` and build the project:

```bash
mvn clean install
```

### 3️⃣ Frontend Setup

Navigate to the frontend directory and install dependencies:

```bash
cd fronted
npm install
```

---

## 🏃 Usage Guide

The search engine operates in three phases: **Crawling**, **Indexing**, and **Searching**.

### 🔹 Step 1: Web Crawling

Run the crawler with the desired number of threads, maximum pages, and a seed file.

```bash
java -cp target/search-engine-1.0-SNAPSHOT-jar-with-dependencies.jar com.team.searchengine.crawler.Crawler <threads> <maxPages> <seedFile>
```

**Example:**

```bash
java -cp target/search-engine-1.0-SNAPSHOT-jar-with-dependencies.jar com.team.searchengine.crawler.Crawler 10 500 seeds.txt
```

Crawled pages are stored in the `crawled_pages/` directory.

> **Note:** The crawler saves its state on shutdown (`Ctrl + C`).

---

### 🔹 Step 2: Indexing

Parse crawled files and populate MongoDB:

```bash
java -cp target/search-engine-1.0-SNAPSHOT-jar-with-dependencies.jar com.team.searchengine.indexer.Indexer
```

- Reads `.txt` files from `crawled_pages/`
- Clears existing MongoDB collections before indexing

---

### 🔹 Step 3: Start the Search Server

```bash
java -cp target/search-engine-1.0-SNAPSHOT-jar-with-dependencies.jar com.team.searchengine.SearchServer
```

Backend API runs at: **http://localhost:7070**

---

### 🔹 Step 4: Run the Frontend

```bash
cd fronted
npm run dev
```

Open your browser at **http://localhost:5173** to use the search engine.

---

## 🔌 API Endpoints

### `GET /search`
Performs a search query.

**Query Parameters:**
- `q` – search query (string)
- `page` – page number (default: 1)
- `size` – results per page (default: 10)

**Response:**  
JSON object containing search time, total results, and ranked result entries.

---

### `GET /suggest`
Provides autocomplete suggestions.

**Query Parameters:**
- `q` – prefix string

**Response:**  
JSON list of the top 5 most frequent queries matching the prefix.

---

## 📂 Project Structure

```text
search-engine/
├── src/main/java/com/team/searchengine/
│   ├── crawler/                # Web crawling logic
│   ├── indexer/                # Indexing and MongoDB integration
│   ├── RankerQueryProcessor/   # Query processing, ranking, stemming
│   └── SearchServer.java       # Backend API entry point
├── fronted/
│   ├── src/
│   │   ├── components/         # React UI components
│   │   ├── assets/             # Images and icons
│   │   └── App.jsx             # Main React application
│   └── package.json
├── crawled_pages/              # Downloaded web pages
├── seeds.txt                   # Seed URLs for crawler
└── pom.xml                     # Maven configuration
```
