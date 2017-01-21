package com.samlanning.core.server.hue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.samlanning.core.server.config.ServerConfig.HueConfig;
import com.samlanning.core.server.hue.HueLightStates.LightState;
import com.samlanning.core.server.hue.HueLightStates.LightStateListener;
import com.samlanning.core.server.hue.api.objects.HueApiTypes;
import com.samlanning.core.server.hue.api.objects.HueLight;
import com.samlanning.core.server.lighting.RGBLightValue;
import com.samlanning.core.server.util.JsonConstants;

/**
 * Class to interact with a Philips Hue Hub
 */
public class HueManager {

    private final HueLightStates states = new HueLightStates();
    private final HueConfig config;

    private HueMonitor thread;

    public HueManager(HueConfig config) {
        this.config = config;
    }
    
    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new HueMonitor();
            thread.start();
        }
    }
    
    public void addLightStateListener(LightStateListener listener) {
        states.addLightStateListener(listener);
    }

    public class HueMonitor extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    URL url = new URL("http", config.host, "/api/" + config.username + "/lights");
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("GET");
                    try (InputStream is = c.getInputStream()) {

                        Map<String, HueLight> lights =
                            JsonConstants.JSON.readValue(is, HueApiTypes.LIGHTS_MAP_TYPE);
                        Map<String, LightState> newStates = new HashMap<>();

                        for (Map.Entry<String, HueLight> entry : lights.entrySet()) {
                            HueLight light = entry.getValue();

                            if (light.state != null) {
                                
                                // Color-Correct Hue

                                int effectiveHue;
                                
                                if (light.state.hue >= 2000 && light.state.hue < 8000 ) {
                                    effectiveHue = light.state.hue - 2000;
                                } else if (light.state.hue >= 8000 && light.state.hue < 11000) {
                                    effectiveHue = light.state.hue - 4000;
                                } else if (light.state.hue >= 8000 && light.state.hue < 46000) {
                                    effectiveHue = light.state.hue - 7000;
                                } else if (light.state.hue >= 46000){
                                    effectiveHue = light.state.hue + 1000;
                                } else {
                                    effectiveHue = light.state.hue;
                                }
                                if (effectiveHue < 0)
                                    effectiveHue += 65536;
                                if (effectiveHue >= 65536)
                                    effectiveHue -= 65536;

                                // Convert to float values
                                float hue = effectiveHue / 65536f;
                                float saturation = light.state.sat / 256f;
                                float brightness = light.state.bri / 256f;
                                
                                // Convert to RGB
                                RGBLightValue color =
                                    RGBLightValue.fromHSB(hue, saturation, brightness);

                                newStates.put(entry.getKey(), new LightState(light.state.on, color));
                            }
                        }
                        states.updateStates(newStates);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
