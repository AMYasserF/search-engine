package com.team.searchengine.indexer;

import java.io.*;
import java.util.*;

import com.team.searchengine.queryprocessor.Stemmer;

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
                // Save full document fields to the documents collection
                dbManager.saveDocument(url, title, h1, h2, h3, h4, h5, h6, strong, body);

                // Add words to inverted index with different scores based on importance
                addWordsToIndex(title, url, 5, indexManager); // Title is highest importance
                addWordsToIndex(h1, url, 4, indexManager); // H1 tags
                addWordsToIndex(h2, url, 3, indexManager); // H2 tags
                addWordsToIndex(h3 + " " + h4 + " " + h5 + " " + h6, url, 2, indexManager); // H3-H6 combined
                addWordsToIndex(strong, url, 2, indexManager); // <strong> and <b> tags
                addWordsToIndex(body, url, 1, indexManager); // Normal body text
            }

        } catch (IOException e) {
            System.err.println("Failed to parse file: " + file.getName());
            e.printStackTrace();
        }
    }

  private static void addWordsToIndex(String text, String url, int score, InvertedIndexManager indexManager) {
    String[] words = text.toLowerCase().split("\\W+");
    for (String word : words) {
        if (!word.isEmpty() && !StopWords.isStopWord(word)) {
            String stemmedWord = Stemmer.stem(word);  // ⭐ apply stemming here
            indexManager.addWord(stemmedWord, url, score);
        }
    }
}


}
