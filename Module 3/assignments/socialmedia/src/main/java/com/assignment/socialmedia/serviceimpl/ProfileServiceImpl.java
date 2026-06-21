package com.assignment.socialmedia.serviceimpl;

import com.assignment.socialmedia.dto.ProfileDTO;
import com.assignment.socialmedia.entity.Profile;
import com.assignment.socialmedia.entity.User;
import com.assignment.socialmedia.exception.ResourceNotFoundException;
import com.assignment.socialmedia.repository.ProfileRepository;
import com.assignment.socialmedia.repository.UserRepository;
import com.assignment.socialmedia.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user id: " + userId));
        return toDto(profile);
    }

    @Override
    public ProfileDTO updateProfile(Long userId, ProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setBio(dto.getBio());
        profile.setPhoneNumber(dto.getPhoneNumber());

        userRepository.save(user);
        return toDto(profile);
    }

    private ProfileDTO toDto(Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .build();
    }
}
