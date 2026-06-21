package com.assignment.socialmedia.controller;

import com.assignment.socialmedia.dto.ProfileDTO;
import com.assignment.socialmedia.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the one-to-one {@link com.assignment.socialmedia.entity.Profile}
 * keyed by its owning user's id.
 */
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ProfileDTO> updateProfile(@PathVariable Long userId,
                                                    @Valid @RequestBody ProfileDTO request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }
}
