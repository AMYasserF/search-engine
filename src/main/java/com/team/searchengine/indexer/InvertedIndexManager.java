package com.team.searchengine.indexer;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import java.util.*;

public class InvertedIndexManager {
    private final MongoClient client;
    private final MongoCollection<Document> invertedCollection;

    public InvertedIndexManager() {
        this.client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("searchengine");
        this.invertedCollection = db.getCollection("inverted_index");
    }

    public void addWord(String word, String url, int score, int frequency) {
        Document posting = new Document("url", url)
                .append("score", score)
                .append("frequency", frequency);

        invertedCollection.updateOne(
                Filters.eq("word", word),
                Updates.addToSet("postings", posting),
                new UpdateOptions().upsert(true));
    }

    public void clearInvertedIndex() {
        invertedCollection.deleteMany(new Document());
    }

    public void close() {
        client.close();
    }
}
