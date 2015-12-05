package org.bff.javampd.monitor;

import org.bff.javampd.playlist.PlaylistBasicChangeListener;

public interface PlaylistMonitor extends StatusMonitor {
    void addPlaylistChangeListener(PlaylistBasicChangeListener pcl);

    void removePlaylistStatusChangeListener(PlaylistBasicChangeListener pcl);

    int getSongId();

    void playerStopped();
}
