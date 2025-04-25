package com.team.searchengine.indexer;

import java.io.*;
import java.util.*;

public class DocumentParser {
    public static void parse(File file, InvertedIndex index) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String url = br.readLine().split(":", 2)[1].trim();
            String title = br.readLine().split(":", 2)[1].trim();
            String headers = br.readLine().split(":", 2)[1].trim();
            String body = br.readLine().split(":", 2)[1].trim();

            index.addDocument(url, title, headers, body);
        } catch (Exception e) {
            System.err.println("Failed to parse: " + file.getName());
        }
    }
}
