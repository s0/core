package com.samlanning.core.server.switchboard;

import com.samlanning.core.server.client_protocol.messages.types.ErrorMessage.ErrorType;
import com.samlanning.core.server.mpd.MPDMonitor;

public class ServerSwitchboard {
    
    private MPDMonitor mpdMonitor;

    public synchronized void addMPDMonitor(MPDMonitor mpdMonitor) {
        this.mpdMonitor = mpdMonitor;
    }
    
    public synchronized void listenToMPD(MPDMonitor.Listener listener){
        if(mpdMonitor == null)
            throw new RuntimeException("MPDMonitor not setup");
        mpdMonitor.addListener(listener);
    }

    public void performAction(String action) throws ActionError {
        switch(action){
            case "media_toggle":
                // Toggle Media
                mpdMonitor.toggle();
                return;
        }
        throw new ActionError(ErrorType.invalid_request, "Unknown Switchboard Action: " + action);
    }

    
    
    
}
