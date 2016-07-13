package com.samlanning.synesthesia.database.dummy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samlanning.synesthesia.database.TrackDatabase;
import com.samlanning.synesthesia.track.Track;

public class DummyTrackDatabase implements TrackDatabase {

    private static final Logger logger = LoggerFactory.getLogger(DummyTrackDatabase.class
        .getCanonicalName());
    
    private static final Track ONSTUH;
    
    static {
        List<Long> markers = new ArrayList<>();
        
        int step = 935;
        int offset = 900;
        for (int i = 0; i< 32; i += 2){
            markers.add(Long.valueOf(offset + i * step));
            markers.add(Long.valueOf(offset + (i + 1) * step));
        }
        
        ONSTUH = new Track(markers);
    }

    @Override
    public Track lookupSong(String songTitle, String artist, String album) {
        logger.info("Looking up song: " + songTitle + " by " + artist);
        
        if (songTitle.equals("Onstuh") && artist.equals("Feed Me")) {
            return ONSTUH;
        }
        
        return null;
    }

}
