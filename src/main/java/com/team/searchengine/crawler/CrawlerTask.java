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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import org.bson.BsonDocument;

public class CrawlerTask implements Runnable {
    private final URLManager urlManager;
    private final RobotsTxtManager robotsTxtManager;

    public CrawlerTask(URLManager urlManager) {
        this.urlManager = urlManager;
        this.robotsTxtManager = new RobotsTxtManager();
    }

    @Override
    public void run() {

        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = mongoClient.getDatabase("searchengine");
            MongoCollection<org.bson.Document> pages = db.getCollection("pages", org.bson.Document.class);

            while (CrawlerManager.canCrawlMore()) {
                System.out.println("thread " + Thread.currentThread().getName() + " can crawl");
                String url = CrawlerManager.urlQueue.poll();

                if (url == null) {
                    System.out.println(
                            "Queue is empty. No more URLs to crawl. Exiting thread "
                                    + Thread.currentThread().getName());
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
                    List<String> outlinks = new ArrayList<>();
                    for (Element link : links) {
                        String nextUrl = URLUtils.normalizeUrl(link.absUrl("href"));
                        if (!urlManager.isVisited(nextUrl) &&
                                URLUtils.isHtmlLink(nextUrl)) {
                            CrawlerManager.urlQueue.add(nextUrl);
                            outlinks.add(nextUrl);
                        }
                    }

                    BsonDocument pageDoc = new BsonDocument("url", new org.bson.BsonString(url))
                            .append("outlinks", new org.bson.BsonArray(
                                    outlinks.stream().map(org.bson.BsonString::new).toList()));
                    pages.updateOne(
                            Filters.eq("url", url),
                            new BsonDocument("$set", pageDoc),
                            new UpdateOptions().upsert(true));
                } catch (IOException ex) {
                    System.err.println("Failed to fetch: " + url);
                }
            }
        }
    }
}