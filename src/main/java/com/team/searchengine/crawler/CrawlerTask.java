/*
 * Represents a single crawling thread.
 * Fetches the HTML content of a given URL,
 * extaracts new links and adds them to the queue if they haven't been visited
 */
package com.team.searchengine.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.team.searchengine.crawler.RobotsTxtManager;
import com.team.searchengine.crawler.URLUtils;

import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class CrawlerTask implements Runnable {
    private final ConcurrentLinkedQueue<String> urlQueue;
    private final URLManager urlManager;
    private final CrawlerManager manager;
    private final RobotsTxtManager robotsTxtManager;

    public CrawlerTask(ConcurrentLinkedQueue<String> urlQueue, URLManager urlManager, CrawlerManager manager) {
        this.urlQueue = urlQueue;
        this.urlManager = urlManager;
        this.manager = manager;
        this.robotsTxtManager = new RobotsTxtManager();
    }

    @Override
    public void run() {

        while (CrawlerManager.canCrawlMore()) {

            String url = urlQueue.poll();

            if (url == null) {
                System.out.println(
                        "Queue is empty. No more URLs to crawl. Exiting thread " + Thread.currentThread().getName());
                break; // Exit the loop and thread
            }
            if (urlManager.isVisited(url) || !robotsTxtManager.canCrawl(url)) {
                System.out.println("URL not allowed or already visited: " + url);
                continue;
            }
            try {
                System.out.println("Crawling: " + url);

                Document doc = Jsoup.connect(url).get();
                urlManager.markVisited(url, doc); // Pass the Document object to markVisited

                // Extract and add new links to the queue
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String nextUrl = URLUtils.normalizeUrl(link.absUrl("href"));
                    if (!urlManager.isVisited(nextUrl) &&
                            robotsTxtManager.canCrawl(nextUrl) &&
                            URLUtils.isHtmlLink(nextUrl)) {
                        urlQueue.add(nextUrl);
                    }
                }
            } catch (IOException ex) {
                System.err.println("Failed to fetch: " + url);
            }
        }
    }
}
