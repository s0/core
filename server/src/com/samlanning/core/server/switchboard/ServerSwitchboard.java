package com.samlanning.core.server.switchboard;

import java.io.IOException;

import org.slf4j.Logger;

import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage.ErrorType;
import com.samlanning.core.server.config.ServerConfig;
import com.samlanning.core.server.mpd.MPDMonitor;
import com.samlanning.core.server.mpd.MPDMonitor.Listener;
import com.samlanning.core.server.util.Logging;

public class ServerSwitchboard {

    private static final Logger log = Logging.logger(ServerSwitchboard.class);

    private final ServerConfig config;
    private MPDMonitor mpdMonitor;

    public ServerSwitchboard(ServerConfig config) {
        this.config = config;
    }

    public synchronized void addMPDMonitor(MPDMonitor mpdMonitor) {
        this.mpdMonitor = mpdMonitor;
    }

    public synchronized void listenToMPD(MPDMonitor.Listener listener) {
        if (mpdMonitor == null)
            throw new RuntimeException("MPDMonitor not setup");
        mpdMonitor.addListener(listener);
    }

    public void removeMPDListener(Listener listener) {
        mpdMonitor.removeListener(listener);
    }

    public void performAction(String action) throws ActionError {
        switch (action) {
            case "media_toggle":
                // Toggle Media
                mpdMonitor.toggle();
                return;
        }

        // Try command line actions
        String command = config.commandLineAction().get(action);
        if (action != null) {
            try {
                Process process = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                log.error("Error running action command process", e);
                throw new ActionError(ErrorType.internal, "Problem while running Action: " + action);
            }
            return;
        }

        throw new ActionError(ErrorType.invalid_request, "Unknown Switchboard Action: " + action);
    }

}
