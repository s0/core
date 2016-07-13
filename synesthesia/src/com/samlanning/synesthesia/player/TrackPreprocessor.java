package com.samlanning.synesthesia.player;

import com.samlanning.synesthesia.track.Track;

/**
 * Contains methods for converting elements of a {@link Track} into the required
 * format to be consumed by a particular {@link EventHandler}.
 * 
 * @param <Event>
 */
public interface TrackPreprocessor<Event> {
    
    public Event processMarker();
    
}
