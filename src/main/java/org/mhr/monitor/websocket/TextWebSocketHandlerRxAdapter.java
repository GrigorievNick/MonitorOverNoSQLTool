/**
 * Copyright (C) Zoomdata, Inc. 2012-2016. All rights reserved.
 */
package org.mhr.monitor.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

@Component("webSocketHandler")
public class TextWebSocketHandlerRxAdapter extends TextWebSocketHandler {

    @Autowired
    private RxWebSocketHandler<TextMessage> sessionHandler;

    private Map<WebSocketSession, RxWebSocketSession<TextMessage>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        RxWebSocketSession<TextMessage> rxSession = new RxWebSocketSession<>(sessionHandler.decorateNativeSession(session),
                new SerializedSubject<>(PublishSubject.create()));
        sessions.put(session, rxSession);
        sessionHandler.afterConnectionEstablished(rxSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            SecurityContextHolder.getContext().setAuthentication((Authentication) session.getPrincipal());
            sessions.get(session).handleMessage(message);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        RxWebSocketSession<TextMessage> rxSession = sessions.remove(session);
        rxSession.close();
        sessionHandler.afterConnectionClosed(rxSession, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.get(session).handleTransportError(exception);
    }
}
