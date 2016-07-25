package org.eclipse.jetty.demo;

import java.io.FileNotFoundException;
import java.net.URI;
import org.mhr.monitor.model.CommandEvent;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mhr.monitor.model.CommandEvent.Command.START;
import static org.mhr.monitor.model.CommandEvent.Command.STOP;
import static org.mhr.monitor.model.SerializeUtils.toJson;

public class EventClient {

    private static final WebSocketHandler WEB_SOCKET_HANDLER = new WebSocketHandler() {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            System.out.println(session);
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            System.out.println(message.getPayload());
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            exception.printStackTrace();
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            System.out.println(session + " : " + closeStatus);
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    };

    public static void main(String[] args) throws FileNotFoundException {
        URI uri = URI.create("ws://admin:admin@localhost:8080/websocket/");
        final SockJsClient client = new SockJsClient(singletonList(new WebSocketTransport(new StandardWebSocketClient())));
        client.start();
        try {
            try (WebSocketSession session = client.doHandshake(WEB_SOCKET_HANDLER, uri.toString()).get()) {
                session.sendMessage(new TextMessage(toJson(new CommandEvent(START))));
                System.out.println("Start");
                Thread.sleep(1000);
                session.sendMessage(new TextMessage(toJson(new CommandEvent(STOP))));
                System.out.println("Stop >");
                Thread.sleep(5000);
                System.out.println("Stop <");
                session.sendMessage(new TextMessage(toJson(new CommandEvent(START))));
                System.out.println("Start");
                Thread.sleep(10000);
                session.sendMessage(new TextMessage(toJson(new CommandEvent(STOP))));
                System.out.println("Stop");
                Thread.sleep(1000);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }
}
