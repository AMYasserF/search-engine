package com.team.searchengine.indexer;

import com.team.searchengine.indexer.Models.DocumentService;
import com.team.searchengine.indexer.Models.TokenService;
import com.team.searchengine.indexer.tokenizer.Token;
import com.team.searchengine.indexer.tokenizer.Tokenizer;
import com.team.searchengine.indexer.utils.DBManager;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.file.Paths;
import java.io.File;
import java.util.HashMap;

public class Indexer {

    private static TokenService tokenService;
    private static DocumentService docService;
    private static String stopsPath;
    private static String docPath;
    private static String scorePath;

    private static void initializePaths() {
        String currentDirectory = System.getProperty("user.dir");
        stopsPath = Paths.get(currentDirectory, "resources", "stops.txt").toString();
        docPath = Paths.get(currentDirectory, "plain_docs").toString();
        scorePath = Paths.get(currentDirectory, "resources", "html.txt").toString();
    }

    private static void initializeDB() {
        MongoDatabase connection = DBManager.connect("mongodb://localhost:27017", "test");
        docService = new DocumentService(connection);
        tokenService = new TokenService(connection);
    }

    private static void indexDocument(String path, ObjectId docId, String docPath) {
        String fullFilePath = Paths.get(Indexer.docPath, path).toString() + ".txt";
        System.out.println("Indexing document: " + fullFilePath);

        File file = new File(fullFilePath);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + fullFilePath);
        }

        try {
            Tokenizer tokenizer = new Tokenizer(fullFilePath);
            HashMap<String, Token> dictionary = tokenizer.tokenizeDocument(docPath);
            tokenService.insert(dictionary, docId.toString(), tokenizer.getWordCount());
        } catch (Exception e) {
            throw new RuntimeException("Error indexing document: " + fullFilePath, e);
        }
    }

    public static void main(String[] args) {
        initializePaths();
        initializeDB();
        Language.initializeDictionary(stopsPath);
        Language.initializeHTML(scorePath);

        File folder = new File(docPath);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                String fileName = file.getName().replace(".txt", "");
                Document existingDoc = docService.getDocumentByPath(fileName);

                if (existingDoc == null) {
                    System.out.println("Adding new document to database: " + fileName);
                    docService.addDocument(fileName);
                }
            }
        }

        processUnindexedDocuments();
    }

    private static void processUnindexedDocuments() {
        FindIterable<Document> unindexedDocs = docService.getUnindexedDocuments();
        for (Document document : unindexedDocs) {
            ObjectId docId = document.getObjectId("_id");
            String path = document.getString("path");
            String docFilePath = Paths.get(docPath, docId + ".txt").toString();
            indexDocument(path, docId, docFilePath);
            docService.setIndexed(docId, docFilePath);
        }
    }
}
