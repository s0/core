package com.samlanning.synesthesia.track;

import java.util.List;

public class Track {

    private final List<Long> markers;

    public Track(List<Long> markers) {
        this.markers = markers;
    }

    public List<Long> getMarkers() {
        return markers;
    }

}
