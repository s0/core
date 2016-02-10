package com.samlanning.core.server.client_protocol.messages.types.requests;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.samlanning.core.server.client_protocol.messages.JsonConstants;

public class ListenRequestPayload {

    public final String target;

    public ListenRequestPayload(@JsonProperty("target") String target) throws IOException {
        if (target == null)
            throw new IOException("Missing field \"target\"");
        this.target = target;
    }

    public static ListenRequestPayload fromJsonNode(JsonNode payload) throws IOException {
        return JsonConstants.JSON.treeToValue(payload, ListenRequestPayload.class);
    }

}
