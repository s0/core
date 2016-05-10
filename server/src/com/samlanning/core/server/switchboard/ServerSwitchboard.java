package com.samlanning.core.server.switchboard;

import java.io.IOException;

import org.slf4j.Logger;

import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage.ErrorType;
import com.samlanning.core.server.config.ServerConfig;
import com.samlanning.core.server.lighting.LightingControl;
import com.samlanning.core.server.lighting.RGBLightValue;
import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.mpd.MPDMonitor.Listener;
import com.samlanning.core.server.util.Logging;

public class ServerSwitchboard {

    private static final Logger log = Logging.logger(ServerSwitchboard.class);

    private final ServerConfig config;
    private MPDMonitor mpdMonitor;
    private LightingControl lightingControl;

    public ServerSwitchboard(ServerConfig config) {
        this.config = config;
    }

    public synchronized void addMPDMonitor(MPDMonitor mpdMonitor) {
        this.mpdMonitor = mpdMonitor;
    }

    public synchronized void addLightingControl(LightingControl lightingControl) {
        this.lightingControl = lightingControl;
    }

    public synchronized void listenToMPD(MPDMonitor.Listener listener) {
        if (mpdMonitor == null)
            throw new RuntimeException("MPDMonitor not setup");
        mpdMonitor.addListener(listener);
    }

    public void removeMPDListener(Listener listener) {
        mpdMonitor.removeListener(listener);
    }

    public synchronized LightingControl lighting() {
        if (lightingControl == null)
            throw new RuntimeException("Lighting not setup");
        return lightingControl;
    }

    public synchronized void performAction(String action) throws ActionError {
        switch (action) {
            case "media_toggle":
                // Toggle Media
                mpdMonitor.toggle();
                return;
        }

        if (action.startsWith("set_light ")) {
            String colorString = action.substring(10);
            RGBLightValue color;
            try {
                color = RGBLightValue.fromString(colorString);
            } catch (IllegalArgumentException e) {
                throw new ActionError(ErrorType.invalid_request, "Invalid Colour: " + colorString);
            }
            if (this.lightingControl == null) {
                throw new ActionError(ErrorType.internal, "Lighting not setup");
            } else {
                this.lightingControl.setColor(color);
            }
        }

        if (action.startsWith("set_light_brightness ")) {
            String brightnessString = action.substring(21);
            float brightness;
            try {
                brightness = Float.parseFloat(brightnessString);
            } catch (NumberFormatException e) {
                throw new ActionError(ErrorType.invalid_request, "Invalid Brightness: "
                    + brightnessString);
            }
            if (this.lightingControl == null) {
                throw new ActionError(ErrorType.internal, "Lighting not setup");
            } else {
                this.lightingControl.setStaticBrightness(brightness);
            }
        }

        // Try command line actions
        String command = config.commandLineAction().get(action);
        if (command != null) {
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                log.error("Error running action command process", e);
                throw new ActionError(ErrorType.internal, "Problem while running Action: " + action);
            }
            return;
        }

        throw new ActionError(ErrorType.invalid_request, "Unknown Switchboard Action: " + action);
    }

}
