package com.team.searchengine.indexer.tokenizer;

import com.team.searchengine.indexer.Language;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Tokenizer {

    private Document htmlDoc;
    private int wordCount;
    private HashMap<String, Token> tokenDictionary;
    private RandomAccessFile fileWriter;

    public Tokenizer(String htmlFilePath) {
        try {
            File inputFile = new File(htmlFilePath);
            this.htmlDoc = Jsoup.parse(inputFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse HTML file.", e);
        }
    }

    private int getCurrentFilePosition() {
        try {
            return (int) fileWriter.getFilePointer();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving file position", e);
        }
    }

    private void writeTokenToFile(String token) {
        try {
            fileWriter.writeBytes(token + " ");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write token to file", e);
        }
    }

    private String cleanToken(String token) {
        return token.toLowerCase().replaceAll("[^a-zA-Z]", "");
    }

    private String selectDominantTag(String existingTag, String newTag) {
        return Language.getHtmlScore(newTag) > Language.getHtmlScore(existingTag) ? newTag : existingTag;
    }

    private void processText(String text, String htmlTag) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            int filePosition = getCurrentFilePosition();
            writeTokenToFile(word);

            String cleaned = cleanToken(word);
            if (cleaned.isEmpty() || Language.isStopWord(cleaned)) continue;

            String stemmed = stemToken(cleaned);

            Token tokenEntry = tokenDictionary.get(stemmed);
            if (tokenEntry == null) {
                tokenEntry = new Token(stemmed, htmlTag);
                tokenDictionary.put(stemmed, tokenEntry);
            } else {
                tokenEntry.TF++;
                tokenEntry.html_pos = selectDominantTag(tokenEntry.html_pos, htmlTag);
            }
            tokenEntry.position.add(filePosition);
            wordCount++;
        }
    }

    private void processDOMTree(Node node) {
        if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            processText(textNode.text(), Objects.requireNonNull(node.parent()).nodeName());
        } else {
            for (Node child : node.childNodes()) {
                processDOMTree(child);
            }
        }
    }

    public HashMap<String, Token> tokenizeDocument(String outputFilePath) {
        this.tokenDictionary = new HashMap<>();
        openOutputFile(outputFilePath);

        processText(htmlDoc.title(), "title");
        processDOMTree(htmlDoc.body());

        closeOutputFile();
        return tokenDictionary;
    }

    private void openOutputFile(String path) {
        try {
            this.fileWriter = new RandomAccessFile(path, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to open output file for writing.", e);
        }
    }

    private void closeOutputFile() {
        try {
            this.fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close output file.", e);
        }
    }

    private String stemToken(String token) {
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(token);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public int getWordCount() {
        return this.wordCount;
    }
}
