package com.team.searchengine.RankerQueryProcessor;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

public class PhraseSearchHandler {
    public final MongoCollection<Document> documentsCollection;

    public PhraseSearchHandler(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("searchengine");
        this.documentsCollection = database.getCollection("documents");
    }

    public Set<String> searchPhrase(String phrase) {
        Set<String> result = new HashSet<>();
        List<String> phraseTokens = Arrays.asList(phrase.toLowerCase().split("\\s+"));

        FindIterable<Document> docs = documentsCollection.find();

        for (Document doc : docs) {
            StringBuilder allText = new StringBuilder();
            for (String field : Arrays.asList("title", "h1", "h2", "h3", "h4", "h5", "h6", "strong", "body")) {
                if (doc.getString(field) != null) {
                    allText.append(doc.getString(field)).append(" ");
                }
            }

            String[] docWords = allText.toString()
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")  // remove punctuation
                .split("\\s+");

            if (containsExactPhrase(docWords, phraseTokens)) {
                result.add(doc.getString("url"));
            }
        }

        return result;
    }

    private boolean containsExactPhrase(String[] docWords, List<String> phraseTokens) {
        for (int i = 0; i <= docWords.length - phraseTokens.size(); i++) {
            boolean match = true;
            for (int j = 0; j < phraseTokens.size(); j++) {
                if (!docWords[i + j].equals(phraseTokens.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
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

            switch (op) {
                case "AND":
                    result.retainAll(nextResult);
                    break;
                case "OR":
                    result.addAll(nextResult);
                    break;
                case "NOT":
                    result.removeAll(nextResult);
                    break;
            }
        }

        return result;
    }
}
