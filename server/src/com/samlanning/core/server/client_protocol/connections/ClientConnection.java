package com.samlanning.core.server.client_protocol.connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.bff.javampd.Player.Status;
import org.slf4j.Logger;

import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage;
import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage.ErrorType;
import com.samlanning.core.server.client_protocol.messages.types.EventMessage;
import com.samlanning.core.server.client_protocol.messages.types.RequestMessage;
import com.samlanning.core.server.client_protocol.messages.types.ResponseMessage;
import com.samlanning.core.server.client_protocol.messages.types.requests.ActionRequestPayload;
import com.samlanning.core.server.client_protocol.messages.types.requests.ListenRequestPayload;
import com.samlanning.core.server.client_protocol.messages.types.responses.ListenResponsePayload;
import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.switchboard.ActionError;
import com.samlanning.core.server.switchboard.ServerSwitchboard;
import com.samlanning.core.server.util.Logging;

public class ClientConnection {

    private static final Logger log = Logging.logger(ClientConnection.class);

    private final ServerSwitchboard switchboard;
    private final ClientConnectionMessageSender sender;

    private final ListenerData listeners = new ListenerData();

    public ClientConnection(ServerSwitchboard switchboard, ClientConnectionMessageSender sender) {
        this.switchboard = switchboard;
        this.sender = sender;
    }

    public void receiveMessageFromClient(JsonMessage message) {

        switch (message.type) {
            case request:
                handleRequest((RequestMessage) message);
                return;
        }
        this.sender.sendMessageToClient(new ErrorMessage(ErrorType.internal, "Not Implemented"));
    }

    private void handleRequest(RequestMessage request) {
        switch (request.requestType) {
            case listen:
                handleListenRequest(request);
                return;
            case action:
                handleActionRequest(request);
                return;
        }
        this.sendRequestError(request, ErrorType.internal, "Not Implemented");
        throw new RuntimeException("Not Implemented");
    }

    private void handleListenRequest(RequestMessage request) {
        if (request.payload == null) {
            this.sendRequestError(request, ErrorType.invalid_request, "Missing Payload");
            return;
        }

        ListenRequestPayload payload;
        try {
            payload = ListenRequestPayload.fromJsonNode(request.payload);
        } catch (IOException e) {
            this.sendRequestError(request, ErrorType.invalid_request,
                "Payload Error: " + e.getMessage());
            return;
        }

        switch (payload.target) {
            case "media":
                listenToMedia(request);
                return;
        }
        this.sendRequestError(request, ErrorType.invalid_request, "Unknown Target: "
            + payload.target);
    }

    private void listenToMedia(RequestMessage request) {
        final int listenerId = listeners.nextListenerId();
        // Send client the ID of the listener
        ListenResponsePayload payload = new ListenResponsePayload(listenerId);
        this.sender.sendMessageToClient(new ResponseMessage(request.requestId, payload));
        // Send listener ID here
        MPDMonitor.Listener mediaListener = new MPDMonitor.Listener() {

            @Override
            public void statusChanged(Status status) {
                sender.sendMessageToClient(new EventMessage(listenerId, "MPD Status Changed: "
                    + status));
            }

        };
        listeners.storeListener(mediaListener);
        switchboard.listenToMPD(mediaListener);
    }

    private void handleActionRequest(RequestMessage request) {
        if (request.payload == null) {
            this.sendRequestError(request, ErrorType.invalid_request, "Missing Payload");
            return;
        }

        ActionRequestPayload payload;
        try {
            payload = ActionRequestPayload.fromJsonNode(request.payload);
        } catch (IOException e) {
            this.sendRequestError(request, ErrorType.invalid_request,
                "Payload Error: " + e.getMessage());
            return;
        }

        try {
            switchboard.performAction(payload.action);
            this.sender.sendMessageToClient(new ResponseMessage(request.requestId, null));
        } catch (ActionError e) {
            this.sendRequestError(request, e.errorType, e.getMessage());
        }

    }

    private void sendRequestError(RequestMessage request, ErrorType error, String message) {
        this.sender.sendMessageToClient(new ErrorMessage(request.requestId, error, message));

    }

    private static class ListenerData {

        private int nextListenerId = 100;
        private final Collection<MPDMonitor.Listener> mpdListeners = new LinkedList<>();

        public synchronized int nextListenerId() {
            return nextListenerId++;
        }

        public synchronized void storeListener(MPDMonitor.Listener listener) {
            mpdListeners.add(listener);
        }

        public synchronized Collection<MPDMonitor.Listener> mpdListeners() {
            return new ArrayList<>(mpdListeners);
        }

    }

    /**
     * Called by the underlying transport when the connection has been closed.
     */
    public void transportClosed() {

        // Unregister all listeners
        for (MPDMonitor.Listener listener : listeners.mpdListeners())
            switchboard.removeMPDListener(listener);

    }

}
