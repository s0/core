package com.samlanning.core.server.client_protocol.connections;

import org.bff.javampd.Player.Status;

import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.types.EventMessage;
import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.switchboard.ServerSwitchboard;

public class ClientConnection {

    private final ServerSwitchboard switchboard;
    private final ClientConnectionMessageSender sender;

    private MPDMonitor.Listener mediaListener = null;

    public ClientConnection(ServerSwitchboard switchboard, ClientConnectionMessageSender sender) {
        this.switchboard = switchboard;
        this.sender = sender;

        mediaListener = new MPDMonitor.Listener() {

            @Override
            public void statusChanged(Status status) {
                sender.sendMessageToClient(new EventMessage(0, "MPD Status Changed: " + status));
            }

        };
        switchboard.listenToMPD(mediaListener);
    }

    public void receiveMessageFromClient(JsonMessage message) {
        this.sender.sendMessageToClient(message);
    }

}
