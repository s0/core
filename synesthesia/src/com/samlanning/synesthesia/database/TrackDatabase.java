package com.samlanning.synesthesia.database;

import com.samlanning.synesthesia.track.Track;

public interface TrackDatabase {

    /**
     * @return the track if found, otherwise null 
     */
    public Track lookupSong(String songTitle, String artist, String album);
    
}
