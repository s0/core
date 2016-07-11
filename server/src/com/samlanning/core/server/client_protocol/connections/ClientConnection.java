package com.samlanning.core.server.client_protocol.connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.bff.javampd.player.Player.Status;
import org.bff.javampd.song.MPDSong;
import org.slf4j.Logger;

import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage;
import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage.ErrorType;
import com.samlanning.core.server.client_protocol.messages.types.EventMessage;
import com.samlanning.core.server.client_protocol.messages.types.RequestMessage;
import com.samlanning.core.server.client_protocol.messages.types.ResponseMessage;
import com.samlanning.core.server.client_protocol.messages.types.events.LightingEventPayload;
import com.samlanning.core.server.client_protocol.messages.types.events.MediaEventPayload;
import com.samlanning.core.server.client_protocol.messages.types.requests.ActionRequestPayload;
import com.samlanning.core.server.client_protocol.messages.types.requests.ListenRequestPayload;
import com.samlanning.core.server.client_protocol.messages.types.responses.ListenResponsePayload;
import com.samlanning.core.server.lighting.LightingControl;
import com.samlanning.core.server.lighting.RGBLightValue;
import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.switchboard.ActionError;
import com.samlanning.core.server.switchboard.ServerSwitchboard;
import com.samlanning.core.server.util.Logging;

public class ClientConnection {

    private static final Logger log = Logging.logger(ClientConnection.class);

    private final ServerSwitchboard switchboard;
    private final ClientConnectionMessageSender sender;

    private final ListenerData<MPDMonitor.Listener> mpdListeners =
        new ListenerData<MPDMonitor.Listener>();
    private final ListenerData<LightingControl.Listener> lightingListeners =
        new ListenerData<LightingControl.Listener>();

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
            case "lights":
                listenToLights(request);
                return;
        }
        this.sendRequestError(request, ErrorType.invalid_request, "Unknown Target: "
            + payload.target);
    }

    private void listenToMedia(RequestMessage request) {
        final int listenerId = mpdListeners.nextListenerId();
        // Send client the ID of the listener
        ListenResponsePayload payload = new ListenResponsePayload(listenerId);
        this.sender.sendMessageToClient(new ResponseMessage(request.requestId, payload));
        // Send listener ID here
        MPDMonitor.Listener mediaListener = new MPDMonitor.Listener() {

            private Status status;
            private MPDSong song;

            @Override
            public synchronized void statusChanged(Status status) {
                this.status = status;
                sendEvent();
            }

            @Override
            public synchronized void songChanged(MPDSong song) {
                this.song = song;
                sendEvent();
            }

            private synchronized void sendEvent() {
                MediaEventPayload.State state = null;
                switch (status) {
                    case STATUS_PAUSED:
                        state = MediaEventPayload.State.paused;
                        break;
                    case STATUS_PLAYING:
                        state = MediaEventPayload.State.playing;
                        break;
                    case STATUS_STOPPED:
                        state = MediaEventPayload.State.stopped;
                        break;
                }
                MediaEventPayload payload;
                if (song == null) {
                    payload = new MediaEventPayload(state, null, null, null);
                } else {
                    payload =
                        new MediaEventPayload(state, song.getTitle(), song.getArtistName(),
                            song.getAlbumName());
                }
                sender.sendMessageToClient(new EventMessage(listenerId, payload));
            }

        };
        mpdListeners.storeListener(mediaListener);
        switchboard.listenToMPD(mediaListener);
    }

    private void listenToLights(RequestMessage request) {
        final int listenerId = mpdListeners.nextListenerId();
        // Send client the ID of the listener
        ListenResponsePayload payload = new ListenResponsePayload(listenerId);
        this.sender.sendMessageToClient(new ResponseMessage(request.requestId, payload));
        // Send listener ID here
        LightingControl.Listener lightsListener = new LightingControl.Listener() {

            RGBLightValue oldColor = null;
            float oldBrightness = -1f;

            @Override
            public void newLightColor(RGBLightValue newColor, float newBrightness) {
                RGBLightValue color = null;
                Float brightness = null;
                if (!newColor.equals(oldColor)) {
                    color = oldColor = newColor;
                }
                if (oldBrightness != newBrightness) {
                    brightness = oldBrightness = newBrightness;
                }
                if (color != null || brightness != null) {
                    LightingEventPayload payload = new LightingEventPayload(color, brightness);
                    sender.sendMessageToClient(new EventMessage(listenerId, payload));
                }
            }

        };
        lightingListeners.storeListener(lightsListener);
        switchboard.lighting().addListener(lightsListener);
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

    private static class ListenerData<T> {

        private int nextListenerId = 100;
        private final Collection<T> mpdListeners = new LinkedList<>();

        public synchronized int nextListenerId() {
            return nextListenerId++;
        }

        public synchronized void storeListener(T listener) {
            mpdListeners.add(listener);
        }

        public synchronized Collection<T> listeners() {
            return new ArrayList<>(mpdListeners);
        }

    }

    /**
     * Called by the underlying transport when the connection has been closed.
     */
    public void transportClosed() {

        // Unregister all listeners
        for (MPDMonitor.Listener listener : mpdListeners.listeners())
            switchboard.removeMPDListener(listener);

        // Unregister all listeners
        for (LightingControl.Listener listener : lightingListeners.listeners())
            switchboard.lighting().removeListener(listener);

    }

}
