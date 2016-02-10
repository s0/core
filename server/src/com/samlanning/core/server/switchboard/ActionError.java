package com.samlanning.core.server.switchboard;

import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage.ErrorType;

public class ActionError extends Exception {
    
    public final ErrorType errorType;

    public ActionError(ErrorType errorType, String message){
        super(message);
        this.errorType = errorType;
    }
    
}
