package org.mhr.monitor.dao;


import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import java.util.concurrent.TimeUnit;
import org.bson.conversions.Bson;
import org.mhr.monitor.mock.data.mongo.MongoClientUtils;
import org.mhr.monitor.model.Msg;
import org.mhr.monitor.model.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import static com.mongodb.client.model.Filters.and;
import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.DATABASE_NAME;
import static org.mhr.monitor.mock.data.mongo.MongoClientUtils.createCursor;
import static org.mhr.monitor.model.OperationType.valueOf;
import static rx.RxReactiveStreams.toObservable;

@Component
public class MongoDao implements ILiveStreamDao {

    @Autowired
    private MongoClient client;

    @Override
    public Observable<Msg> find(OperationType type) {
        final Bson operationType = Filters.eq("operationType", type.name());
        final Bson ts = Filters.gte("_ts", System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        return toObservable(createCursor(and(ts, operationType), client, DATABASE_NAME, "logs"))
            .map(document -> new Msg(document.getLong("_ts"), valueOf(document.getString("operationType")), document));
    }

    @Override
    public Observable<Msg> find() {
        final Bson ts = Filters.gte("_ts", System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        return toObservable(createCursor(ts, client, DATABASE_NAME, "logs"))
            .map(document -> new Msg(document.getLong("_ts"), valueOf(document.getString("operationType")), document));
    }
}
