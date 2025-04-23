/*
 * Keeps track of visited URLs to 
 * prevent duplicate crawling
 */
package com.team.searchengine.crawler;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class URLManager {
    private final Set<String> visitedUrls;
    private static final String VISITED_URLS_FILE = "visited_urls.txt";

    public URLManager() {
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        loadVisitedUrls();
    }

    public boolean isVisited(String url) {
        if (visitedUrls.contains(URLUtils.compact(url)))
            System.out.println(url + " have been visited before");
        return visitedUrls.contains(URLUtils.compact(url));
    }

    public synchronized void markVisited(String url) {
        String compact = URLUtils.compact(url);
        if (visitedUrls.add(compact)) {
            appendVisitedUrlToFile(compact);
        }
    }

    private void loadVisitedUrls() {
        File file = new File(VISITED_URLS_FILE);
        if (!file.exists())
            return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                visitedUrls.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error loading visited URLs: " + e.getMessage());
        }
    }

    private void appendVisitedUrlToFile(String url) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(VISITED_URLS_FILE, true))) {
            bw.write(url);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending visited URL: " + e.getMessage());
        }
    }
}
