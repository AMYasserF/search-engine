/**
 * CrawlerManager.java
 * Manages the entire crawling process including:
 * - Tracking visited URLs
 * - Handling multithreading
 * - Enforcing maximum pages limit
 */
package com.team.searchengine.crawler;

import java.util.List;
import java.util.Queue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerManager {
    private final ConcurrentLinkedQueue<String> urlQueue;
    private final List<Thread> threads;
    private final URLManager urlManager;
    private final int threadCount;
    private static final int maxPages = 1;
    private static final AtomicInteger pagesCrawled = new AtomicInteger(0);
    private static final String QUEUE_FILE = "url_queue.txt";

    public CrawlerManager(List<String> seedUrls, int threadCount, int maxPages) {
        this.urlQueue = loadUrlQueue(seedUrls);
        this.threads = new ArrayList<>();
        this.urlManager = new URLManager();
        this.threadCount = threadCount;
        // this.maxPages = maxPages;
    }

    // Load the queue from file (before crawling starts)
    private ConcurrentLinkedQueue<String> loadUrlQueue(List<String> seedUrls) {
        File file = new File(QUEUE_FILE);
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        if (file.exists()) {

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = br.readLine()) != null) {
                    queue.add(line.trim());
                    System.out.println("add " + line + " to the queue");
                }
            } catch (IOException e) {
                System.err.println("Error loading URL queue: " + e.getMessage());
            }

            if (queue.isEmpty()) {
                System.out.println("add  seed urls to the queue");

                queue.addAll(seedUrls);
                System.out.println("Current queue contents:");
                for (String url : queue) {
                    System.out.println(url);
                }
            }
        } else {
            // First time: add seed URLs
            System.out.println("add  seed urls to the queue");
            queue.addAll(seedUrls);
        }
        return queue;
    }

    // Save the queue to file (after crawling ends)
    public void saveUrlQueue() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(QUEUE_FILE))) {
            for (String url : urlQueue) {
                bw.write(url);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving URL queue: " + e.getMessage());
        }
    }

    public synchronized static boolean canCrawlMoreAndIncrement() {
        if (pagesCrawled.get() < maxPages) {
            pagesCrawled.incrementAndGet();
            return true;
        }
        return false;
    }

    public synchronized static boolean canCrawlMore() {
        return (pagesCrawled.get() < maxPages);
    }

    public void startCrawling() {
        long startTime = System.currentTimeMillis();
        System.out.println("start crawling");

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(new CrawlerTask(urlQueue, urlManager, this), "CrawlerThread-" + i);
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + t.getName());
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Crawling completed in " + (endTime - startTime) / 1000.0 + " seconds.");
        System.out.println("Total pages crawled: " + pagesCrawled.get());

    }
}
