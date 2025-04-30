package com.team.searchengine.indexer;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.*;

public class InvertedIndexManager {
    private final MongoClient client;
    private final MongoCollection<Document> invertedCollection;
    private final MongoDatabase db;

    public InvertedIndexManager() {
        this.client = MongoClients.create("mongodb://localhost:27017");
        this.db = client.getDatabase("searchengine");
        this.invertedCollection = db.getCollection("inverted_index");
    }

    public void addWord(String word, String url, String title, String description, double tf) {
        Document existing = invertedCollection.find(Filters.eq("word", word)).first();
        if (existing == null) {
            List<String> titles = new ArrayList<>();
            List<String> links = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            List<Double> tfs = new ArrayList<>();

            titles.add(title);
            links.add(url);
            descriptions.add(description);
            tfs.add(tf);

            Document newDoc = new Document("word", word)
                    .append("titles", titles)
                    .append("links", links)
                    .append("descriptions", descriptions)
                    .append("tfs", tfs)
                    .append("idf", 0.0); // initialize idf
            invertedCollection.insertOne(newDoc);
        } else {
            invertedCollection.updateOne(
                    Filters.eq("word", word),
                    Updates.combine(
                            Updates.addToSet("titles", title),
                            Updates.addToSet("links", url),
                            Updates.addToSet("descriptions", description),
                            Updates.addToSet("tfs", tf)
                    )
            );
        }
    }

    public void calculateIDF(int totalDocuments) {
        MongoCursor<Document> cursor = invertedCollection.find().iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            List<String> links = doc.getList("links", String.class);
            int df = links.size();
            double idf = Math.log((double) totalDocuments / df);

            invertedCollection.updateOne(
                    Filters.eq("word", doc.getString("word")),
                    Updates.set("idf", idf)
            );
        }
    }

    public void clearInvertedIndex() {
        invertedCollection.deleteMany(new Document());
    }

    public void close() {
        client.close();
    }
}
