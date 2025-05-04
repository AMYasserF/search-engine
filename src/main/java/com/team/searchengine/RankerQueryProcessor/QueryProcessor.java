package com.team.searchengine.RankerQueryProcessor;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class QueryProcessor {
    public final MongoClient mongoClient;
    public final PhraseSearchHandler phraseHandler;
    public final MongoDatabase database;
    public final MongoCollection<Document> documents;      

    public final MongoCollection<Document> invertedIndex;

    public QueryProcessor() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("searchengine");
        invertedIndex = database.getCollection("inverted_index");
        documents    = database.getCollection("documents");    

        phraseHandler = new PhraseSearchHandler(mongoClient);
    }

    public List<String> search(String query) {
        String[] processedWords = Preprocessor.preprocess(query);
        Set<String> wordIDs = new HashSet<>();

        for (String word : processedWords) {
            findWordID(word, wordIDs);
            String stemmed = Stemmer.stem(word);
            if (!stemmed.equals(word)) {
                findWordID(stemmed, wordIDs);
            }
        }

        return new ArrayList<>(wordIDs);
    }

    private void findWordID(String word, Set<String> wordIDs) {
        Document doc = invertedIndex.find(new Document("word", word)).first();
        if (doc != null && doc.containsKey("_id")) {
            ObjectId id = doc.getObjectId("_id");
            wordIDs.add(id.toHexString());
        }
    }

    public void search_phrase(String query) {
        Set<String> result = phraseHandler.handlePhraseQuery(query);
        System.out.println("Phrase search results for: " + query);
        displayResults(result);
    }

    private void displayResults(Set<String> results) {
        if (results.isEmpty()) {
            System.out.println("No documents found matching your phrase search.");
        } else {
            results.forEach(url -> System.out.println("Document: " + url));
        }
    }

    public void close() {
        mongoClient.close();
    }

    public static void main(String[] args) {
        QueryProcessor processor = new QueryProcessor();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your search query: ");
        String input = scanner.nextLine();

        if (input.startsWith("\"") && input.endsWith("\"")) {
            // Handle phrase search only
            // input = input.substring(1, input.length() - 1); // remove quotes
            processor.search_phrase(input);
        } else {
            // Handle normal word-based search
            List<String> wordIDs = processor.search(input);
            String[] queryWords = Preprocessor.preprocess(input);
            System.out.println("Query tokens: " + Arrays.toString(queryWords));

            if (wordIDs.isEmpty()) {
                System.out.println("No matching documents found.");
            } else {
                System.out.println("Matching document IDs:");
                for (String id : wordIDs) {
                    System.out.println(id);
                }

                //Now call MongoResultBuilder, ranker, etc.
                List<wordResult> results = MongoResultBuilder.getWordResults(wordIDs, processor.invertedIndex);
                ArrayList<wordResult> resultsArrayList = new ArrayList<>(results);

                ranker myRanker = new ranker();
              myRanker.score(resultsArrayList, (ArrayList<String>) wordIDs);
               myRanker.sortByValue();
               List<rankerresults> sortedLinks = myRanker.setresults();
               for (rankerresults result : sortedLinks) {
                // Split the description into words
                String[] words = result.description.split("\\s+");
                List<String> highlightedWords = new ArrayList<>();
            
                // Find the searched word and include three words before and six words after
                for (int i = 0; i < words.length; i++) {
                    if (words[i].equalsIgnoreCase(input)) {
                        // Highlight the searched word
                        words[i] = "**" + words[i] + "**";
            
                        // Calculate the start and end indices for the range
                        int start = Math.max(0, i - 3); // Ensure we don't go out of bounds
                        int end = Math.min(words.length, i + 7); // Include six words after
            
                        // Add the selected range of words to the list
                        highlightedWords.addAll(Arrays.asList(words).subList(start, end));
                        break; // Stop after finding the first occurrence
                    }
                }
            
                // Join the highlighted words into a string
                String highlightedDescription = String.join(" ", highlightedWords);
            
                System.out.println("Link: " + result.link);
                System.out.println("Title: " + result.title);
                System.out.println("Description: " + result.description);
                System.out.println("-------------------------");
            }
        }

        processor.close();
    }
}
}
