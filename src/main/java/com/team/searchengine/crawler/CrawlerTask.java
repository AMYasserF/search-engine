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
        while (manager.canCrawlMore()) {
            System.out.println("thread " + Thread.currentThread().getName() + "Started");
            String url = urlQueue.poll();

            System.out.println("on url: " + url);
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

                manager.incrementCrawlCount();
                Document doc = Jsoup.connect(url).get();
                urlManager.markVisited(url);
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
