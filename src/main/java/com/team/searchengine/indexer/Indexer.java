package com.team.searchengine.indexer;

import java.io.File;

public class Indexer {
    public static void main(String[] args) {
        File folder = new File("crawled_pages");
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Crawled pages folder not found!");
            return;
        }

        MongoDBManager dbManager = new MongoDBManager();
        InvertedIndexManager indexManager = new InvertedIndexManager();

        // Clear old data before indexing
        dbManager.clearDocuments();
        indexManager.clearInvertedIndex();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".txt")) {
                DocumentParser.parse(file, dbManager, indexManager);
            }
        }

        System.out.println("Indexing complete!");
        dbManager.close();
        indexManager.close();
    }
}
