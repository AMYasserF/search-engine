package com.team.searchengine;
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
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost(); // Allow all or specify allowed host (e.g., "http://localhost:3000")
                });
            });
        }).before(ctx -> {
            ctx.header("Content-Type", "application/json");
        }).start(7070);

        // Search Endpoint with Pagination
        app.get("/search", ctx -> {
            String query = ctx.queryParam("q");
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);

            long startTime = System.currentTimeMillis();

            if (query == null || query.isBlank()) {
                ctx.status(400).result("Query parameter 'q' is required.");
                return;
            }

            // Update query history for suggestion feature
            queryHistory.put(query, queryHistory.getOrDefault(query, 0) + 1);

            List<ResultDTO> results = new ArrayList<>();
            if (query.startsWith("\"") && query.endsWith("\"")) {
                Set<String> phraseResults = processor.phraseHandler.handlePhraseQuery(query);
                List<String> paginated = paginateList(new ArrayList<>(phraseResults), page, size);

                for (String url : paginated) {
                    results.add(new ResultDTO("Phrase Match", url, "Matched phrase in document."));
                }
            } else {
                List<String> wordIDs = processor.search(query);
                List<wordResult> wordResults = MongoResultBuilder.getWordResults(wordIDs, processor.invertedIndex);
                ArrayList<wordResult> resultsArrayList = new ArrayList<>(wordResults);

                ranker myRanker = new ranker();
                myRanker.score(resultsArrayList, (ArrayList<String>) wordIDs);
                myRanker.sortByValue();
                List<rankerresults> sortedLinks = myRanker.setresults();

                // Apply pagination
                List<rankerresults> paginated = paginateList(sortedLinks, page, size);

                for (rankerresults result : paginated) {
                    String snippet = getSnippet(result.description, query);
                    results.add(new ResultDTO(result.title, result.link, snippet));
                }
            }

            long endTime = System.currentTimeMillis();
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("page", page);
            response.put("size", size);
            response.put("time", (endTime - startTime) / 1000.0 + " seconds");
            response.put("total", results.size());  // Include total results count
            response.put("results", results);

            ctx.json(response);
        });

        // Suggestion Endpoint
        app.get("/suggest", ctx -> {
            String prefix = ctx.queryParam("q");
            if (prefix == null || prefix.isBlank()) {
                ctx.json(Collections.emptyList());
                return;
            }

            List<String> suggestions = queryHistory.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().startsWith(prefix.toLowerCase()))
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();

            ctx.json(suggestions);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(processor::close));
    }

    // Generic pagination method
    private static <T> List<T> paginateList(List<T> fullList, int page, int size) {
        int fromIndex = Math.max(0, (page - 1) * size);
        int toIndex = Math.min(fullList.size(), fromIndex + size);
        if (fromIndex >= toIndex) return Collections.emptyList();
        return fullList.subList(fromIndex, toIndex);
    }

    private static String getSnippet(String text, String keyword) {
        String[] words = text.split("\\s+");
        List<String> snippet = new ArrayList<>();
        keyword = keyword.toLowerCase();

        for (int i = 0; i < words.length; i++) {
            if (words[i].toLowerCase().contains(keyword)) {
                words[i] = words[i].replaceAll("(?i)" + keyword, "<b>" + keyword + "</b>");
                int start = Math.max(0, i - 3);
                int end = Math.min(words.length, i + 6);
                snippet.addAll(Arrays.asList(words).subList(start, end));
                break;
            }
        }

        return String.join(" ", snippet);
    }

    public record ResultDTO(String title, String url, String snippet) {}
}
