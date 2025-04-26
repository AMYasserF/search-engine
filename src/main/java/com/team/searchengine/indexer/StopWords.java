package com.team.searchengine.indexer;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class StopWords {
    private static final Set<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "in", "on", "at", "to",
            "of", "for", "by", "with", "about", "against", "between", "into",
            "through", "during", "before", "after", "above", "below", "from",
            "up", "down", "out", "over", "under", "again", "further", "then",
            "once", "here", "there", "when", "where", "why", "how", "all", "any",
            "both", "each", "few", "more", "most", "other", "some", "such", "no",
            "nor", "not", "only", "own", "same", "so", "than", "too", "very",
            "can", "will", "just", "don't", "should", "now"));

    public static boolean isStopWord(String word) {
        return stopWords.contains(word.toLowerCase());
    }
}
