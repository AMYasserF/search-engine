package com.team.searchengine.queryprocessor;

import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import java.io.IOException;
import java.io.StringReader;

public class Stemmer {
    public static String stem(String word) {
        try {
            // Create a WhitespaceTokenizer with a StringReader
            WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
            tokenizer.setReader(new StringReader(word));

            TokenStream tokenStream = new PorterStemFilter(tokenizer);

            tokenStream.reset();
            String stemmed = word;
            if (tokenStream.incrementToken()) {
                stemmed = tokenStream.getAttribute(CharTermAttribute.class).toString();
            }
            tokenStream.end();
            tokenStream.close();
            return stemmed;
        } catch (IOException e) {
            e.printStackTrace();
            return word; // If error, return original word
        }
    }
}
