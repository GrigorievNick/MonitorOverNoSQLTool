package org.mhr.monitor.config;

import org.mhr.monitor.websocket.TextWebSocketHandlerRxAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeHandler;


@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TextWebSocketHandlerRxAdapter websocketHandler;

    @Autowired
    private HandshakeHandler handshakeHandler;

    @Value("${server.session-timeout:1800}")
    private int sessionTimeout;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(websocketHandler, "/websocket", "/websocket/*")
            .setAllowedOrigins("*")
            .setHandshakeHandler(handshakeHandler)
            .withSockJS();
    }
}
