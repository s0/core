package com.samlanning.core.server.client_protocol.messages.types;

import com.samlanning.core.server.client_protocol.messages.JsonMessage;
import com.samlanning.core.server.client_protocol.messages.MessageType;

public class ErrorMessage extends JsonMessage {
    
    public final ErrorType error;
    public final String message;

    public ErrorMessage(ErrorType error) {
        this(error, null);
    }

    public ErrorMessage(ErrorType error, String message) {
        super(MessageType.error);
        this.error = error;
        this.message = message;
    }
    
    public enum ErrorType {
        invalid_json
    }

}
