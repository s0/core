package com.samlanning.core.server.client_protocol.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.MessageType;

public class EventMessage extends JsonMessage {
    
    @JsonProperty("listener_id")
    public final int listenerId;
    public final Object payload;

    public EventMessage(int listenerId, Object payload) {
        super(MessageType.event);
        this.listenerId = listenerId;
        this.payload = payload;
        
    }

}
