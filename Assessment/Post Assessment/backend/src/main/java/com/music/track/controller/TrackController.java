package com.music.track.controller;

import com.music.track.dto.TrackRequest;
import com.music.track.model.Track;
import com.music.track.service.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("music/platform/v1/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Create a track.
     *
     * @param trackRequest the track payload (without id)
     * @return the created track with its generated id and HTTP 201
     */
    @PostMapping
    public ResponseEntity<Track> createTrack(@RequestBody TrackRequest trackRequest) {
        Track created = trackService.createTrack(trackRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all tracks.
     *
     * @return the list of all tracks with HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Track>> getAllTracks() {
        return ResponseEntity.ok(trackService.getAllTracks());
    }

    /**
     * Delete a track by id.
     *
     * @param trackId the id of the track to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long trackId) {
        trackService.deleteTrack(trackId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get a track filtered by title.
     *
     * @param title the title to match
     * @return the matching track with HTTP 200
     */
    @GetMapping("/search")
    public ResponseEntity<Track> getTrackByTitle(@RequestParam String title) {
        return ResponseEntity.ok(trackService.getTracksByTitle(title));
    }

}
