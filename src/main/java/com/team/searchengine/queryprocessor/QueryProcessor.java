package com.team.searchengine.queryprocessor;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

public class QueryProcessor {
    private final MongoClient mongoClient;
    private final PhraseSearchHandler phraseHandler;

    public QueryProcessor() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        phraseHandler = new PhraseSearchHandler(mongoClient);
    }

    public void search(String query) {
        if (query.contains("\"")) {
            // Phrase search
            Set<String> result = phraseHandler.handlePhraseQuery(query);
            displayResults(result);
        } else {
            System.out.println("Please put phrases inside quotation marks.");
        }
    }

    private void displayResults(Set<String> results) {
        if (results.isEmpty()) {
            System.out.println("No documents found matching your phrase search.");
        } else {
            results.forEach(url -> System.out.println("Document: " + url));
        }
    }

    public void close() {
        mongoClient.close();
    }

    public static void main(String[] args) {
        QueryProcessor processor = new QueryProcessor();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your search query (phrases inside \"quotes\"): ");
        
        String input = scanner.nextLine();
        processor.search(input);
        processor.close();
    }
}
