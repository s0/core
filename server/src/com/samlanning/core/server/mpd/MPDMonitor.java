package com.samlanning.core.server.mpd;

import org.bff.javampd.player.Player;
import org.bff.javampd.player.Player.Status;
import org.bff.javampd.server.MPD;
import org.bff.javampd.song.MPDSong;
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

        public void songChanged(MPDSong song);

        default void updateBoth(Status status, MPDSong song) {
            statusChanged(status);
            songChanged(song);
        }
    }

    private class T extends Thread {

        private static final int DELAY = 400;

        private final MPD mpd;
        private Status lastStatus;
        private MPDSong lastSong;

        boolean refreshInturrupt = false;

        private T(MPD mpd) {
            super("MPDMonitor Thread");
            this.mpd = mpd;
        }

        @Override
        public void run() {
            Player player = mpd.getPlayer();
            while (true) {
                final Status status = player.getStatus();
                final MPDSong song = player.getCurrentSong();
                if (status != lastStatus) {
                    MPDMonitor.this.updateNewListenerVisitor(l -> l.updateBoth(status, song));
                    MPDMonitor.this.visitListeners(l -> l.statusChanged(status));
                    lastStatus = status;
                }
                if (song != lastSong && (song == null || !song.equals(lastSong))) {
                    MPDMonitor.this.updateNewListenerVisitor(l -> l.updateBoth(status, song));
                    MPDMonitor.this.visitListeners(l -> l.songChanged(song));
                    lastSong = song;
                }
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    synchronized (this) {
                        if (refreshInturrupt) {
                            refreshInturrupt = false;
                        } else {
                            logger.warn("MPDMonitor Interrupted", e);
                        }
                    }
                }
            }
        }

        /** Trigger a refresh immediately */
        public void refresh() {
            synchronized (this) {
                refreshInturrupt = true;
            }
            this.interrupt();
        }

    }

    public boolean toggle() {
        if (mpd.getPlayer().getStatus() == Status.STATUS_PLAYING)
            mpd.getPlayer().pause();
        else
            mpd.getPlayer().play();
        thread.refresh();
        return true;
    }

}
