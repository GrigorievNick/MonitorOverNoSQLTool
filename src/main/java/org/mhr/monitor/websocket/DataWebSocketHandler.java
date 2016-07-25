package org.mhr.monitor.websocket;

import org.mhr.monitor.model.CommandEvent;
import org.mhr.monitor.model.DataEvent;
import org.mhr.monitor.model.Event;
import org.mhr.monitor.model.SerializeUtils;
import org.mhr.monitor.service.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import rx.Observable;
import rx.Subscription;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mhr.monitor.model.CommandEvent.Command.START;
import static org.mhr.monitor.model.CommandEvent.Command.START_DONE;
import static org.mhr.monitor.model.CommandEvent.Command.STOP;
import static org.mhr.monitor.model.CommandEvent.Command.STOP_DONE;

@Component
public class DataWebSocketHandler implements RxWebSocketHandler<TextMessage> {

    private static final Logger logger = LoggerFactory.getLogger(DataWebSocketHandler.class);

    private static final int SEND_TIME_LIMIT = 2 * 60 * 1000;
    private static final int BUFFER_SIZE_LIMIT = 2 * 1024 * 1024;
    private Subscription responseSubscribtion;

    @Autowired
    private Cache cache;

    @Override
    public void afterConnectionEstablished(RxWebSocketSession<TextMessage> session) {
        logger.info(session + " opened websocket: {}", session);
        final Observable<Event> requestStream = session.getInput()
            .map(TextMessage::getPayload)
            .map(SerializeUtils::fromJson)
            .doOnNext(m -> logger.debug("< {}", m))
            .publish()
            .refCount();

        final Observable<CommandEvent> stopEvent = requestStream
            .map(e -> (CommandEvent) e)
            .filter(commandEvent -> commandEvent.getCommand() == STOP)
            .map(e -> new CommandEvent(STOP_DONE));

        Observable<Event> startEvent =
            requestStream
                .filter(e -> e instanceof CommandEvent)
                .map(e -> (CommandEvent) e)
                .filter(e -> e.getCommand() == START)
                .flatMap(e -> cache.replay()
                    .map(msg -> (Event) new DataEvent(msg))
                    .startWith(new CommandEvent(START_DONE))
                    .takeUntil(stopEvent));

        responseSubscribtion = Observable.merge(startEvent, stopEvent)
            .map(SerializeUtils::toJson)
            .doOnNext(m -> logger.debug("< {}", m))
            .map(TextMessage::new)
            .doOnCompleted(() -> logger.debug("Session Close"))
            .publish()
            .refCount()
            .throttleWithTimeout(500, MILLISECONDS)
            .subscribe(session::sendMessage);

    }

    @Override
    public WebSocketSession decorateNativeSession(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, BUFFER_SIZE_LIMIT);
    }

    @Override
    public void afterConnectionClosed(RxWebSocketSession<TextMessage> session, CloseStatus status) {
        if (responseSubscribtion != null && !responseSubscribtion.isUnsubscribed()) {
            responseSubscribtion.unsubscribe();
        }
        logger.info("left the room!, status:{}, websocket: {}", new Object[]{status, session});
    }
}