package com.samlanning.core.server.client_protocol.connections;

import java.io.IOException;

import com.samlanning.core.server.client_protocol.messages.JsonMessage;

public interface ClientConnectionMessageSender {

    public void sendMessageToClient(JsonMessage message);
    
}
