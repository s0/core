package com.samlanning.core.server.client_protocol.messages.types.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListenResponsePayload {

    @JsonProperty("listener_id")
    public final int listenerId;
    
    public ListenResponsePayload(int listenerId){
        this.listenerId = listenerId;
    }
    
}
