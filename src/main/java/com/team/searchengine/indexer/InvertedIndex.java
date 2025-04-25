package com.team.searchengine.indexer;

import java.util.*;

public class InvertedIndex {
    private final Map<String, Set<String>> index = new HashMap<>();

    public void addDocument(String url, String title, String headers, String body) {
        addWords(title, url);
        addWords(headers, url);
        addWords(body, url);
    }

    private void addWords(String text, String url) {
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (word.isEmpty())
                continue;
            index.computeIfAbsent(word, k -> new HashSet<>()).add(url);
        }
    }

    public Set<String> search(String word) {
        return index.getOrDefault(word.toLowerCase(), Set.of());
    }
}
