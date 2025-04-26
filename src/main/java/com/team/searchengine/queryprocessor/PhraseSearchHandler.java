package com.team.searchengine.queryprocessor;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

public class PhraseSearchHandler {
    private final MongoCollection<Document> documentsCollection;

    public PhraseSearchHandler(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("searchengine");
        this.documentsCollection = database.getCollection("documents");
    }

    public Set<String> searchPhrase(String phrase) {
        Set<String> result = new HashSet<>();
        FindIterable<Document> docs = documentsCollection.find();

        for (Document doc : docs) {
            StringBuilder allText = new StringBuilder();
            for (String field : Arrays.asList("title", "h1", "h2", "h3", "h4", "h5", "h6", "strong", "body")) {
                if (doc.getString(field) != null) {
                    allText.append(doc.getString(field)).append(" ");
                }
            }
            if (allText.toString().toLowerCase().contains(phrase.toLowerCase())) {
                result.add(doc.getString("url"));
            }
        }
        return result;
    }

    public Set<String> handlePhraseQuery(String input) {
        List<String> phrases = new ArrayList<>();
        List<String> operations = new ArrayList<>();

        int idx = 0;
        while (idx < input.length()) {
            if (input.charAt(idx) == '"') {
                int end = input.indexOf('"', idx + 1);
                if (end == -1) break;
                phrases.add(input.substring(idx + 1, end).trim());
                idx = end + 1;
            } else if (input.substring(idx).toUpperCase().startsWith("AND")) {
                operations.add("AND");
                idx += 3;
            } else if (input.substring(idx).toUpperCase().startsWith("OR")) {
                operations.add("OR");
                idx += 2;
            } else if (input.substring(idx).toUpperCase().startsWith("NOT")) {
                operations.add("NOT");
                idx += 3;
            } else {
                idx++;
            }
        }

        if (phrases.isEmpty()) return Collections.emptySet();

        Set<String> result = searchPhrase(phrases.get(0));

        for (int i = 0; i < operations.size(); i++) {
            Set<String> nextResult = searchPhrase(phrases.get(i + 1));
            String op = operations.get(i);

            if (op.equals("AND")) {
                result.retainAll(nextResult);
            } else if (op.equals("OR")) {
                result.addAll(nextResult);
            } else if (op.equals("NOT")) {
                result.removeAll(nextResult);
            }
        }

        return result;
    }
}
