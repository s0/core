package com.samlanning.core.server.client_protocol.messages.types;

import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.MessageType;

public class EventMessage extends JsonMessage {
    
    public final int listenerId;
    public final Object data;

    public EventMessage(int listenerId, Object data) {
        super(MessageType.event);
        this.listenerId = listenerId;
        this.data = data;
        
    }

}
