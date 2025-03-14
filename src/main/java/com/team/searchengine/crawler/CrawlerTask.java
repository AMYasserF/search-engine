/*
 * Represents a single crawling thread.
 * Fetches the HTML content of a given URL,
 * extaracts new links and adds them to the queue if they haven't been visited
 */
package com.team.searchengine.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.Queue;

public class CrawlerTask implements Runnable {

    private final Queue<String> urlQueue;
    private final URLManager urlManager;

    public CrawlerTask(Queue<String> urlQueue, URLManager urlManager) {
        this.urlQueue = urlQueue;
        this.urlManager = urlManager;
    }

    @Override
    public void run() {
        while (CrawlerManager.canCrawlMore()) {
            String url;
            synchronized (urlQueue) {
                if (urlQueue.isEmpty())
                    break;
                url = urlQueue.poll();
            }

            if (url == null || urlManager.isVisited(url))
                continue;

            try {
                System.out.println("Crawling: " + url);
                CrawlerManager.incrementCrawlCount();
                Document doc = Jsoup.connect(url).get();
                urlManager.markVisited(url);

                Elements links = doc.select(("a[href]"));
                synchronized (urlQueue) {
                    for (Element link : links) {
                        String nextUrl = link.absUrl("href");
                        if (!urlManager.isVisited(nextUrl)) {
                            urlQueue.add(nextUrl);
                        }

                    }
                }
            } catch (IOException ex) {
                System.err.println("Failed to fetch: " + url);
            }
        }
    }
}