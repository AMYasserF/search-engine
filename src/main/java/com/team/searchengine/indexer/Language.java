package com.team.searchengine.indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Language {

    private static final HashMap<String, Boolean> stopWords = new HashMap<>();
    private static final HashMap<String, Integer> scoreDictionary = new HashMap<>();

    public static int getHtmlScore(String pos) {
        return scoreDictionary.getOrDefault(pos, 1);
    }

    public static void initializeHTML(String filePath) {
        try (Scanner reader = new Scanner(new File(filePath))) {
            while (reader.hasNextLine()) {
                String[] scoreEntry = reader.nextLine().split(" ");
                String pos = scoreEntry[0];
                int score = Integer.parseInt(scoreEntry[1]);
                scoreDictionary.put(pos, score);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Can't Find HTML POS Files");
        }
    }

    public static void initializeDictionary(String filePath) {
        try (Scanner reader = new Scanner(new File(filePath))) {
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                stopWords.put(data, true);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Can't Find Stop Words File");
        }
    }

    public static boolean isStopWord(String word) {
        return stopWords.get(word) != null || word.length() < 2;
    }
}
