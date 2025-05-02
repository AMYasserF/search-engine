package com.team.searchengine.RankerQueryProcessor;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class MongoResultBuilder {

    public static List<wordResult> getWordResults(List<String> wordIDs, MongoCollection<Document> collection) {
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

                for (int i = 0; i < links.size(); i++) {
                    wr.addValue(links.get(i));
                    wr.addTitle(titles.get(i));
                    wr.addDisc(descriptions.get(i));
                    wr.addTF(tfs.get(i));
                }

                wr.addIDF(idf);
                resultList.add(wr);
            }
        }

        return resultList;
    }
}
