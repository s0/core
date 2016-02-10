package com.samlanning.core.server.client_protocol.messages.types;

import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.MessageType;

public class PingMessage extends JsonMessage {

    public PingMessage() {
        super(MessageType.ping);
    }

    
    
}
