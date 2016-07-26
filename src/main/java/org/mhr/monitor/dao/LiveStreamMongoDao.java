package org.mhr.monitor.dao;


import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.mhr.monitor.model.DataEvent;
import org.mhr.monitor.model.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Maps.filterKeys;
import static com.google.common.collect.Maps.transformEntries;
import static com.mongodb.client.model.Filters.and;
import static org.mhr.monitor.mock.data.mongo.EmbeddedMongoContainer.DATABASE_NAME;
import static org.mhr.monitor.mock.data.mongo.MongoClientUtils.createCursor;
import static org.mhr.monitor.model.OperationType.valueOf;
import static rx.RxReactiveStreams.toObservable;

@Component
public class LiveStreamMongoDao implements ILiveStreamDao {

    @Autowired
    private MongoClient client;

    @Override
    public Observable<DataEvent> find(OperationType type) {
        final Bson operationType = Filters.eq("operationType", type.name());
        final Bson ts = Filters.gte("_ts", System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        return toObservable(createCursor(and(ts, operationType), client, DATABASE_NAME, "logs")).map(this::convertToMsg);
    }

    @Override
    public Observable<DataEvent> find() {
        final Bson ts = Filters.gte("_ts", System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        return toObservable(createCursor(ts, client, DATABASE_NAME, "logs")).map(this::convertToMsg);
    }

    private DataEvent convertToMsg(Document document) {
        //noinspection unchecked
        return new DataEvent(document.getLong("_ts"),
            valueOf(document.getString("operationType")),
            transformEntries(filterKeys(document, Predicates.and(not("_id"::equals)
                /*, not("_ts"::equals), not("operationType"::equals) */)),
                (key, value) -> {
                    if(key.equalsIgnoreCase("_ts"))
                        return new Date((Long)value);
                    return value;
                }));
    }
}
