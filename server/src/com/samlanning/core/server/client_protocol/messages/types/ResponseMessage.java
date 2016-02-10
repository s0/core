package com.samlanning.core.server.client_protocol.messages.types;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.MessageType;

public class ResponseMessage extends JsonMessage {

    @JsonProperty("request_id")
    public final int requestId;

    public final Object payload;

    @JsonCreator
    public ResponseMessage(
        @JsonProperty(value = "request_id") int requestId,
        @JsonProperty(value = "payload") Object payload) {
        super(MessageType.response);
        this.requestId = requestId;
        this.payload = payload;
    }

}
