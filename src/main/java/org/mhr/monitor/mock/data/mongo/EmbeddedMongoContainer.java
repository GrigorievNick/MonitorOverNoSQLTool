package org.mhr.monitor.mock.data.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import java.util.Date;
import java.util.function.Consumer;
import org.bson.Document;
import org.springframework.stereotype.Component;

public class EmbeddedMongoContainer {

    public static final String HOST = "localhost";
    public static final String DATABASE_NAME = "test";
    public static int PORT = 12345;

    private final MongodStarter DEFAULT_INSTANCE = MongodStarter.getDefaultInstance();
    private MongodProcess mongod;
    private MongodExecutable mongodExecutable = null;

    public void start() throws IOException {
        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(PORT, Network.localhostIsIPv6()))
            .build();

        mongodExecutable = DEFAULT_INSTANCE.prepare(mongodConfig);
        mongod = mongodExecutable.start();
        MongoClient mongo = new MongoClient(HOST, PORT);
        MongoDatabase db = mongo.getDatabase(DATABASE_NAME);
        db.createCollection("testCol");
        final MongoCollection<Document> col = db.getCollection("testCol");
        col.insertOne(new Document("col1", new Date()));
        col.find().forEach((Consumer<Document>) System.out::println);
        col.drop();
        mongo.close();
    }

    public void stop() {
        mongod.stop();
        if (mongodExecutable != null)
            mongodExecutable.stop();
    }
}
