package com.team.searchengine.crawler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class RobotsTxtManager {
    private static class RuleSet {
        Set<String> disallowed = new HashSet<>();
        Set<String> allowed = new HashSet<>();
    }

    private final Map<String, RuleSet> hostRulesMap = new HashMap<>();

    public boolean canCrawl(String url) {
        try {
            URL urlObj = new URL(url);
            String host = normalizeHost(urlObj.getHost());

            if (!hostRulesMap.containsKey(host)) {
                fetchAndParseRobotsTxt(urlObj.getProtocol(), host);
            }

            RuleSet rules = hostRulesMap.getOrDefault(host, new RuleSet());
            String path = urlObj.getPath();

            // Check allow/disallow with proper priority
            for (String allow : rules.allowed) {
                if (path.startsWith(allow)) {
                    return true;
                }
            }
            for (String disallow : rules.disallowed) {
                if (path.startsWith(disallow)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error in canCrawl(): " + e.getMessage());
            return true; // Default to allowed on error
        }
    }

    private void fetchAndParseRobotsTxt(String protocol, String host) {
        RuleSet ruleSet = new RuleSet();

        try {
            String robotsUrl = protocol + "://" + host + "/robots.txt";
            HttpURLConnection connection = (HttpURLConnection) new URL(robotsUrl).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    boolean relevantUserAgent = false;

                    while ((line = br.readLine()) != null) {
                        line = line.trim();

                        if (line.toLowerCase().startsWith("user-agent:")) {
                            String agent = line.split(":", 2)[1].trim();
                            relevantUserAgent = agent.equals("*");
                        } else if (relevantUserAgent && line.toLowerCase().startsWith("disallow:")) {
                            String value = line.split(":", 2)[1].trim();
                            if (!value.isEmpty()) {
                                ruleSet.disallowed.add(value);
                            }
                        } else if (relevantUserAgent && line.toLowerCase().startsWith("allow:")) {
                            String value = line.split(":", 2)[1].trim();
                            if (!value.isEmpty()) {
                                ruleSet.allowed.add(value);
                            }
                        } else if (line.isEmpty()) {
                            // Reset after section ends
                            relevantUserAgent = false;
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to fetch robots.txt for host: " + host + " - " + e.getMessage());
            // Be permissive if we can’t fetch
        }

        hostRulesMap.put(host, ruleSet);
    }

    private String normalizeHost(String host) {
        if (host == null)
            return "";
        return host.toLowerCase().startsWith("www.") ? host.substring(4).toLowerCase() : host.toLowerCase();
    }
}
