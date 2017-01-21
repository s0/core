package com.samlanning.core.server.client_protocol.messages.types.requests;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.samlanning.core.server.util.JsonConstants;

public class ActionRequestPayload {
    
    public final String action;

    public ActionRequestPayload(@JsonProperty("action") String action) throws IOException {
        if (action == null)
            throw new IOException("Missing field \"action\"");
        this.action = action;
    }

    public static ActionRequestPayload fromJsonNode(JsonNode payload) throws IOException {
        return JsonConstants.JSON.treeToValue(payload, ActionRequestPayload.class);
    }

}
