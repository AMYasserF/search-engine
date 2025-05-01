package com.team.searchengine.Ranker;

import com.mongodb.client.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class MongoResultBuilder {

    public static List<wordResult> getWordResults(List<String> wordIDs) {
        // MongoDB connection setup
        String uri = "mongodb://localhost:27017"; // Change this if using a different host/port
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("SearchEngine"); // Replace with your DB name
        MongoCollection<Document> collection = database.getCollection("words"); // Replace with your collection name

        List<wordResult> resultList = new ArrayList<>();

        for (String wordID : wordIDs) {
            Document doc = collection.find(new Document("_id", new ObjectId(wordID))).first();

            if (doc != null) {
                wordResult wr = new wordResult();

                List<String> links = (List<String>) doc.get("links");
                List<String> titles = (List<String>) doc.get("titles");
                List<String> descriptions = (List<String>) doc.get("descriptions");
                List<Double> tfs = (List<Double>) doc.get("tfs");
                Double idf = doc.getDouble("idf");

                // Safely cast headers to List<List<Boolean>>
                List<List<Boolean>> headers = new ArrayList<>();
                List<?> rawHeaders = (List<?>) doc.get("headers");
                for (Object obj : rawHeaders) {
                    if (obj instanceof List) {
                        List<?> inner = (List<?>) obj;
                        List<Boolean> booleanList = new ArrayList<>();
                        for (Object val : inner) {
                            if (val instanceof Boolean) {
                                booleanList.add((Boolean) val);
                            }
                        }
                        headers.add(booleanList);
                    }
                }

                // Populate wordResult
                for (int i = 0; i < links.size(); i++) {
                    wr.addValue(links.get(i));
                    wr.addTitle(titles.get(i));
                    wr.addDisc(descriptions.get(i));
                    wr.addTF(tfs.get(i));
                    if (i < headers.size()) {
                        wr.addHeaders(headers.get(i));
                    } else {
                        wr.addHeaders(Arrays.asList(false, false, false)); // default if missing
                    }
                }

                wr.addIDF(idf);
                resultList.add(wr);
            }
        }

        mongoClient.close();
        return resultList;
    }
}
