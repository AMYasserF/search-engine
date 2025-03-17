/*
 * Keeps track of visited URLs to 
 * prevent duplicate crawling
 */
package com.team.searchengine.crawler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class URLManager {
    private final Set<String> visitedUrls;
    private static final String VISITED_URLS_FILE = "visited_urls.txt";

    public URLManager() {
        this.visitedUrls = new HashSet<>();
        loadVisitedUrls();
    }

    public boolean isVisited(String url) {
        return visitedUrls.contains(url);
    }

    public void markVisited(String url) {
        visitedUrls.add(url);
        saveVisitedUrls();
    }

    private void loadVisitedUrls() {
        File file = new File(VISITED_URLS_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                visitedUrls.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error loading visited URLs: " + e.getMessage());
        }
    }

    private void saveVisitedUrls() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(VISITED_URLS_FILE))) {
            for (String url : visitedUrls) {
                bw.write(url);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving visited URLs: " + e.getMessage());
        }
    }
}

class RobotsTxtManager {
    private final Set<String> disallowedPaths = new HashSet<>();

    public boolean canCrawl(String url) {
        try {
            URL urlObj = new URL(url);
            String robotsTxtUrl = urlObj.getProtocol() + "://" + urlObj.getHost() + "/robots.txt";
            
            HttpURLConnection connection = (HttpURLConnection) new URL(robotsTxtUrl).openConnection();
            connection.setRequestMethod("GET");
            
            if (connection.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("Disallow:")) {
                            String[] parts = line.split(":", 2);
                            if (parts.length > 1) {  // Check if there are at least 2 parts
                                String disallowedPath = parts[1].trim();
                                disallowedPaths.add(disallowedPath);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error fetching robots.txt: " + e.getMessage());
        }


        System.out.println("Checking robots.txt for: " + url);
        System.out.println("Allowed to crawl: " + !isDisallowed(url));

        return !isDisallowed(url);
    }

    private boolean isDisallowed(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            for (String disallowed : disallowedPaths) {
                if (path.startsWith(disallowed)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
