/*
 * Keeps track of visited URLs to 
 * prevent duplicate crawling
 */
package com.team.searchengine.crawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class URLManager {
    private final Set<String> visitedUrls;

    public URLManager() {
        this.visitedUrls = new HashSet<>();
    }

    public boolean isVisited(String url) {
        return visitedUrls.contains(normalizeUrl(url));
    }

    public void markVisited(String url) {
        visitedUrls.add(normalizeUrl(url));
    }

    private String normalizeUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath().replaceAll("/$", ""); // Remove trailing slash
            return new URI(uri.getScheme(), uri.getAuthority(), path, null, null).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

}