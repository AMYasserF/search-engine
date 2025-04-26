package com.team.searchengine.indexer;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

public class MongoDBManager {
    private final MongoClient client;
    private final MongoCollection<Document> documentsCollection;

    public MongoDBManager() {
        this.client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("searchengine");
        this.documentsCollection = db.getCollection("documents");
    }

    public void saveDocument(String url, String title, String h1, String h2, String h3, String h4, String h5, String h6,
            String strong, String body) {
        Document doc = new Document("url", url)
                .append("title", title)
                .append("h1", h1)
                .append("h2", h2)
                .append("h3", h3)
                .append("h4", h4)
                .append("h5", h5)
                .append("h6", h6)
                .append("strong", strong)
                .append("body", body);

        documentsCollection.replaceOne(Filters.eq("url", url), doc, new ReplaceOptions().upsert(true));
    }

    public void clearDocuments() {
        documentsCollection.deleteMany(new Document());
    }

    public void close() {
        client.close();
    }
}
