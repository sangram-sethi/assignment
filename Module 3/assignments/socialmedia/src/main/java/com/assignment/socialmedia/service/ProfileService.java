package com.assignment.socialmedia.service;

import com.assignment.socialmedia.dto.ProfileDTO;

public interface ProfileService {

    ProfileDTO getProfileByUserId(Long userId);

    ProfileDTO updateProfile(Long userId, ProfileDTO profileDTO);
}
