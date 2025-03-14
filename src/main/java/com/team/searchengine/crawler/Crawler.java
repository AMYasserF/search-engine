/*
 * The main entry point for the web crawler
 * Sets the maximum number of pages to crawl
 * Initializes sedd URLs and starts crawling process
 */
package com.team.searchengine.crawler;

import java.util.Arrays;
import java.util.List;

public class Crawler {

    public static void main(String[] args) {

        List<String> seedUrls = Arrays.asList("https://www.britannica.com/animal/cat");
        int threadCount = 100;
        int maxPages = 100;
        CrawlerManager manager = new CrawlerManager(seedUrls, threadCount, maxPages);
        manager.startCrawling();

    }
}