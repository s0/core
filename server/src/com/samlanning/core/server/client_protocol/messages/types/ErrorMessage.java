package com.samlanning.core.server.client_protocol.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.MessageType;

public class ErrorMessage extends JsonMessage {

    @JsonProperty("request_id")
    public final Integer requestId;
    public final ErrorType error;
    public final String message;

    public ErrorMessage(ErrorType error) {
        this(error, null);
    }
    
    public ErrorMessage(ErrorType error, String message) {
        this(null, error, null);
    }

    public ErrorMessage(Integer requestId, ErrorType error, String message) {
        super(MessageType.error);
        this.requestId = requestId;
        this.error = error;
        this.message = message;
    }
    
    public enum ErrorType {
        invalid_json, internal, invalid_request
    }

}
