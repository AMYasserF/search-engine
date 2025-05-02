package com.team.searchengine.RankerQueryProcessor;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class QueryProcessor {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    public final MongoCollection<Document> invertedIndex;  // Made accessible for passing to MongoResultBuilder

    public QueryProcessor() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("searchengine");
        invertedIndex = database.getCollection("inverted_index");
    }

    /**
     * Processes a query and returns the list of matching word document ObjectId strings.
     */
    public List<String> search(String query) {
        String[] processedWords = Preprocessor.preprocess(query);

        Set<String> wordIDs = new HashSet<>(); // Set to avoid duplicates

        for (String word : processedWords) {
            // Original word (high weight)
            findWordID(word, wordIDs);

            // Stemmed version (low weight)
            String stemmed = Stemmer.stem(word);
            if (!stemmed.equals(word)) {
                findWordID(stemmed, wordIDs);
            }
        }

        return new ArrayList<>(wordIDs); // Return as list
    }

    /**
     * Finds word document by word text and collects its ObjectId.
     */
    private void findWordID(String word, Set<String> wordIDs) {
        Document doc = invertedIndex.find(new Document("word", word)).first();
        if (doc != null && doc.containsKey("_id")) {
            ObjectId id = doc.getObjectId("_id");
            wordIDs.add(id.toHexString()); // Store as string
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
    
        List<String> wordIDs = processor.search(input);
    
        if (wordIDs.isEmpty()) {
            System.out.println("No matching words found.");
        } else {
            System.out.println("Matching word document IDs:");
            // for (String id : wordIDs) {
            //     System.out.println(id);
            // }
            List<wordResult> results = MongoResultBuilder.getWordResults(wordIDs, processor.invertedIndex);
             ranker myRanker = new ranker();
          //  myRanker.score(results,);
            myRanker.sortByValue();
            List<rankerresults> sortedLinks = myRanker.setresults();

            // for (rankerresults result : sortedLinks) {
            //     System.out.println("Link: " + result.link);
            //     System.out.println("Title: " + result.title);
            //     System.out.println("Description: " + result.description);
            //     System.out.println("-------------------------");
            // }
           

        }
    
        processor.close();
    }
}
