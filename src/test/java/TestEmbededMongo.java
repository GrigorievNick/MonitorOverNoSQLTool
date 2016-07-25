import com.mongodb.ConnectionString;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer;
import org.mhr.monitor.mock.data.mongo.MongoClientUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;

import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.DATABASE_NAME;
import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.HOST;
import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.PORT;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toObservable;

public class TestEmbededMongo {

    private static final EmbeddedMongoContainer embeddedMongoContainer = new EmbeddedMongoContainer();
    private static final String COLLECTION_NAME = "logs";
    private static final String TIME_FIELD = "_ts";
    private static MongoClient client = MongoClients.create(new ConnectionString("mongodb://" + HOST +":" + PORT));
    private static final long twoGB = 2147483648L / 4;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


    @BeforeClass
    public static void startMongo() throws IOException {
        embeddedMongoContainer.start();
        final CreateCollectionOptions options = new CreateCollectionOptions();
        options.capped(true);
        options.sizeInBytes(twoGB);
        pretreatedTable(options);
        executorService.scheduleAtFixedRate(() -> {
            com.mongodb.MongoClient client = new com.mongodb.MongoClient(HOST, PORT);
            com.mongodb.client.MongoDatabase database = client.getDatabase(DATABASE_NAME);
                final MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
                for (int i = 0; i < 3; i++) {
                    collection.insertOne(new Document("_ts", System.currentTimeMillis()));
                }
            client.close();
            }, 0, 1, TimeUnit.SECONDS
        );

    }


    @Test
    public void testLiveTable() throws IOException, InterruptedException {
        BlockingArrayQueue<Document> result = new BlockingArrayQueue<>();
        final Bson ts = Filters.gte(TIME_FIELD, System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        FindPublisher<Document> cursor = MongoClientUtils.createCursor(ts, client, DATABASE_NAME, COLLECTION_NAME);
        final int limit = 10;
        cursor.limit(limit);
        final BlockingArrayQueue<Document> finalResult = result;
        final AtomicReference<Subscription> shareSubscription = new AtomicReference<>();
        cursor.subscribe(new Subscriber<Document>() {
            @Override
            public void onSubscribe(final Subscription s) {
                s.request(limit);
                shareSubscription.set(s);
            }

            @Override
            public void onNext(final Document success) {
                finalResult.add(success);
            }

            @Override
            public void onError(final Throwable t) {
                System.out.println("Failed");
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
            }
        });
        int count = 0;
        while (count < limit) {
            result.take();
            count++;
        }
        shareSubscription.get().cancel();
        result.poll(10, TimeUnit.SECONDS);
    }

    @Test
    public void testCashingWithByTime() throws InterruptedException {
        final Bson ts = Filters.gte("_ts", System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        FindPublisher<Document> cursor = MongoClientUtils.createCursor(ts, client, DATABASE_NAME, COLLECTION_NAME);
        final Observable<Date> replay = toObservable(cursor)
            .replay(4, TimeUnit.SECONDS)
            .autoConnect().map(document -> new Date(document.getLong(TIME_FIELD)));
        replay.subscribe(System.out::println);
        Thread.sleep(5_000);
        replay.subscribe((d) -> System.out.println("Subscriber 2 " + d));
        Thread.sleep(10_000);
    }

    @Test
    public void testStartStop() throws InterruptedException {
        final Bson ts = Filters.gte("_ts", System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        FindPublisher<Document> cursor = MongoClientUtils.createCursor(ts, client, DATABASE_NAME, COLLECTION_NAME);
        toObservable(cursor)
            .map(Document::toJson)
            .replay(1, TimeUnit.SECONDS)
            .refCount()
            .takeUntil(just("*********************************************")
                            .delay(4, TimeUnit.SECONDS).doOnNext(System.out::println).take(1))
            .doOnNext(System.out::println)
            .toList().toBlocking().first();

    }

    @AfterClass
    public static void stopMongo() {
        executorService.shutdownNow();
        com.mongodb.MongoClient mongo = new com.mongodb.MongoClient(HOST, PORT);
        com.mongodb.client.MongoDatabase db = mongo.getDatabase(DATABASE_NAME);
        db.getCollection(COLLECTION_NAME).drop();
        mongo.close();
        embeddedMongoContainer.stop();
    }

    private static com.mongodb.MongoClient pretreatedTable(CreateCollectionOptions options) {
        com.mongodb.MongoClient mongo = new com.mongodb.MongoClient(HOST, PORT);
        com.mongodb.client.MongoDatabase db = mongo.getDatabase(DATABASE_NAME);
        db.createCollection(COLLECTION_NAME, options);
        mongo.close();
        return mongo;
    }
}
