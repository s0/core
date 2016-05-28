package com.samlanning.core.server.client_protocol.messages.types.events;

import com.samlanning.core.server.lighting.RGBLightValue;

public class LightingEventPayload {

    public final String value;

    public LightingEventPayload(RGBLightValue value) {
        this.value = value.toHexString();
    }

}
