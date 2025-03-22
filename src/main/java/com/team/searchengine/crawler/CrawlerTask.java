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
    private final RobotsTxtManager robotsTxtManager;

    public CrawlerTask(Queue<String> urlQueue, URLManager urlManager) {
        this.urlQueue = urlQueue;
        this.urlManager = urlManager;
        this.robotsTxtManager = new RobotsTxtManager();
    }

    @Override
    public void run() {
        while (CrawlerManager.canCrawlMore()) {
            String url;

            //synchronized block ensures only one thread takes a URL at a time
            synchronized (urlQueue) {
                if (urlQueue.isEmpty())
                    break;
                url = urlQueue.poll();
            }

            //check url is in visited_urls.txt or it is disallowed in robot.txt 
            //and print the allowed urls with true and disallwoed urls with false

            if (url == null || urlManager.isVisited(url) || !robotsTxtManager.canCrawl(url))
                continue;

            try {
                System.out.println("Crawling: " + url);
                CrawlerManager.incrementCrawlCount();

                //JSoup connects to the URL.
                //Fetches the HTML document of the webpage.
                Document doc = Jsoup.connect(url).get();

                //save allowed urls in set visitedUrls and re-overwrite the whole visited_urls.txt
                urlManager.markVisited(url);

                //get all hyper-links
                Elements links = doc.select("a[href]");
                synchronized (urlQueue) {
                    for (Element link : links) {

            //converts relative urls like /page1 into absolute urls https://example.com/page1

                        String nextUrl = link.absUrl("href");
                        //add all valid hyper-links to urlQueue
                        if (!urlManager.isVisited(nextUrl) && robotsTxtManager.canCrawl(nextUrl)) {
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
