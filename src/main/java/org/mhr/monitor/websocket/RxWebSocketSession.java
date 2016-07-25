/**
 * Copyright (C) Zoomdata, Inc. 2012-2016. All rights reserved.
 */
package org.mhr.monitor.websocket;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import rx.Observable;
import rx.subjects.Subject;

public class RxWebSocketSession<T extends WebSocketMessage<?>> {
    private static final Logger log = LoggerFactory.getLogger(RxWebSocketSession.class);

    private final WebSocketSession nativeSession;
    private final Subject<T, T> inputSubject;

    public RxWebSocketSession(WebSocketSession nativeSession, Subject<T, T> inputSubject) {
        this.nativeSession = nativeSession;
        this.inputSubject = inputSubject;
    }

    public Principal getPrincipal() {
        return nativeSession.getPrincipal();
    }

    public void sendMessage(T message) {
        try {
            nativeSession.sendMessage(message);
        } catch (Exception e) {
            log.error("Can't send message to websocket, looks like it's dropped", e);
        }
    }

    public Observable<T> getInput() {
        return inputSubject;
    }

    public void handleMessage(T message) {
        inputSubject.onNext(message);
    }

    public void handleTransportError(Throwable exception) {
        log.debug("Transport error", exception);
    }

    public void close() {
        inputSubject.onCompleted();
    }

    @Override
    public String toString() {
        return nativeSession.toString();
    }
}
