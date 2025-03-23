package com.team.searchengine.indexer.Models;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

public class DocumentService {

    private final MongoCollection<Document> documentTable;

    public DocumentService(MongoDatabase connection)
    {
        this.documentTable = connection.getCollection("documents");
    }

    

    public FindIterable<Document> getUnindexedDocuments() {
        Document projection = new Document("_id", true).append("path", true);
        Document query = new Document("indexed", false); // Corrected field name
    
        return documentTable.find(query).projection(projection);
    }

    public void setIndexed(ObjectId id, String docPath) {
        Document query = new Document("_id", id);
        Document updates = new Document("indexed", true).append("doc_path", docPath);
        Document updateQuery = new Document("$set", updates);
    
        documentTable.updateOne(query, updateQuery);
    }
    
      // Check if a document exists by its path
      public Document getDocumentByPath(String path) {
        return documentTable.find(new Document("path", path)).first();
    }

    // Add a new document to the collection
    public void addDocument(String path) {
        Document newDoc = new Document("path", path).append("indexed", false);
        documentTable.insertOne(newDoc);
    }



}
