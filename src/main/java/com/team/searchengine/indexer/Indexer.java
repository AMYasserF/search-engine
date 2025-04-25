package com.team.searchengine.indexer;

import java.io.File;

public class Indexer {
    public static void main(String[] args) {
        File folder = new File("crawled_pages");
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Crawled pages folder not found!");
            return;
        }

        InvertedIndex index = new InvertedIndex();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".txt")) {
                DocumentParser.parse(file, index);
            }
        }

        System.out.println("Indexing complete!");
        // Save index to disk (to do later)
    }
}
