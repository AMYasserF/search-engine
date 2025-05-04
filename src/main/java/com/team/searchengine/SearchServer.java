package com.team.searchengine;
import org.bson.Document;

import java.util.stream.Collectors;

import io.javalin.Javalin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.team.searchengine.RankerQueryProcessor.MongoResultBuilder;
import com.team.searchengine.RankerQueryProcessor.wordResult;
import com.team.searchengine.RankerQueryProcessor.ranker;
import com.team.searchengine.RankerQueryProcessor.rankerresults;    
import com.team.searchengine.RankerQueryProcessor.QueryProcessor;

public class SearchServer {

    // Store popular queries for suggestion feature
    private static final Map<String, Integer> queryHistory = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        QueryProcessor processor = new QueryProcessor();

        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        }).before(ctx -> ctx.header("Content-Type", "application/json"))
          .start(7070);

        // Search Endpoint with Pagination
        app.get("/search", ctx -> {
            String query = ctx.queryParam("q");
            int page  = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int size  = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);

            long startTime = System.currentTimeMillis();

            if (query == null || query.isBlank()) {
                ctx.status(400).result("Query parameter 'q' is required.");
                return;
            }

            queryHistory.put(query, queryHistory.getOrDefault(query, 0) + 1);

            List<ResultDTO> results = new ArrayList<>();
            int totalCount;

            if (query.startsWith("\"") && query.endsWith("\"")) {
                Set<String> phraseResults = processor.phraseHandler.handlePhraseQuery(query);
                totalCount = phraseResults.size();

                List<String> pageSlice = paginateList(new ArrayList<>(phraseResults), page, size);
                for (String url : pageSlice) {
                    Document doc = processor.documents.find(new Document("url", url)).first();
                    Optional.ofNullable(doc).ifPresent(d -> {
                        String title = d.getString("title");
                        String snippet = d.getString("body"); // Assuming body contains the main content
                        results.add(new ResultDTO(title, url, getTwentyWordSnippetpharse(snippet, query)));
                    });
                    
                    if (results.isEmpty()) {
                        results.add(new ResultDTO("No results found", url, "No snippet available."));
                    } else {
                        // Highlight the matched phrase in the snippet
                        String highlightedSnippet = getTwentyWordSnippet(results.get(0).snippet, query);
                        results.set(0, new ResultDTO(results.get(0).title, url, highlightedSnippet));
                    }
                }
                
                

                // 2) for each URL load full doc to get title+content
                
            } else {
                List<String> wordIDs       = processor.search(query);
                List<wordResult> wrs       = MongoResultBuilder.getWordResults(wordIDs, processor.invertedIndex);
                ArrayList<wordResult> list = new ArrayList<>(wrs);

                ranker myRanker = new ranker();
                myRanker.score(list, (ArrayList<String>) wordIDs);
                myRanker.sortByValue();
                List<rankerresults> sortedLinks = myRanker.setresults();

                totalCount = sortedLinks.size();

                List<rankerresults> pageSlice = paginateList(sortedLinks, page, size);
                for (rankerresults r : pageSlice) {
                    String snippet = getTwentyWordSnippet(r.description, query);
                    results.add(new ResultDTO(r.title, r.link, snippet));
                }
            }

            long endTime = System.currentTimeMillis();

            Map<String, Object> resp = new HashMap<>();
            resp.put("query", query);
            resp.put("page",  page);
            resp.put("size",  size);
            resp.put("time",  (endTime - startTime)/1000.0 + " seconds");
            resp.put("total", totalCount);
            resp.put("results", results);

            ctx.json(resp);
        });

        app.get("/suggest", ctx -> {
            String prefix = ctx.queryParam("q");
            if (prefix == null || prefix.isBlank()) {
                ctx.json(Collections.emptyList());
                return;
            }
            List<String> suggestions = queryHistory.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().startsWith(prefix.toLowerCase()))
                .sorted((a,b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
            ctx.json(suggestions);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(processor::close));
    }

    private static <T> List<T> paginateList(List<T> fullList, int page, int size) {
        int from = Math.max(0, (page - 1)*size);
        int to   = Math.min(fullList.size(), from + size);
        if (from >= to) return Collections.emptyList();
        return fullList.subList(from, to);
    }
    public static String getTwentyWordSnippetpharse(String body, String query) {
        if (body == null || body.isEmpty() || query == null) return "";
    
        // Remove quotes from phrase query
        String cleanQuery = query.replaceAll("^\"|\"$", "").toLowerCase();
        String[] queryWords = cleanQuery.split("\\s+");
    
        // Split body into words, keeping original words for final output
        String[] bodyWords = body.split("\\s+");
        List<String> lowerWords = Arrays.stream(bodyWords)
                .map(w -> w.toLowerCase().replaceAll("[^a-z0-9]", ""))
                .collect(Collectors.toList());
    
        // Search for phrase
        for (int i = 0; i <= lowerWords.size() - queryWords.length; i++) {
            boolean match = true;
            for (int j = 0; j < queryWords.length; j++) {
                if (!lowerWords.get(i + j).equals(queryWords[j])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                // Get snippet around matched phrase
                int start = Math.max(0, i - 10);
                int end = Math.min(bodyWords.length, i + queryWords.length + 10);
    
                StringBuilder snippet = new StringBuilder();
                for (int k = start; k < end; k++) {
                    // Highlight matching phrase
                    String word = bodyWords[k];
                    String wordClean = lowerWords.get(k);
                    if (k >= i && k < i + queryWords.length) {
                        snippet.append("<b>").append(word).append("</b> ");
                    } else {
                        snippet.append(word).append(" ");
                    }
                }
                return snippet.toString().trim() + "...";
            }
        }
    
        // If phrase not found, fallback to first 20 words
        return Arrays.stream(bodyWords).limit(20).collect(Collectors.joining(" ")) + "...";
    }
    private static Set<String> getQueryTermsForSnippet(String query) {
        return Arrays.stream(query.replaceAll("\"", "")
                                  .replaceAll("(?i)\\bAND\\b|\\bOR\\b", "")
                                  .split("\\s+"))
                     .filter(term -> !term.equalsIgnoreCase("NOT"))
                     .map(String::toLowerCase)
                     .collect(Collectors.toSet());
    }
    

    private static String getTwentyWordSnippet(String text, String keyword) {
        String[] words = text.split("\\s+");
        String lowerKey = keyword.toLowerCase();

        int idx = -1;
        for (int i = 0; i < words.length; i++) {
            if (words[i].toLowerCase().contains(lowerKey)) {
                idx = i;
                break;
            }
        }

        int snippetSize = 20;
        int start, end;
        if (idx == -1) {
            start = 0;
            end = Math.min(snippetSize, words.length);
        } else {
            int half = snippetSize / 2;
            start = Math.max(0, idx - half);
            end   = Math.min(words.length, start + snippetSize);
            if (end - start < snippetSize && start > 0) {
                start = Math.max(0, end - snippetSize);
            }
        }

        for (int i = start; i < end; i++) {
            if (words[i].toLowerCase().contains(lowerKey)) {
                words[i] = words[i].replaceAll("(?i)" + keyword, "<b>" + keyword + "</b>");
            }
        }

        return String.join(" ", Arrays.copyOfRange(words, start, end));
    }

    private static String truncateSnippet(String text, String keyword) {
        String[] words = text.split("\\s+");
        int end = Math.min(20, words.length);
        return String.join(" ", Arrays.copyOfRange(words, 0, end));
    }

    public record ResultDTO(String title, String url, String snippet) {}
}
