package com.samlanning.core.server.transports;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WebSocketTransport {

    private final Server server;

    public WebSocketTransport(String hostname, int port) {
        server = new Server(hostname, port);
    }

    private static class Server extends WebSocketServer {

        public Server(String hostname, int port) {
            super(new InetSocketAddress(hostname, port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            System.out.println("open");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("close");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            System.out.println("message: " + message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

    }

    public void start() {
        server.start();
    }

}
