package com.samlanning.core.server.client_protocol.messages.types;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.MessageType;

public class RequestMessage extends JsonMessage {

    @JsonProperty("request_type")
    public final RequestType requestType;

    @JsonProperty("request_id")
    public final int requestId;

    public final JsonNode payload;

    @JsonCreator
    public RequestMessage(
        @JsonProperty(value = "request_type") RequestType requestType,
        @JsonProperty(value = "request_id") int requestId,
        @JsonProperty(value = "payload") JsonNode payload) throws IOException {
        super(MessageType.request);
        if (requestType == null)
            throw new IOException("Missing \"request_type");
        if (requestId == 0)
            throw new IOException("Invalid / Missing \"request_id");
        this.requestType = requestType;
        this.requestId = requestId;
        this.payload = payload;
    }

    public enum RequestType {
        listen
    }

}
