package com.samlanning.core.server.client_protocol.messages;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage;
import com.samlanning.core.server.client_protocol.messages.types.PingMessage;

public class JsonMessage {

    public final MessageType type;
    
    public JsonMessage(MessageType type){
        this.type = type;
    }
    
    public static JsonMessage fromJson(String json) throws IOException {
        JsonNode rootNode = JsonConstants.JSON.readValue(json, JsonNode.class);
        JsonNode typeNode = rootNode.get("type");
        if(typeNode == null){
            throw new IOException("message is missing field \"type\"");
        }
        MessageType type = JsonConstants.JSON.treeToValue(rootNode.get("type"), MessageType.class);
        switch(type){
            case ping:
                return JsonConstants.JSON.treeToValue(rootNode, PingMessage.class);
            case error:
                return JsonConstants.JSON.treeToValue(rootNode, ErrorMessage.class);
        }
        throw new IOException("Not Implemented");
    }

    public String toJson() throws IOException {
        return JsonConstants.JSON.writeValueAsString(this);
    }
    
}
