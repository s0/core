package com.samlanning.core.server.client_protocol.transports;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;

import com.samlanning.core.server.client_protocol.connections.ClientConnection;
import com.samlanning.core.server.client_protocol.connections.ClientConnectionMessageSender;
import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage;
import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage.ErrorType;
import com.samlanning.core.server.switchboard.ServerSwitchboard;
import com.samlanning.core.server.util.Logging;

public class WebSocketTransport {

    private static final Logger log = Logging.logger(WebSocketTransport.class);

    private final WSServer server;

    public WebSocketTransport(ServerSwitchboard switchboard, String hostname, int port) {
        server = new WSServer(switchboard, hostname, port);
    }

    private static class WSServer extends WebSocketServer {

        private final ServerSwitchboard switchboard;
        private final Map<WebSocket, ClientConnection> connections = new ConcurrentHashMap<>();

        public WSServer(ServerSwitchboard switchboard, String hostname, int port) {
            super(new InetSocketAddress(hostname, port));
            this.switchboard = switchboard;
        }

        @Override
        public void onOpen(final WebSocket conn, ClientHandshake handshake) {
            ClientConnectionMessageSender messageSender = new ClientConnectionMessageSender() {

                @Override
                public void sendMessageToClient(JsonMessage message) {
                    System.out.println("sending: " + message);
                    try {
                        conn.send(message.toJson());
                    } catch (IOException | WebsocketNotConnectedException e) {
                        // TODO: shutdown ClientConnection when not connected
                        log.error("Error sending message", e);
                    }
                }

            };
            connections.put(conn, new ClientConnection(switchboard, messageSender));
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            ClientConnection connection = connections.remove(conn);
            connection.transportClosed();
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            log.debug("Received message: " + message);
            ClientConnection clientConnection = connections.get(conn);
            if (clientConnection == null) {
                log.error("Received message from WebSocket with no ClientConnection");
                conn.close();
                return;
            }
            try {
                JsonMessage jsonMessage;
                try {
                    jsonMessage = JsonMessage.fromJson(message);
                } catch (IOException e) {
                    sendError(conn, ErrorType.invalid_json, e);
                    return;
                }
                clientConnection.receiveMessageFromClient(jsonMessage);
            } catch (Throwable e) {
                log.error("Uncaught Exception in onMessage()", e);
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
            conn.close();
        }

        private static void sendError(WebSocket conn, ErrorType errorType, Throwable throwable) {
            log.info("Error: ", throwable);
            try {
                conn.send(new ErrorMessage(errorType, throwable.getMessage()).toJson());
            } catch (IOException e) {
                log.error("Error sending error message", e);
            }
        }

    }

    public void start() {
        server.start();
    }

}
