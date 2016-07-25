package org.mhr.monitor.mock.data.mongo;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.mhr.monitor.model.OperationType;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;
import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.DATABASE_NAME;

@Component
@DependsOn("mongoClient")
@Slf4j
public class LiveStream {

    @Autowired
    private MongoClient client;

    @Value("${table.size}")
    private long twoGB;

    @PostConstruct
    public void startStream() {
        final CreateCollectionOptions options = new CreateCollectionOptions();
        options.capped(true);
        options.sizeInBytes(twoGB);
        final MongoDatabase db = client.getDatabase(DATABASE_NAME);
        db.createCollection(MongoClientUtils.COLLECTION_NAME, options).subscribe(new Subscriber<Success>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Success success) {

            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Scheduled(fixedRateString = "${mock.data.generation.rate:1000}")
    public void write() {
        final MongoDatabase database = client.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> collection = database.getCollection(MongoClientUtils.COLLECTION_NAME);
        final ImmutableMap.Builder<String, Object> opBuilder = ImmutableMap.<String, Object>builder()
            .put(MongoClientUtils.TIME_COLUMN, System.currentTimeMillis());
        for (int i = 0; i < 50; i++) {
            opBuilder.put("Column " + i, i);
        }
        Map<String, Object> map = opBuilder.build();

        final Map<String, Object> depositOp = ImmutableMap.<String, Object>builder()
            .put("operationType", OperationType.DEPOSIT.name())
            .putAll(map)
            .build();
        final Map<String, Object> widthrawOp = ImmutableMap.<String, Object>builder()
            .put("operationType", OperationType.WIDTHRAW.name())
            .putAll(map)
            .build();
        final Map<String, Object> transferOp = ImmutableMap.<String, Object>builder()
            .put("operationType", OperationType.TRANSFER.name())
            .putAll(map)
            .build();
        collection.insertMany(
            asList(new Document(depositOp), new Document(transferOp), new Document(widthrawOp))
        ).subscribe(new Subscriber<Success>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(10);
            }

            @Override
            public void onNext(Success success) {
                log.debug(success.name());
            }

            @Override
            public void onError(Throwable t) {
                log.error("During save", t);
            }

            @Override
            public void onComplete() {
                log.debug("Save complete");
            }
        });
    }


}
