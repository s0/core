package com.samlanning.core.server.mpd;

import org.bff.javampd.MPD;
import org.bff.javampd.Player;
import org.bff.javampd.Player.Status;
import org.bff.javampd.exception.MPDPlayerException;
import org.slf4j.Logger;

import com.samlanning.core.server.util.Listenable;
import com.samlanning.core.server.util.Logging;

/**
 * Monitor MPD for state changes
 * 
 * The MPD protocol does not have any features for "subscribing" to events, so
 * statuses need to be checked periodically. This class manages checking these
 * things at the rate we want.
 *
 */
public class MPDMonitor extends Listenable<MPDMonitor.Listener> {

    private static final Logger logger = Logging.logger(MPDMonitor.class);

    private final MPD mpd;

    private T thread;

    public MPDMonitor(MPD mpd) {
        this.mpd = mpd;
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new T(mpd);
            thread.start();
        }
    }

    public interface Listener {
        public void statusChanged(Status status);
    }

    private class T extends Thread {

        private static final int DELAY = 400;

        private final MPD mpd;
        private Status status;

        private T(MPD mpd) {
            super("MPDMonitor Thread");
            this.mpd = mpd;
        }

        @Override
        public void run() {
            Player player = mpd.getPlayer();
            while (true) {
                try {
                    Status lastStatus = status;
                    status = player.getStatus();
                    if (status != lastStatus) {
                        MPDMonitor.this.updateNewListenerVisitor(l -> l.statusChanged(status));
                        MPDMonitor.this.visitListeners(l -> l.statusChanged(status));
                    }
                } catch (MPDPlayerException e) {
                    // Wait until nes
                    logger.warn("Exception getting status", e);
                }
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    logger.warn("MPDMonitor Interrupted", e);
                }
            }
        }

    }

}
