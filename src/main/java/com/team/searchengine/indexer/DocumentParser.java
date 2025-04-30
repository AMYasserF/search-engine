package com.team.searchengine.indexer;

import java.io.*;
import java.util.*;

public class DocumentParser {
    public static void parse(File file, MongoDBManager dbManager, InvertedIndexManager indexManager) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String url = "";
            String title = "";
            String h1 = "";
            String h2 = "";
            String h3 = "";
            String h4 = "";
            String h5 = "";
            String h6 = "";
            String strong = "";
            String body = "";

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("URL:")) {
                    url = line.substring(4).trim();
                } else if (line.startsWith("Title:")) {
                    title = line.substring(6).trim();
                } else if (line.startsWith("H1:")) {
                    h1 = line.substring(3).trim();
                } else if (line.startsWith("H2:")) {
                    h2 = line.substring(3).trim();
                } else if (line.startsWith("H3:")) {
                    h3 = line.substring(3).trim();
                } else if (line.startsWith("H4:")) {
                    h4 = line.substring(3).trim();
                } else if (line.startsWith("H5:")) {
                    h5 = line.substring(3).trim();
                } else if (line.startsWith("H6:")) {
                    h6 = line.substring(3).trim();
                } else if (line.startsWith("Strong:")) {
                    strong = line.substring(7).trim();
                } else if (line.startsWith("Body:")) {
                    body = line.substring(5).trim();
                }
            }

            if (!url.isEmpty()) {
                dbManager.saveDocument(url, title, h1, h2, h3, h4, h5, h6, strong, body);
                
                String description = body;
                
                // Step 1: Build word frequency for the whole document
                Map<String, Integer> fullWordFreq = new HashMap<>();
                int totalWords = 0;
                
                totalWords += processSection(fullWordFreq, title);
                totalWords += processSection(fullWordFreq, h1);
                totalWords += processSection(fullWordFreq, h2);
                totalWords += processSection(fullWordFreq, h3 + " " + h4 + " " + h5 + " " + h6);
                totalWords += processSection(fullWordFreq, strong);
                totalWords += processSection(fullWordFreq, body);

                // Step 2: Insert words into inverted index
                for (Map.Entry<String, Integer> entry : fullWordFreq.entrySet()) {
                    String word = entry.getKey();
                    int frequency = entry.getValue();
                    double tf = (double) frequency / totalWords;
                    indexManager.addWord(word, url, title, description, tf);
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to parse file: " + file.getName());
            e.printStackTrace();
        }
    }

    private static int processSection(Map<String, Integer> wordFrequency, String text) {
        int wordCount = 0;
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (!word.isEmpty() && !StopWords.isStopWord(word)) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                wordCount++;
            }
        }
        return wordCount;
    }
}
