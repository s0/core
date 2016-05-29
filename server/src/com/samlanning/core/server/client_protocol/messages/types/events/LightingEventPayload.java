package com.samlanning.core.server.client_protocol.messages.types.events;

import com.samlanning.core.server.lighting.RGBLightValue;

public class LightingEventPayload {

    public final String color;
    public final String brightness;

    public LightingEventPayload(RGBLightValue color, Float brightness) {
        this.color = color == null ? null : color.toHexString();
        this.brightness = brightness == null ? null : Float.toString(brightness);
    }

}
