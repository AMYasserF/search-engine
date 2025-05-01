package com.team.searchengine.crawler;

import java.net.URI;
import java.net.URISyntaxException;

public class URLUtils {

    // For crawling and printing
    public static String normalizeUrl(String url) {
        try {
            URI uri = new URI(url).normalize();
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "https";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase().replaceFirst("^www\\.", "") : "";
            int port = uri.getPort();
            String path = uri.getPath() != null ? uri.getPath() : "";
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);

            return scheme + "://" + host + (port > 0 && port != 80 && port != 443 ? ":" + port : "") + path;

        } catch (URISyntaxException e) {
            return url.trim().toLowerCase();
        }
    }

    public static boolean isHtmlLink(String url) {
        url = url.toLowerCase();
        return url.endsWith(".html") || url.endsWith(".htm")
                || !url.matches(".*\\.(jpg|jpeg|png|gif|pdf|doc|docx|ppt|pptx|xls|xlsx|zip|rar|exe)(\\?.*)?$");
    }

    // For comparison and deduplication (compact version)
    public static String compact(String url) {
        try {
            URI uri = new URI(url).normalize();
            String host = uri.getHost() != null ? uri.getHost().toLowerCase().replaceFirst("^www\\.", "") : "";
            String path = uri.getPath() != null ? uri.getPath() : "";
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);

            return host + path;

        } catch (URISyntaxException e) {
            return url.trim().toLowerCase();
        }
    }

    public static String getDomain(String url) {
        try {
            URI uri = new URI(url).normalize();
            String host = uri.getHost();
            if (host == null)
                return "";
            return host.toLowerCase().replaceFirst("^www\\.", "");
        } catch (URISyntaxException e) {
            return "";
        }
    }
}
