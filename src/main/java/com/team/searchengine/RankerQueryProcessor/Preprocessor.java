package com.team.searchengine.RankerQueryProcessor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Preprocessor {
    public static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "is", "at", "which", "on", "and", "a", "an", "for", "to", "of", "in", "that", "by", "with", "as"));

    public static String[] preprocess(String query) {
        query = query.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        String[] words = query.split("\\s+");
        return Arrays.stream(words)
                .filter(word -> !STOP_WORDS.contains(word))
                .toArray(String[]::new);
    }
}
