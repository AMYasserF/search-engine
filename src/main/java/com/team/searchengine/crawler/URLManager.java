/*
 * Keeps track of visited URLs to 
 * prevent duplicate crawling
 */
package com.team.searchengine.crawler;

import org.jsoup.nodes.Document;

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
            System.out.println(url + " has been visited before");
        return visitedUrls.contains(URLUtils.compact(url));
    }

    public synchronized void markVisited(String url, Document doc) {
        String compact = URLUtils.compact(url);
        if (visitedUrls.add(compact)) {
            if (CrawlerManager.canCrawlMoreAndIncrement()) { // Ensure crawl count is within the limit
                appendVisitedUrlToFile(compact);
                saveCrawledPage(url, doc);
            } else {
                visitedUrls.remove(compact); // Rollback if the limit is exceeded
                System.out.println("Rollback: " + url);
            }
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

    private synchronized void appendVisitedUrlToFile(String url) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(VISITED_URLS_FILE, true))) {
            bw.write(url);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending visited URL: " + e.getMessage());
        }
    }

    private void saveCrawledPage(String url, Document doc) {
        try {
            File dir = new File("crawled_pages");
            if (!dir.exists())
                dir.mkdirs();

            String safeFileName = url.replaceAll("[^a-zA-Z0-9]", "_");
            File file = new File(dir, safeFileName + ".txt");

            String title = doc.title();
            String h1 = doc.select("h1").text();
            String h2 = doc.select("h2").text();
            String h3 = doc.select("h3").text();
            String h4 = doc.select("h4").text();
            String h5 = doc.select("h5").text();
            String h6 = doc.select("h6").text();
            String strong = doc.select("strong, b").text(); // bold and strong
            String body = doc.body().text();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("URL: " + url + "\n");
                writer.write("Title: " + title + "\n");
                writer.write("H1: " + h1 + "\n");
                writer.write("H2: " + h2 + "\n");
                writer.write("H3: " + h3 + "\n");
                writer.write("H4: " + h4 + "\n");
                writer.write("H5: " + h5 + "\n");
                writer.write("H6: " + h6 + "\n");
                writer.write("Strong: " + strong + "\n");
                writer.write("Body: " + body + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving crawled page for URL: " + url);
        }
    }
}
