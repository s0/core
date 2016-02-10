package com.samlanning.core.server.switchboard;

import com.samlanning.core.server.mpd.MPDMonitor;

public class ServerSwitchboard {
    
    private MPDMonitor monitor;

    public synchronized void addMPDMonitor(MPDMonitor monitor) {
        this.monitor = monitor;
    }
    
    public synchronized void listenToMPD(MPDMonitor.Listener listener){
        if(monitor == null)
            throw new RuntimeException("MPDMonitor not setup");
        monitor.addListener(listener);
    }

    
    
    
}
