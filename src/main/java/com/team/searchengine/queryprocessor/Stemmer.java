package com.team.searchengine.queryprocessor;

import org.tartarus.snowball.ext.PorterStemmer;

public class Stemmer {
    private static final PorterStemmer stemmer = new PorterStemmer();

    public static String stem(String word) {
        stemmer.setCurrent(word);
        if (stemmer.stem()) {
            return stemmer.getCurrent();
        }
        return word;
    }
}
