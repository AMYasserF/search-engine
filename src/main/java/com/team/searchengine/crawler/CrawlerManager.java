/*
 *  Manages the entire crawling process including:
 * Trackig visited URLs
 * Handling multithreading
 */
package com.team.searchengine.crawler;

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

public class CrawlerManager {

    private final Queue<String> urlQueue;
    private final List<Thread> threads;
    private final URLManager urlManager;
    private final int threadCount;
    private static int MAX_PAGES;
    private static int pagesCrawled = 0;

    public CrawlerManager(List<String> seedUrls, int threadCount, int maxPages) {
        this.urlQueue = new LinkedList<>(seedUrls);
        this.threads = new LinkedList<>();
        this.urlManager = new URLManager();
        this.threadCount = threadCount;
        MAX_PAGES = maxPages;

    }

    static public synchronized void incrementCrawlCount() {
        pagesCrawled++;
    }

    static public synchronized boolean canCrawlMore() {
        return pagesCrawled < MAX_PAGES;
    }

    public void startCrawling() {
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new CrawlerTask(urlQueue, urlManager));
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                System.err.println("Thread has been interruped");
            }
        }

        System.out.println("Crawling finished!");
    }

}