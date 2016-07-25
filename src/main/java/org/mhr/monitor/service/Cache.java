package org.mhr.monitor.service;

import javax.annotation.PreDestroy;
import org.mhr.monitor.dao.ILiveStreamDao;
import org.mhr.monitor.model.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Subscription;

import static java.util.concurrent.TimeUnit.valueOf;

@Component
public class Cache {

    private final Observable<Msg> replay;
    private final Observable<Msg> live;
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

    public Observable<Msg> replay() {
        return replay;
    }

    public Observable<Msg> getLive() {
        return live;
    }

}
