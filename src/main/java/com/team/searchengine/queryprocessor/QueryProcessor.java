package com.team.searchengine.queryprocessor;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

public class QueryProcessor {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> invertedIndex;

    public QueryProcessor() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("searchengine");
        invertedIndex = database.getCollection("inverted_index");
    }

    public void search(String query) {
        String[] processedWords = Preprocessor.preprocess(query);

        Map<String, Double> docScores = new HashMap<>();

        for (String word : processedWords) {
            // Search original word (higher weight)
            findAndScore(word, 1.0, docScores);

            // Search stemmed version (lower weight)
            String stemmed = Stemmer.stem(word);
            if (!stemmed.equals(word)) {
                findAndScore(stemmed, 0.5, docScores);
            }
        }

        if (docScores.isEmpty()) {
            System.out.println("No documents found for your query.");
        } else {
            // Sort and display
            docScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> System.out.println("Document: " + entry.getKey() + ", Score: " + entry.getValue()));
        }
    }

    private void findAndScore(String word, double weight, Map<String, Double> docScores) {
        Document doc = invertedIndex.find(new Document("word", word)).first();
        if (doc != null) {
            List<Document> postings = (List<Document>) doc.get("postings");
            for (Document posting : postings) {
                String url = posting.getString("url");
                int score = posting.getInteger("score", 1);
                docScores.put(url, docScores.getOrDefault(url, 0.0) + score * weight);
            }
        }
    }

    public void close() {
        mongoClient.close();
    }

    public static void main(String[] args) {
        QueryProcessor processor = new QueryProcessor();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your search query: ");
        String input = scanner.nextLine();
        processor.search(input);
        processor.close();
    }
}
