package com.samlanning.synesthesia.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samlanning.synesthesia.database.TrackDatabase;
import com.samlanning.synesthesia.track.Track;

public class SynesthesiaPlayer {

    private static final Logger logger = LoggerFactory.getLogger(SynesthesiaPlayer.class
        .getCanonicalName());

    private final List<ProcessorAndHandler<?>> handlers = new ArrayList<>();
    private final TrackDatabase database;
    
    // List of currently active (started) handlers
    private final List<ActiveHandler<?>> activeHandlers = new ArrayList<>();
    
    public SynesthesiaPlayer(TrackDatabase database) {
        this.database = database;
    }

    /**
     * Notify the {@link SynesthesiaPlayer} that a particular song is playing
     * 
     * @param songStartTime the effective time in milliseconds that the song started playing
     * @param songTitle the title of the song
     * @param artist the song artist
     * @param album the currentl album the song is playing from
     */
    public synchronized void setCurrentSong(long songStartTime, String songTitle, String artist,
        String album) {
        this.stop();
        Track track = this.database.lookupSong(songTitle, artist, album);
        if (track != null) {
            logger.info("Found Track for Song: " + track);
            // Activate handlers with new track
            for(ProcessorAndHandler<?> handler : this.handlers)
                makeHandlerActive(handler, track, songStartTime);
        } else {
            logger.info("No Track found for Song: " + songTitle + " by " + artist);
        }
    }

    private <T> void makeHandlerActive(ProcessorAndHandler<T> handler, Track track,
        long songStartTime) {
        handler.handler.started();
        // Prepare the event sheet for the current handler
        List<EventMarker<T>> eventList = new ArrayList<>();
        for (Long marker : track.getMarkers()) {
            eventList.add(new EventMarker<T>(marker.longValue(), handler.processor.processMarker()));
        }
        
        EventSheet<T> eventSheet = new EventSheet<>(eventList);
        
        // Start the event player and store in activeHandlers
        EventPlayer<T> eventPlayer = new EventPlayer<>(eventSheet, handler.handler);
        eventPlayer.play(songStartTime);
        activeHandlers.add(new ActiveHandler<>(eventPlayer, handler));
    }
 
    /**
     * Notify the {@link SynesthesiaPlayer} that it should stop playing
     */
    public synchronized void stop() {
        logger.info("stop");
        for(ActiveHandler<?> handler : this.activeHandlers) {
            handler.eventPlayer.stop();
            handler.processorAndHandler.handler.stopped();
        }
        this.activeHandlers.clear();
    }

    public synchronized <T> void addHandler(
            TrackPreprocessor<T> processor,
            EventHandler<T> handler) {
        handlers.add(new ProcessorAndHandler<T>(processor, handler));
    }
    
    private static class ProcessorAndHandler<T> {
        
        private final TrackPreprocessor<T> processor;
        private final EventHandler<T> handler;

        public ProcessorAndHandler(TrackPreprocessor<T> processor, EventHandler<T> handler) {
            this.processor = processor;
            this.handler = handler;
        }
        
    }
    
    private static class ActiveHandler<T> {
        private final EventPlayer<T> eventPlayer;
        private final ProcessorAndHandler<T> processorAndHandler;

        public ActiveHandler(EventPlayer<T> eventPlayer, ProcessorAndHandler<T> processorAndHandler) {
            this.eventPlayer = eventPlayer;
            this.processorAndHandler = processorAndHandler;
        }
    }
    
}
