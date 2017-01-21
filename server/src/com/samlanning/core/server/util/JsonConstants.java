package com.samlanning.core.server.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConstants {

    public static final ObjectMapper JSON;
    
    static {
        JSON = new ObjectMapper();
        JSON.setSerializationInclusion(Include.NON_NULL);
        JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
}
