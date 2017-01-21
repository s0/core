package com.samlanning.core.server.hue.api.objects;

import java.util.HashMap;

import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.samlanning.core.server.util.JsonConstants;

public class HueApiTypes {

    public static final MapType LIGHTS_MAP_TYPE;
        
    static {
        TypeFactory typeFactory = JsonConstants.JSON.getTypeFactory();
        LIGHTS_MAP_TYPE = typeFactory.constructMapType(HashMap.class, String.class, HueLight.class);
    }
}
