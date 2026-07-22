package com.music.track.exception;

/**
 * Thrown when a requested track cannot be found in the datastore.
 */
public class TrackNotFoundException extends RuntimeException {

    public TrackNotFoundException(String message) {
        super(message);
    }

    public static TrackNotFoundException forId(Long id) {
        return new TrackNotFoundException("Track not found with id: " + id);
    }

    public static TrackNotFoundException forTitle(String title) {
        return new TrackNotFoundException("Track not found with title: " + title);
    }
}
