package org.mhr.monitor.mock.data.mongo;

import com.mongodb.CursorType;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoClientUtils {

    public static final String COLLECTION_NAME = "logs";
    public static final String TIME_COLUMN = "_ts";

    public static FindPublisher<Document> createCursor(final Bson query, MongoClient _mongo, String dbName, String collectionName) {
        final MongoCollection<Document> col = _mongo.getDatabase(dbName).getCollection(collectionName);
        return col.find(query).cursorType(CursorType.Tailable);
    }
}
