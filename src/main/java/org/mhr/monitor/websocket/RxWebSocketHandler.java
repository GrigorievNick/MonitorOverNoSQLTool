/**
 * Copyright (C) Zoomdata, Inc. 2012-2016. All rights reserved.
 */
package org.mhr.monitor.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public interface RxWebSocketHandler<T extends WebSocketMessage<?>> {

    default WebSocketSession decorateNativeSession(WebSocketSession session) {
        return session;
    }

    void afterConnectionEstablished(RxWebSocketSession<T> session);

    void afterConnectionClosed(RxWebSocketSession<T> session, CloseStatus status);

}
