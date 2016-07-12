package com.samlanning.synesthesia.player;

import java.util.Collections;
import java.util.List;

/**
 * A collection of {@link EventMarker EventMarkers} for a specific event type.
 * 
 * This class stores the markers in a way that is easy for an
 * {@link EventPlayer} to consume and "play".
 *
 * @param <Event>
 */
public class EventSheet<Event> {

    private final List<EventMarker<Event>> eventMarkers;

    /**
     * @param eventMarkers a list of EventMarkers sorted in ascending time
     *            order.
     * 
     * @throws IllegalArgumentException if eventMarkers is not sorted, or a
     *             marker has a {@link EventMarker#time} value < 0.
     */
    public EventSheet(List<EventMarker<Event>> eventMarkers) {
        // Check that the events are sorted in
        long lastTime = 0;
        for (EventMarker<Event> marker : eventMarkers) {
            if (marker.time < 0)
                throw new IllegalArgumentException("Marker has time < 0");
            if (marker.time < lastTime)
                throw new IllegalArgumentException("Markers are not in date order");
            lastTime = marker.time;
        }
        this.eventMarkers = Collections.unmodifiableList(eventMarkers);
    }

    /**
     * Read the markers in a sorted order
     */
    public Iterable<EventMarker<Event>> readEventMarkers() {
        return this.eventMarkers;
    }

}
