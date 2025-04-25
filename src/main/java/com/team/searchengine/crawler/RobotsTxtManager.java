package com.team.searchengine.crawler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class RobotsTxtManager {
    private final Map<String, Set<String>> disallowedMap = new HashMap<>();

    public boolean canCrawl(String url) {
        try {
            System.out.println("robots checking on " + url);

            URL urlObj = new URL(url);
            String host = urlObj.getHost();

            // if the host doesn't exist
            if (!disallowedMap.containsKey(host)) {
                fetchAndParseRobotsTxt(urlObj, host);
            }

            // is host exists return disallowed paths if not return empty set "Set.of()"
            boolean allowed = !isDisallowed(urlObj.getPath(), disallowedMap.getOrDefault(host, Set.of()));
            System.out.println("Allowed to crawl: " + allowed);
            return allowed;

        } catch (Exception e) {
            System.err.println("Error checking robots.txt: " + e.getMessage());
            return true;
        }
    }

    private void fetchAndParseRobotsTxt(URL urlObj, String host) {
        Set<String> disallowedPaths = new HashSet<>();
        try {
            String robotsUrl = urlObj.getProtocol() + "://" + host + "/robots.txt";
            // cast into Http connection to allow get method

            HttpURLConnection connection = (HttpURLConnection) new URL(robotsUrl).openConnection();
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000); // 5 seconds
            connection.setRequestMethod("GET");

            // if connection was successful
            if (connection.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("Disallow:")) {
                            String[] parts = line.split(":", 2);
                            if (parts.length > 1) {
                                String disallowedPath = parts[1].trim();
                                if (!disallowedPath.isEmpty()) {
                                    disallowedPaths.add(disallowedPath);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Timeout or connection error fetching robots.txt for: " + host + " - " + e.getMessage());
            // Disallow all crawling for this host
            disallowedPaths.clear();
            disallowedPaths.add("/");
        }

        disallowedMap.put(host, disallowedPaths);
    }

    private boolean isDisallowed(String path, Set<String> disallowedPaths) {
        for (String disallowed : disallowedPaths) {
            if (path.startsWith(disallowed)) {
                return true;
            }
        }
        return false;
    }
}
