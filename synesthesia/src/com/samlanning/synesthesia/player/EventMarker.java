package com.samlanning.synesthesia.player;

/**
 * Represents an event that should be fired at a specific point in time.
 *
 * @param <Event> - the type of the event
 */
public class EventMarker<Event> {

    /**
     * The time at which this event is supposed to happen in milliseconds
     * 
     * (relative to the start time of the player)
     */
    public final long time;
    
    public final Event event;

    public EventMarker(long time, Event event) {
        this.time = time;
        this.event = event;
    }
    
}
