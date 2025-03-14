/*
 * Keeps track of visited URLs to 
 * prevent duplicate crawling
 */
package com.team.searchengine.crawler;

import java.util.HashSet;
import java.util.Set;

public class URLManager {
    private final Set<String> visitedUrls;

    public URLManager() {
        this.visitedUrls = new HashSet<>();
    }

    public boolean isVisited(String url) {
        return visitedUrls.contains(url);
    }

    public void markVisited(String url) {
        visitedUrls.add(url);
    }

}