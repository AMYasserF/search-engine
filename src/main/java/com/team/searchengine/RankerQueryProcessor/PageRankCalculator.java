package com.team.searchengine.RankerQueryProcessor;

import java.util.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class PageRankCalculator {

    public static Map<String, Set<String>> buildOutLinksFromDatabase() {
        Map<String, Set<String>> outLinks = new HashMap<>();

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("SearchEngine");
        MongoCollection<Document> pages = database.getCollection("pages"); // assume you store pages separately

        // Fetch documents from the collection
        for (Document doc : pages.find()) {
            // Fetch the list of URLs in this document
            String urls = (String) doc.get("url");

        
            List<String> outgoingLinksList = (List<String>) doc.get("outlinks");


            outLinks.put(urls, new HashSet<>(outgoingLinksList)); 
        }

        mongoClient.close();
        return outLinks;
    }

    public static Map<String, Double> computePageRank(Map<String, Set<String>> outLinks) {
        Map<String, Double> pageRanks = new HashMap<>();
        int numPages = outLinks.size();
        double initialRank = 1.0 / numPages;
        double dampingFactor = 0.85;

        // Initialize PageRank values
        for (String page : outLinks.keySet()) {
            pageRanks.put(page, initialRank);
        }

        // Iterate and update PageRank values
        for (int i = 0; i < 100; i++) { // Arbitrary number of iterations
            Map<String, Double> newPageRanks = new HashMap<>();

            for (String page : outLinks.keySet()) {
                double newRank = (1 - dampingFactor) / numPages;

                // Add rank from incoming links
                for (String linkingPage : outLinks.keySet()) {
                    if (outLinks.get(linkingPage).contains(page)) {
                        int numLinks = outLinks.get(linkingPage).size();
                        newRank += dampingFactor * (pageRanks.get(linkingPage) / numLinks);
                    }
                }

                newPageRanks.put(page, newRank);
            }

            // Update the page ranks
            pageRanks = new HashMap<>(newPageRanks);
        }

        return pageRanks;
    }
}
