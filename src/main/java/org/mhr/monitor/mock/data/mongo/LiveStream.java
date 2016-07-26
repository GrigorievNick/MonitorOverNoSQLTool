package org.mhr.monitor.mock.data.mongo;

import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
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
import static java.util.stream.Collectors.toList;
import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.DATABASE_NAME;

@Component
@DependsOn("mongoClient")
@Slf4j
public class LiveStream {

    @Autowired
    private MongoClient client;

    @Value("${table.size}")
    private long twoGB;

    private final Random random = new Random();
    private static final int DAY_PERIOD = 24 * 60 * 60 * 1000;


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
        client.getDatabase(DATABASE_NAME)
            .getCollection(MongoClientUtils.COLLECTION_NAME)
            .insertMany(
                asList(new Document(generateDocument(System.currentTimeMillis())),
                    new Document(generateDocument(System.currentTimeMillis())),
                    new Document(generateDocument(System.currentTimeMillis())))
            ).subscribe(new Subscriber<Success>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
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

    private Document generateDocument(Long created) {
        OperationType type = OperationType.values()[random.nextInt(OperationType.values().length)];
        Document document = new Document();
        generateFieldList(49).forEach(f -> generateField(f, document));

        document.put("operationType", type.name());
        document.put(MongoClientUtils.TIME_COLUMN, created);
        return document;
    }

    private void generateField(String fieldName, Document document) {
        switch (fieldName.split("_")[0]) {
            case "STRING":
                document.put(fieldName, generateString());
                break;
            case "INTEGER":
                document.put(fieldName, generateInteger());
                break;
            case "TIMESTAMP":
                document.put(fieldName, generateTimestamp());
                break;
            case "BOOLEAN":
                document.put(fieldName, generateBoolean());
                break;
        }
    }

    private List<String> generateFieldList(int size) {
        String[] array = new String[]{"INTEGER", "BOOLEAN", "STRING", "TIMESTAMP"};
        return IntStream.rangeClosed(0, size)
            .mapToObj(i -> array[random.nextInt(array.length)] + "_" + i)
            .collect(toList());
    }

    private String generateString() {
        int length = 1 + random.nextInt(10);
        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = (char) ('a' + random.nextInt(26));
        }
        return String.valueOf(array);
    }

    private int generateInteger() {
        return random.nextInt(100000);
    }

    private Date generateTimestamp() {
        int periodLocation = random.nextInt(DAY_PERIOD);
        return new Date(System.currentTimeMillis() - periodLocation);
    }

    private boolean generateBoolean() {
        return random.nextInt(2) == 0;
    }

}
