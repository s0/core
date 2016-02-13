package com.samlanning.core.server.client_protocol.messages.types.events;

public class MediaEventPayload {

    public final State state;
    public final String title;
    public final String artist;
    public final String album;

    public MediaEventPayload(State state, String title, String artist, String album) {
        this.state = state;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public enum State {
        playing,
        paused,
        stopped
    }

}
