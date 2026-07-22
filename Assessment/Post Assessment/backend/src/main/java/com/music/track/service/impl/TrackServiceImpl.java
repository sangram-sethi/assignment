package com.music.track.service.impl;

import com.music.track.dto.TrackRequest;
import com.music.track.exception.TrackNotFoundException;
import com.music.track.model.Track;
import com.music.track.repository.TrackRepository;
import com.music.track.service.TrackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;

    public TrackServiceImpl(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    @Transactional
    public Track createTrack(TrackRequest trackRequest) {
        Track track = new Track();
        track.setTitle(trackRequest.title());
        track.setAlbumName(trackRequest.albumName());
        track.setReleaseDate(trackRequest.releaseDate());
        track.setPlayCount(trackRequest.playCount());
        return trackRepository.save(track);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteTrack(Long trackId) {
        if (!trackRepository.existsById(trackId)) {
            throw TrackNotFoundException.forId(trackId);
        }
        trackRepository.deleteById(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public Track getTracksByTitle(String title) {
        Track track = trackRepository.findByTitle(title);
        if (track == null) {
            throw TrackNotFoundException.forTitle(title);
        }
        return track;
    }

}
