package org.mhr.monitor.service;

import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.mhr.monitor.dao.ILiveStreamDao;
import org.mhr.monitor.model.CommandEvent;
import org.mhr.monitor.model.DataEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Subscription;

import static com.google.common.collect.Maps.filterKeys;
import static java.util.concurrent.TimeUnit.valueOf;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class Cache {

    private final Observable<DataEvent> replay;
    private final Observable<DataEvent> live;
    private final Subscription dummySubscriptionForPrecache;

    @Autowired
    public Cache(ILiveStreamDao client,
                 @Value("${cache.time.to.live:4}") String timeToCache,
                 @Value("${cache.time.unit:SECONDS}") String timeUnitToCache) {
        replay = client.find().replay(Long.valueOf(timeToCache), valueOf(timeUnitToCache)).refCount();
        live = client.find().publish().refCount();
        dummySubscriptionForPrecache = live.subscribe();
    }

    @PreDestroy
    public void destroy() {
        dummySubscriptionForPrecache.unsubscribe();
    }

    public Observable<DataEvent> replay(CommandEvent cEvent) {
        Observable<DataEvent> filtered =
            replay.filter(data -> cEvent.getOperationType() == data.getOperationType());
        if (!isEmpty(cEvent.getFields())) {
            filtered = filtered.map(dataEvent ->
                new DataEvent(dataEvent.getTs(), dataEvent.getOperationType(),
                    filterKeys(dataEvent.getOperationBody(), cEvent.getFields()::contains))
            );
        }
        if (cEvent.getEnd() != null && cEvent.getStart() != null) {
            filtered = filtered
                .skipWhile(d -> d.getTs() < cEvent.getStart().getDate())
                .takeUntil(d -> d.getTs() > cEvent.getStart().getDate()
                    && d.getTs() < cEvent.getEnd().getDate());
        }
        return filtered;
    }

    public Observable<DataEvent> getLive() {
        return live;
    }

}
