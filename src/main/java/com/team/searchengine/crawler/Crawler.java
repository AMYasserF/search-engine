/*
 * The main entry point for the web crawler
 * Sets the maximum number of pages to crawl
 * Initializes sedd URLs and starts crawling process
 */
package com.team.searchengine.crawler;

import java.util.List;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Crawler {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println(
                    "Usage: java -cp <jarfile> com.team.searchengine.crawler.Crawler <threadCount> <maxPages> <seedFile>");
            return;
        }

        int threadCount, maxPages;
        try {
            threadCount = Integer.parseInt(args[0]);
            maxPages = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Both threadCount and maxPages must be integers.");
            return;
        }

        List<String> seedUrls;
        try {
            seedUrls = Files.readAllLines(Paths.get(args[2]));
        } catch (IOException e) {
            System.out.println("Failed to read seed URLs from file: " + args[2]);
            return;
        }

        CrawlerManager manager = new CrawlerManager(seedUrls, threadCount, maxPages);
        // Add shutdown hook to save the queue on Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown detected! Saving URL queue...");
            manager.saveUrlQueue();
        }));
        manager.startCrawling();
        manager.saveUrlQueue();
    }
}
